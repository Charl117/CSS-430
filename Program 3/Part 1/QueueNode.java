// Author: Quan Trung Nghiem
// Date: 05/11/2019
// QueueNode implements by SyncQueue to house the waiting threads

import java.util.Vector;

public class QueueNode {

	// Stored tid (thread ID)
	private Vector<Integer> q;

	public QueueNode() {

		// for storing waiting thread
		q = new Vector<>();
	}

	public synchronized int sleep() {
		if (q.size() == 0) {
			// take lock from calling thread and put it to sleep
			try {
				// sleep until monitor have another thread, calls notify()
				wait();
			} catch (InterruptedException e) {
				System.out.println("Thread unable to sleep");
			}

			return q.remove(0); // return the parent
		}
		return -1;
	}

	public synchronized void wakeup(int tid) {
		// add thread to waiting queue
		q.add(tid);
		// wake up the parent
		notify();
	}
}