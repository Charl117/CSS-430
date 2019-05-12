// Author: Quan Trung Nghiem
// Date: 05/11/2019
// Test the runtime with disk threads

class TestThread3B extends Thread {

	private byte[] buffer;

	public TestThread3B ( ) {}
	
	public void run() {
		// one disk block
		buffer = new byte[512];
		for (int i = 0; i < 1000; i++) {
			// write
			SysLib.rawwrite(i, buffer);
			// read
			SysLib.rawread(i, buffer);
		}
		SysLib.exit();
	}
}
