// Author: Quan Trung Nghiem
// Date: 05/11/2019
// Test3 runs TestThread3A and TestThead3b with given value from command 
// Example of command: l Test3 3

import java.util.Date;
import java.util.*;

class Test3 extends Thread {

	private int threads;
	private long subTime; // submission time
	private long compTime;

	public Test3(String[] args) {
		threads = Integer.parseInt(args[0]); // get number of thread pair
	}

	public void run() {
		subTime = new Date().getTime(); // get current time

		String[] testThread3A = SysLib.stringToArgs("TestThread3A"); // call compute thread
		String[] testThread3B = SysLib.stringToArgs("TestThread3B"); // call disk thread

		// run computation thread
		for (int i = 0; i < threads; i++) {
			SysLib.exec(testThread3A);
		}

		// run disk thread
		for (int i = 0; i < threads; i++) {
			SysLib.exec(testThread3B);
		}

		// wait for computation thread finish
		for (int i = 0; i < threads; i++) {
			SysLib.join();
			SysLib.cout("comp finished...\n");
		}

		// wait for disk thread finish
		for (int i = 0; i < threads; i++) {
			SysLib.join();
			SysLib.cout("disk finished...\n");
		}

		compTime = new Date().getTime(); // get running time

		SysLib.cout("Elapsed time: " + (compTime - subTime) + "ms" + "\n");
		SysLib.exit();
	}
}
