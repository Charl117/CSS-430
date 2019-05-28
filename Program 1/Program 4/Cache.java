// Quan Nghiem
// Modified on 05/27/2019
// Cache java file

import java.util.Arrays;

public class Cache {

	public class Entry {
		public int frameIndex; // disk block number of cached data
		public boolean reference; // check if block been access
		public boolean isDirty; // check if block need to write back to disk
		public byte[] buffer;

		// ------------------------ Entry Construction-----------------------//
		// Initial state of all Entry entries
		public Entry(int blockSize) {
			frameIndex = -1; // no disk block number at the moment
			reference = false; // set to false since page has not been accessed
			isDirty = false; // page has not been modified
			buffer = new byte[blockSize];
		}
	}

	private int victim; // index of next victim
	private Entry[] page;

	// ------------------------ Cache Construction-----------------------//
	// Initialized number of cache blocks and each block size
	public Cache(int blockSize, int cacheBlock) {
		page = new Entry[cacheBlock];
		victim = 0; // victim is the start of the array
		for (int i = 0; i < page.length; i++) {
			page[i] = new Entry(blockSize); // initial each cache blook
		}
	}

	// ------------------------ Find Victim -----------------------//
	// Find a free page
	// Set the index of next victim if there is no free page
	public void nextVictim() {
		// Find a free page
		for (int i = victim; i < page.length; i++) {
			if (page[i].frameIndex == -1) {
				page[i].reference = false;
				victim = i;
				break;
			}
		}
		// Set next victim
		while (page[victim].reference == true) {
			// Set it as false so if the check goes to the next iteration
			// Can use this
			page[victim].reference = false;
			victim = (victim + 1) % page.length;
		}
		// if this victim is dirty, write cache block to disk
		if (page[victim].isDirty == true) {
			SysLib.rawwrite(page[victim].frameIndex, page[victim].buffer);
			page[victim].isDirty = false;
		}
	}

	// ------------------------ Read-----------------------//
	// Reads into the buffer[] array the cache block specified by blockID
	// Read disk block if not found in cache block
	public synchronized boolean read(int blockID, byte buffer[]) {
		// If entry is in cache
		for (int i = 0; i < page.length; i++) {
			// Page entry in cache matched blockID
			if (page[i].frameIndex == blockID) {
				// Set reference to true
				page[i].reference = true;
				// Read the buffer content in cache
				System.arraycopy(page[i].buffer, 0, buffer, 0, buffer.length);
				return true;
			}
		}
		// If entry isn't in cache
		try {
			// Find a victim
			nextVictim();
			// Data is read from disk into cache block
			SysLib.rawread(blockID, buffer);
			// Data is read from cache block into accompanying buffer
			System.arraycopy(buffer, 0, page[victim].buffer, 0, buffer.length);
			// Update the victim page
			page[victim].frameIndex = blockID;
			page[victim].reference = true;
			page[victim].isDirty = false;
			victim = (victim + 1) % page.length;
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	// ------------------------ Write -----------------------//
	// Write buffer in cache with blockID
	// If not in cache, find free buffer block
	public synchronized boolean write(int blockID, byte buffer[]) {
		// If entry is in cache
		for (int i = 0; i < page.length; i++) {
			// Page entry in cache matched blockID
			if (page[i].frameIndex == blockID) {
				// Set reference to true
				page[i].reference = true;
				// Set page has been modified
				page[i].isDirty = true;
				// Write buffer content into cache
				System.arraycopy(buffer, 0, page[i].buffer, 0, buffer.length);
				return true;
			}
		}

		// If entry isn't in cache
		try {
			// Find a victim
			nextVictim();
			// Write buffer content into victim cache
			System.arraycopy(buffer, 0, page[victim].buffer, 0, buffer.length);
			// Update the victim page
			page[victim].frameIndex = blockID;
			page[victim].reference = true;
			page[victim].isDirty = true;
			victim = (victim + 1) % page.length;
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	// ------------------------ Sync -----------------------//
	// Write all dirty cache block to DISK
	public void sync() {
		for (int i = 0; i < page.length; i++) {
			if (page[i].isDirty == true) {
				// Write cache content into DISK
				SysLib.rawwrite(page[i].frameIndex, page[i].buffer);
				page[i].isDirty = false;
			}
		}
	}

	// ------------------------ Flush -----------------------//
	// Write all dirty cache block to DISK
	// Reset reference and frameIndex
	public void flush() {
		for (int i = 0; i < page.length; i++) {
			if (page[i].isDirty == true) {
				// Write cache content into DISK
				SysLib.rawwrite(page[i].frameIndex, page[i].buffer);
				page[i].isDirty = false;
			}
			// reset each entry
			page[i].frameIndex = -1;
			page[i].reference = false;
		}
		victim = 0; // reset victim as the start of the array
	}
}
