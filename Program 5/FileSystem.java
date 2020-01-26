// --------------------------- FileSystem.java--------------------------------
// Jessela Budiman, Cuong Vo, Quan Nghiem
// CSS 430 Section A
// Creation Date: 6/3/19
// Date of Last Modification: 6/8/19
// --------------------------------------------------------------------------
// FileSystem for interacting with files
// --------------------------------------------------------------------------

public class FileSystem {
	// Variables
	private SuperBlock superblock;
	private Directory directory;
	private FileTable filetable;

	// Construction methods
	public FileSystem(int blocks) {
		superblock = new SuperBlock(blocks);
		directory = new Directory(superblock.inodeBlocks);
		filetable = new FileTable(directory);

		// read the "/" file from disk
		FileTableEntry entry = open("/", "r");
		int directorySize = fsize(entry);
		// data in the directory
		if (directorySize > 0) {
			// read then copy to fsDirectory
			byte[] data = new byte[directorySize];
			read(entry, data);
			directory.bytes2directory(data);
		}
		close(entry);
	}

	// Syncs file system back to physical disk
	// Write data from directory to disk in byte in root directory
	public void sync() {
		byte[] tempData = directory.directory2bytes();
		// open root directory with write access
		FileTableEntry root = open("/", "w");
		// write directory to root
		write(root, directory.directory2bytes());
		// close root directory
		close(root);
		// sync superblock
		superblock.sync();
	}

	// Erase all contents of disk
	// Reset superblock, directory, and file tables
	public boolean format(int files) {
		// format on superblock # from parameter
		superblock.format(files);
		// reset directory
		directory = new Directory(superblock.inodeBlocks);
		// reset filetable
		filetable = new FileTable(directory);
		// return true on completion
		return true;
	}

	// Open a file specified by filename and based on the mode
	// Create a new FileTableEntry
	public FileTableEntry open(String filename, String mode) {
		// Creat a new filetable entry, specifying the filename and the mode
		FileTableEntry ftEntry = filetable.falloc(filename, mode);
		// write mode
		if (mode == "w") {
			// unallocated all blocks
			if (!deallocAllBlocks(ftEntry)) {
				return null;
			}
		}
		// return FileTableEntry
		return ftEntry;
	}

	// Close the file with FileTableEntry matched the parameter.
	public boolean close(FileTableEntry entry) {
		// synchronize entry
		synchronized (entry) {
			// decrease the number of users
			entry.count--;
			if (entry.count == 0) {
				return filetable.ffree(entry);
			}
			return true;
		}
	}

	// Read the file with FileTableEntry matched the parameter.
	public int read(FileTableEntry entry, byte[] buffer) {
		// check write or append status
		if ((entry.mode == "w") || (entry.mode == "a")) {
			return -1;
		}

		int size = buffer.length; // size of data to read
		int rData = 0; // data read
		int rError = -1; // error on read
		int remainSize = 0; // how much is left to read

		// synchronize entry
		synchronized (entry) {
			// loop to read
			while (entry.seekPtr < fsize(entry) && (size > 0)) {
				int currentBlock = entry.inode.findTargetBlock(entry.seekPtr);
				if (currentBlock == rError) {
					break;
				}
				byte[] data = new byte[512];
				// Read data into buffer
				SysLib.rawread(currentBlock, data);
				int dataOffset = entry.seekPtr % 512;
				int blocksLeft = 512 - remainSize;
				int fileLeft = fsize(entry) - entry.seekPtr;

				if (blocksLeft < fileLeft) {
					remainSize = blocksLeft;
				} else {
					remainSize = fileLeft;
				}
				if (remainSize > size) {
					remainSize = size;
				}
				System.arraycopy(data, dataOffset, buffer, rData, remainSize);
				rData += remainSize;
				entry.seekPtr += remainSize;
				size -= remainSize;
			}
			return rData;
		}
	}

