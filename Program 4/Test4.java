// Quan Nghiem
// Modified on 05/27/2019
// Test4 java file

import java.util.Arrays;
import java.util.Date;
import java.util.Random;

public class Test4 extends Thread {
	private static final int blockSize = 512;
	private static final int arraySize = 500;
	private byte[] writeBlock;
	private byte[] readBlock;
	private int test = 0;
	private boolean isCaching = false;
	private long startTime;
	private long endTime;
	private Random random;
	private String testName = " ";

	public Test4(String args[]) {
		test = Integer.parseInt(args[1]);
		writeBlock = new byte[blockSize];
		readBlock = new byte[blockSize];
		random = new Random();
		random.nextBytes(writeBlock);
		if (args[0].toLowerCase().equals("enabled") || args[0].toLowerCase().equals("-enabled")) {
			isCaching = true;
		}
	}

	public void run() {
		SysLib.flush();
		// Test cases depend on the test value
		switch (test) {
		case 1:
			randomAccess();
			break;
		case 2:
			localizedAccess();
			break;
		case 3:
			mixedAccess();
			break;
		case 4:
			adversaryAccess();
			break;
		default:
			SysLib.cout("WARNING: Invalid Argument\n ");
			break;
		}
		sync();
		SysLib.exit();
	}

	// Helper read method for cache enable and disable
	public void read(int blockId, byte buffer[]) {
		if (isCaching) {
			// Cache read
			SysLib.cread(blockId, buffer);
		} else {
			SysLib.rawread(blockId, buffer);
		}
	}

	// Helper write method for cache enable and disable
	public void write(int blockId, byte buffer[]) {
		if (isCaching) {
			// Cache wrtie
			SysLib.cwrite(blockId, buffer);
		} else {
			SysLib.rawwrite(blockId, buffer);
		}
	}

	// Sync helper method for cache enable and disable
	public void sync() {
		if (isCaching) {
			// Cache sync
			SysLib.csync();
		} else {
			SysLib.sync();
		}
	}

	// Result helper method
	// Print out results
	public void result() {
		String status = isCaching ? "Enabled" : "Disabled";
		SysLib.cout(testName + "\n");
		SysLib.cout("Caching: " + status + "\n");
		SysLib.cout("Execution time: " + (endTime - startTime) + "\n");
		SysLib.cout("Average time: " + ((endTime - startTime) / arraySize) + "\n");
	}

	// Random access
	public void randomAccess() {
		testName = "Random Access Test";
		int[] array = new int[arraySize];
		for (int i = 0; i < arraySize; i++) {
			array[i] = Math.abs(random.nextInt() % 512);
		}
		// Get start time
		startTime = (new Date()).getTime();
		// Perform write
		for (int i = 0; i < arraySize; i++) {
			write(array[i], writeBlock);
		}
		// Perform read
		for (int i = 0; i < arraySize; i++) {
			read(array[i], readBlock);
		}
		endTime = (new Date()).getTime();
		result();
	}

	// Localized Access
	public void localizedAccess() {
		testName = "Localized Access Test";
		startTime = (new Date()).getTime();
		// Perform write
		for (int i = 0; i < arraySize; i++) {
			for (int j = 0; j < 10; j++) {
				write(j, writeBlock);
			}
		}
		// Perform read
		for (int i = 0; i < arraySize; i++) {
			for (int j = 0; j < 10; j++) {
				read(j, readBlock);
			}
		}
		endTime = (new Date()).getTime();
		result();
	}

	// Mixed Access
	public void mixedAccess() {
		testName = "Mixed Access Test";
		int[] array = new int[arraySize];
		for (int i = 0; i < arraySize; i++) {
			if ((Math.abs(random.nextInt() % 10)) < 9) {
				array[i] = Math.abs(random.nextInt() % 10);
			} else {
				array[i] = Math.abs(random.nextInt() % 512);
			}
		}
		// Get start time
		startTime = (new Date()).getTime();
		// Perform write
		for (int i = 0; i < arraySize; i++) {
			write(array[i], writeBlock);
		}
		// Perform read
		for (int i = 0; i < arraySize; i++) {
			read(array[i], readBlock);
		}
		endTime = (new Date()).getTime();
		result();
	}

	// Adversary Access
	public void adversaryAccess() {
		testName = "Adversary Access Test";
		startTime = (new Date()).getTime();
		// Perform write
		for (int i = 0; i < blockSize; i++) {
			write(i, writeBlock);
		}
		// Perform read
		for (int i = 0; i < blockSize; i++) {
			read(i, readBlock);
		}
		endTime = (new Date()).getTime();
		result();
	}
}