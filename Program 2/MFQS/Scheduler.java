// Author: Quan Trung Nghiem
// Date: 04/28/2019
// Scheduler implementing multilevel feedback queue
// Changed the default time slice to 500 ms
// millisecond = ms
// quantum = time in millisecond
// Changed from one Vector to a Vector array
// Modified schedulerSleep(), run(), and all initialization method to reflect the new Vector array
// Modified based on the source code Scheduler.java given by Professor Yusuf Pisan

import java.util.*;

@SuppressWarnings("deprecation")

public class Scheduler extends Thread {
	private Vector[] queue = new Vector[3]; // 3 queues
	private int timeSlice;
	private static final int DEFAULT_TIME_SLICE = 500;

	// New data added to p161
	private boolean[] tids; // Indicate which ids have been used
	private static final int DEFAULT_MAX_THREADS = 10000;

	// A new feature added to p161
	// Allocate an ID array, each element indicating if that id has been used
	private int nextId = 0;

	private void initTid(int maxThreads) {
		tids = new boolean[maxThreads];
		for (int i = 0; i < maxThreads; i++)
			tids[i] = false;
	}

	// A new feature added to p161
	// Search an available thread ID and provide a new thread with this ID
	private int getNewTid() {
		for (int i = 0; i < tids.length; i++) {
			int tentative = (nextId + i) % tids.length;
			if (tids[tentative] == false) {
				tids[tentative] = true;
				nextId = (tentative + 1) % tids.length;
				return tentative;
			}
		}
		return -1;
	}

	// A new feature added to p161
	// Return the thread ID and set the corresponding tids element to be unused
	private boolean returnTid(int tid) {
		if (tid >= 0 && tid < tids.length && tids[tid] == true) {
			tids[tid] = false;
			return true;
		}
		return false;
	}

	// A new feature added to p161
	// Retrieve the current thread's TCB from the queue
	public TCB getMyTcb() {
		Thread myThread = Thread.currentThread(); // Get my thread object
		synchronized (queue) {
			for (int j = 0; j < 3; j++) {
				for (int i = 0; i < queue[j].size(); i++) {
					TCB tcb = (TCB) queue[j].elementAt(i);
					Thread thread = tcb.getThread();
					if (thread == myThread) // if this is my TCB, return it
						return tcb;
				}
			}
		}
		return null;
	}

	// A new feature added to p161
	// Return the maximal number of threads to be spawned in the system
	public int getMaxThreads() {
		return tids.length;
	}

	public Scheduler() {
		timeSlice = DEFAULT_TIME_SLICE;
		for (int i = 0; i < 3; i++) {
			queue[i] = new Vector();
		}
		initTid(DEFAULT_MAX_THREADS);
	}

	public Scheduler(int quantum) {
		timeSlice = quantum;
		for (int i = 0; i < 3; i++) {
			queue[i] = new Vector();
		}
		initTid(DEFAULT_MAX_THREADS);
	}

	// A new feature added to p161
	// A constructor to receive the max number of threads to be spawned
	public Scheduler(int quantum, int maxThreads) {
		timeSlice = quantum;
		for (int i = 0; i < 3; i++) {
			queue[i] = new Vector();
		}
		initTid(maxThreads);
	}

	private void schedulerSleep(int timeSlice) {
		try {
			Thread.sleep(timeSlice);
		} catch (InterruptedException e) {
		}
	}

	// A modified addThread of p161 example
	public TCB addThread(Thread t) {
		TCB parentTcb = getMyTcb(); // get my TCB and find my TID
		int pid = (parentTcb != null) ? parentTcb.getTid() : -1;
		int tid = getNewTid(); // get a new TID
		if (tid == -1)
			return null;
		TCB tcb = new TCB(t, tid, pid); // create a new TCB
		queue[0].add(tcb); // always add in queue 0
		return tcb;
	}

	// A new feature added to p161
	// Removing the TCB of a terminating thread
	public boolean deleteThread() {
		TCB tcb = getMyTcb();
		if (tcb != null)
			return tcb.setTerminated();
		else
			return false;
	}

	public void sleepThread(int milliseconds) {
		try {
			sleep(milliseconds);
		} catch (InterruptedException e) {
		}
	}

	// A modified run of p161
	public void run() {
		int quantum = 0;
		while (true) {
			try {
				if (queue[0].size() > 0) { // if queue 0 not empty
					runQ0();
				}

				// queue 0 empty, but queue 1 isn't
				if (queue[0].size() == 0 && queue[1].size() > 0) {
					quantum = runQ1(quantum);
				}
				
				// queue 0 and queue 1 all empty, but queue 2 isn't
				if (queue[0].size() == 0 && queue[1].size() == 0 && queue[2].size() > 0) {
					quantum = runQ2(quantum);
				}

			} catch (NullPointerException e3) {
			}
			;
		}
	}

	/*----------------- Helper Methods of run() ---------------------------*/
	public void runQ0() {
		TCB currentTCB = (TCB) queue[0].firstElement(); // choose front TCB
		Thread current = currentTCB.getThread(); // get first process

		// start process if there is a process
		if (current != null) {
			current.start();
		}

		// process running with 500 millisecond (ms)
		schedulerSleep(DEFAULT_TIME_SLICE);

		// if process done within 500ms
		if (currentTCB.getTerminated() == true) {
			queue[0].remove(currentTCB); // remove from queue 0
			returnTid(currentTCB.getTid());
		}

		// move to queue 1 if process is not done
		synchronized (queue[0]) {
			if (current != null && current.isAlive()) {
				current.suspend();
			}
			queue[0].remove(currentTCB); // move from queue 0
			queue[1].add(currentTCB); // add to queue 1
		}
	}

	public int runQ1(int quantum) {
		TCB currentTCB = (TCB) queue[1].firstElement();
		Thread current = currentTCB.getThread();

		// resume the process from queue 1
		if (current != null) {
			if (current.isAlive()) {
				current.resume();
			}
		}

		// process running with 500 millisecond (ms)
		schedulerSleep(timeSlice);

		// increase quantum time for the process to run
		quantum += timeSlice;

		// process done, remove from queue 1
		if (currentTCB.getTerminated() == true) {
			quantum = 0;
			queue[1].remove(currentTCB);
			returnTid(currentTCB.getTid());
		}

		else if (quantum == 1000) {
			// Process has not finish
			if (current != null && current.isAlive()) {
				current.suspend();
			}
			queue[1].remove(currentTCB); // remove from queue 1
			queue[2].add(currentTCB); // add to queue 2
			quantum = 0; // set quantum back to 0
		}
		return quantum;
	}

	public int runQ2(int quantum) {
		TCB currentTCB = (TCB) queue[2].firstElement();
		Thread current = currentTCB.getThread();

		// resume the process from queue 2
		if (current != null) {
			if (current.isAlive()) {
				current.resume();
			}
		}

		// process running with 500 millisecond (ms)
		schedulerSleep(timeSlice);

		// increase quantum time for the process to run
		quantum += timeSlice;

		// process done, remove from queue 2
		if (currentTCB.getTerminated() == true) {
			quantum = 0;
			queue[2].remove(currentTCB);
			returnTid(currentTCB.getTid());
		}

		else if (quantum == 2000) {
			// Process has not finish
			if (current != null && current.isAlive()) {
				current.suspend();
			}
			queue[2].remove(currentTCB); // remove from front of queue 2
			queue[2].add(currentTCB); // add to the back of queue 2
			quantum = 0; // set quantum back to 0
		}

		// reset quantum for new process
		if (queue[0].size() > 0) {
			quantum = 0;
		}

		return quantum;
	}
}
