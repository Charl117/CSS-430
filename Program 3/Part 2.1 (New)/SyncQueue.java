// Author: Quan Trung Nghiem
// Date: 05/11/2019
// Initialized and define queue to store waiting threads

public class SyncQueue {
	// enqueue all waiting threads based on a given condition
	private QueueNode[] q;

	// Initialized q with capacity for 10 threads
	public SyncQueue() {
		q = new QueueNode[10];
		for (int i = 0; i < 10; i++) {
			q[i] = new QueueNode();
		}
	}

	// initialize queue which set max capacity to wait on condition
	public SyncQueue(int max) {
		q = new QueueNode[max];
		for (int i = 0; i < max; i++) {
			q[i] = new QueueNode();
		}
	}

	// enqueue the calling thread
	// put to sleep until given condition is satisfied
	public int enqueueAndSleep(int condition) {
		return (condition > -1 && condition < q.length) ? q[condition].sleep() : -1;
	}

	// dequeue and wake up thread waiting for given condition
	public void dequeueAndWakeup(int condition, int tid) {
		if (condition > -1 && condition < q.length) {
			q[condition].wakeup(tid);
		}
	}

	// dequeue and wake up a thread waiting for given condition
	public void dequeueAndWakeup(int condition) {
		this.dequeueAndWakeup(condition, 0);
	}
}