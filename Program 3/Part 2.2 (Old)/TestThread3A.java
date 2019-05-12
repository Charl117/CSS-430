// Author: Quan Trung Nghiem
// Date: 05/11/2019
// Test the runtime with computation threads

class TestThread3A extends Thread {

	public void run() {
		function(5);
		SysLib.cout("comp finished...\n");
		SysLib.exit();
	}

	// A function with O(n!) running time
	public void function(int n) {
		if (n <= 0) {
			return;
		}
		for (int i = 0; i < n; i++) {
			function(n - 1);
		}
	}
}
