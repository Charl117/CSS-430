// --------------------------- FileSystem.java--------------------------------
// Jessela Budiman, Cuong Vo, Quan Nghiem
// CSS 430 Section A
// Creation Date: 6/3/19
// Date of Last Modification: 6/8/19
// --------------------------------------------------------------------------
// Contains FileTableEntries
// --------------------------------------------------------------------------

import java.util.Vector;

public class FileTable {
	public final static int UNUSED = 0;
	public final static int USED = 1;
	public final static int READ = 2;
	public final static int WRITE = 3;

	private Vector<FileTableEntry> table; // the actual entity of this file table
	private Directory dir; // the root directory

	// Constructor
	public FileTable(Directory directory) {
		table = new Vector<FileTableEntry>(); // instantiate a file (structure) table
		dir = directory; // receive a reference to the Director
	}

	// Create a FileTableEntry
	// Ensure only 1 thread can acess the file when writing
	public synchronized FileTableEntry falloc(String filename, String mode) {
		short iNumber = -1; // inode number
		Inode inode = null; // holds inode

		while (true) {
			// get the inumber form the inode for given file name
			iNumber = (filename.equals("/") ? (short) 0 : dir.namei(filename));

			// if the inode exist
			if (iNumber >= 0) {
				inode = new Inode(iNumber);

				// read mode
				if (mode.equals("r")) {
					// Change the inode flag to read if not writing
					if (inode.flag == READ || inode.flag == USED || inode.flag == UNUSED) {
						// change the flag of the node to read and break
						inode.flag = READ;
						break;
						// if file is being written
					} else if (inode.flag == WRITE) {
						try {
							wait();
						} catch (InterruptedException e) {
						}
					}
					// other mode
				} else {
					// If inode flag is used or unused
					if (inode.flag == USED || inode.flag == UNUSED) {
						inode.flag = WRITE;
						break;
						// if the flag is read or write, wait until they finish
					} else {
						try {
							wait();
						} catch (InterruptedException e) {
						}
					}
				}
				// Create a new inode if does not exist in given file
			} else if (!mode.equals("r")) {
				iNumber = dir.ialloc(filename);
				inode = new Inode(iNumber);
				inode.flag = WRITE;
				break;
			} else {
				return null;
			}
		}
		inode.count++; // increse the number of users
		inode.toDisk(iNumber);
		// create new file table entry and add it to the file table
		FileTableEntry entry = new FileTableEntry(inode, iNumber, mode);
		table.addElement(entry);
		return entry;
	}

	// Close or remove FileTableEntry from cache
	public synchronized boolean ffree(FileTableEntry entry) {
		// receive a file table entry reference
		Inode inode = new Inode(entry.iNumber);
		// try and remove FTE if it is in table, return true
		if (table.remove(entry)) {
			if (inode.flag == READ) {
				if (inode.count == 1) {
					// free this file table entry.
					notify();
					inode.flag = USED;
				}
			} else if (inode.flag == WRITE) {
				inode.flag = USED;
				notifyAll();
			}
			// decrease the count of users of that file
			inode.count--;
			// save to disk
			inode.toDisk(entry.iNumber);
			return true;
		}
		return false;
	}

	// Check if there is filetableentry in File Table
	public synchronized boolean fempty() {
		return table.isEmpty(); // return if table is empty
	}
}