	// Write from buffer to the file with FileTableEntry matched the parameter.
	public int write(FileTableEntry entry, byte[] buffer) {
		int writeByte = 0;
		int bufferSize = buffer.length;

		if (entry == null || entry.mode == "r") {
			return -1;
		}

		// synchronize entry
		synchronized (entry) {
			while (bufferSize > 0) {
				int location = entry.inode.findTargetBlock(entry.seekPtr);

				// if current block null
				if (location == -1) {
					short newLocation = (short) superblock.nextFreeBlock();

					int tempPtr = entry.inode.registerTargetBlock(entry.seekPtr, newLocation);

					if (tempPtr == -3) {
						short freeBlock = (short) this.superblock.nextFreeBlock();

						// ptr is empty
						if (!entry.inode.setIndexBlock(freeBlock)) {
							return -1;
						}

						// check block pointer error
						if (entry.inode.registerTargetBlock(entry.seekPtr, newLocation) != 0) {
							return -1;
						}

					} else if (tempPtr == -2 || tempPtr == -1) {
						return -1;
					}

					location = newLocation;
				}

				byte[] tempBuff = new byte[512];
				SysLib.rawread(location, tempBuff);

				int tempPtr = entry.seekPtr % 512;
				int diff = 512 - tempPtr;

				if (diff > bufferSize) {
					System.arraycopy(buffer, writeByte, tempBuff, tempPtr, bufferSize);
					SysLib.rawwrite(location, tempBuff);

					entry.seekPtr += bufferSize;
					writeByte += bufferSize;
					bufferSize = 0;
				} else {
					System.arraycopy(buffer, writeByte, tempBuff, tempPtr, diff);
					SysLib.rawwrite(location, tempBuff);

					entry.seekPtr += diff;
					writeByte += diff;
					bufferSize -= diff;
				}
			}

			// update inode length if seekPtr larger
			if (entry.seekPtr > entry.inode.length) {
				entry.inode.length = entry.seekPtr;
			}
			entry.inode.toDisk(entry.iNumber);
			return writeByte;
		}
	}

	// Set then Return seek pointer matched given entry
	// Set the seek pointer to 0 if attemps to set a negative seek pointer
	public int seek(FileTableEntry entry, int offset, int location) {
		// synchronize entry
		synchronized (entry) {
			switch (location) {
			// beginning of file
			case 0:
				// set pointer to offset
				entry.seekPtr = offset;
				break;
			// current position
			case 1:
				// add offset to pointer
				entry.seekPtr += offset;
				break;
			// End of file
			case 2:
				// set pointer to size + offset
				entry.seekPtr = entry.inode.length + offset;
				break;
			// unsuccessful
			default:
				return -1;
			}

			// Set to 0 if goes into negative
			if (entry.seekPtr < 0) {
				entry.seekPtr = 0;
			}
			
			// Go over entry length, set to length
			if (entry.seekPtr > entry.inode.length) {
				entry.seekPtr = entry.inode.length;
			}

			return entry.seekPtr;
		}
	}

	// Deallocate block checks of valid indoes block of entry
	private boolean deallocAllBlocks(FileTableEntry entry) {
		short invalid = -1;
		if (entry.inode.count != 1) {
			SysLib.cerr("Null Pointer");
			return false;
		}
		for (short blockId = 0; blockId < entry.inode.directSize; blockId++) {
			if (entry.inode.direct[blockId] != invalid) {
				superblock.returnFreeBlock(blockId);
				entry.inode.direct[blockId] = invalid;
			}
		}
		byte[] data = entry.inode.unregisterIndexBlock();

		if (data != null) {
			short blockId;
			while ((blockId = SysLib.bytes2short(data, 0)) != invalid) {
				superblock.returnFreeBlock(blockId);
			}
		}
		entry.inode.toDisk(entry.iNumber);
		return true;
	}

	// Delete a specified file with the parameter filename
	boolean delete(String filename) {
		FileTableEntry tcb = open(filename, "w"); // open the TCB (iNode)
		if (directory.ifree(tcb.iNumber) && close(tcb)) { // try to free then delete
			return true; // Delete was completed
		} else {
			return false;
		}
	}

	// Returns the file size in bytes atomically.
	public synchronized int fsize(FileTableEntry entry) {
		// synchronize entry
		synchronized (entry) {
			// Set new Inode object to the entries Inode
			Inode inode = entry.inode;
			// return the length on the new Inode object
			return inode.length;
		}
	}
}
