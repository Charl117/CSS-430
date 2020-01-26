// --------------------------- SuperBlock.java--------------------------------
// Jessela Budiman, Cuong Vo, Quan Nghiem
// CSS 430 Section A
// Creation Date: 6/3/19
// Date of Last Modification: 6/8/19
// --------------------------------------------------------------------------
// Purpose:  It is a block of metadata that describes the file system
// and its componenets. It read the physical SuperBlock from disk,
// then find free blocks, add blocks to the free list, and write back to disk
// the contents of SuperBlock. If fails, it will format the disk
// and write a new SuperBlock to disk.
//The first disk block, block 0, is called the superblock and describes
//The number of disk blocks
//The number of inodes
//The block number of the head block of the free list
// --------------------------------------------------------------------------
public class SuperBlock {
	private final int defaultInodeBlocks = 64;
	private final int totalBlockLocation = 0;
	private final int totalInodeLocation = 4;
	private final int freeListLocation = 8;
	private final int defaultBlocks = 1000;

    public int totalBlocks;
    public int totalInodes;
    public int freeList;
	public int inodeBlocks;

	// -----------------------SuperBlock(int)----------------------------------
	// constructor accepts int arg equal to the total number of blocks on Disk
	// read the SuperBlock from disk and initialize member variables
	// the number of inodes, and the block number of the free list’s head.
	public SuperBlock(int diskSize){

		byte [] superBlock = new byte[Disk.blockSize];

		SysLib.rawread(0, superBlock);

		totalBlocks = SysLib.bytes2int(superBlock, totalBlockLocation);
		totalInodes = SysLib.bytes2int(superBlock, totalInodeLocation);
		freeList = SysLib.bytes2int(superBlock, freeListLocation);
		inodeBlocks = totalInodes;

		if (totalInodes > 0 && freeList >= 2&&totalBlocks == diskSize){
			return;
		}
		else
		{
			totalBlocks = diskSize;
			format(defaultInodeBlocks);
		}
	}
	// ------------------------------sync()------------------------------------
	// Sync brings the SuperBlock contents (at block zero on disk) to update
	// Sync will write back to disk the total number of blocks, the total
	// number of inodes, and the free list.
	public void sync ()
	{
		byte[] tempData = new byte[Disk.blockSize];
		SysLib.int2bytes(freeList, tempData, freeListLocation);
		SysLib.int2bytes(totalBlocks, tempData, totalBlockLocation);
		SysLib.int2bytes(totalInodes, tempData, totalInodeLocation);
		SysLib.rawwrite(0, tempData);
	}
	// ------------------------------nextFreeBlock()---------------------------
	//  nextFreeBlock returns the first free block from the free list
	//  The free block is the top block fromthe free queue and is returned
	//  as an int. If there are no free blocks, -1 is returned
	public int nextFreeBlock()
	{
		if (freeList < totalBlocks && freeList > 0)
		{
			byte[] tempData = new byte[Disk.blockSize];
			SysLib.rawread(freeList, tempData);
			int freeBlockNum = freeList;

			// update next free block
			freeList = SysLib.bytes2int(tempData, 0);

			// return block location
			return freeBlockNum;
		}
		return -1;
	}
	// ------------------------------returnFreeBlock(int)----------------------
	// returnFreeBlock add a newly freed block back to the free list
	// The newly freed block is added to the end of the free block queue
	public boolean returnFreeBlock(int blockNumber)
	{
		if (blockNumber > 0 && blockNumber < totalBlocks)
		{
			int nextFree = freeList;
			int temp = 0;

			byte [] next = new byte[Disk.blockSize];
			byte [] newBlock = new byte[Disk.blockSize];

			for(int i = 0; i < Disk.blockSize; i++)
			{
				newBlock[i] = 0;
			}
			SysLib.int2bytes(-1, newBlock, 0);

			while (nextFree != -1)
			{
				SysLib.rawread(nextFree, next);
				temp = SysLib.bytes2int(next, 0);

				if (temp == -1)
				{
					SysLib.int2bytes(blockNumber, next, 0);
					SysLib.rawwrite(nextFree, next);
					SysLib.rawwrite(blockNumber, newBlock);

					return true;
				}
				nextFree = temp;
			}
		}
		return false;
	}

	// ------------------------------format(int)-------------------------------
	// format cleans the disk of all data and reset correct structure
	// if SuperBlock detects an illegal state, variables of SuperBlock reset
	//	default values and written back to the newly cleared disk.
    public void format (int numberOfBlock){
		if (numberOfBlock < 0)
		{
			numberOfBlock = defaultInodeBlocks;
		}

		totalInodes = numberOfBlock;
		inodeBlocks = totalInodes;
		Inode dummy = null;

		for (int i = 0; i < totalInodes; i++)
		{
			dummy = new Inode();
			dummy.flag = 0;
			dummy.toDisk((short) i);
		}

		freeList = (totalInodes / 16) + 2;

		byte [] newEmpty = null;

		for (int i = freeList; i < defaultBlocks - 1; i++)
		{
			newEmpty = new byte [Disk.blockSize];

			for (int j = 0; j < Disk.blockSize; j++)
			{
				newEmpty[j] = 0;
			}

			SysLib.int2bytes(i+1, newEmpty, 0);
			SysLib.rawwrite(i, newEmpty);
		}

		newEmpty = new byte[Disk.blockSize];

		for (int j = 0; j < Disk.blockSize; j++)
		{
			newEmpty[j] = 0;
		}

		SysLib.int2bytes(-1, newEmpty, 0);
		SysLib.rawwrite(defaultBlocks - 1, newEmpty);
		byte[] newSuperblock = new byte[Disk.blockSize];

		SysLib.int2bytes(totalBlocks, newSuperblock, totalBlockLocation);
		SysLib.int2bytes(totalInodes, newSuperblock, totalInodeLocation);
		SysLib.int2bytes(freeList, newSuperblock, freeListLocation);

		SysLib.rawwrite(0, newSuperblock);

    }
}
