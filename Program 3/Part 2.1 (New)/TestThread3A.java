
// Author: Quan Trung Nghiem
// Date: 05/11/2019
// Test the runtime with computation threads

import java.util.*;
import java.lang.*;

class TestThread3A extends Thread {

	public TestThread3A() {
	}

	public void run() {
		for (int j = 0; j < 10000; j++) {
			for (int i = 0; i < 10000; i++) {
				Math.sqrt(Math.tan(Math.sqrt(fact(15))));
			}
		}
		SysLib.exit();
	}

	private int fact(int factNum) {
		return factHelper(factNum);
	}

	private int factHelper(int fact) {
		if (fact == 1) {
			return 1;
		}
		int result = factHelper(fact - 1) * fact;
		return result;
	}
}
