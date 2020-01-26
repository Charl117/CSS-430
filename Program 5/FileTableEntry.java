// --------------------------- FileSystem.java--------------------------------
// Jessela Budiman, Cuong Vo, Quan Nghiem
// CSS 430 Section A
// Creation Date: 6/3/19
// Date of Last Modification: 6/8/19
// --------------------------------------------------------------------------
// Contains variables associate with a file
// --------------------------------------------------------------------------

public class FileTableEntry {
	public int seekPtr; // a file seek pointer
	public final Inode inode; // a reference to its inode
	public final short iNumber; // this inode number
	public int count; // # threads sharing this entry
	public final String mode; // "r", "w", "w+", or "a"

	public FileTableEntry(Inode i, short inumber, String m) {
		seekPtr = 0; // the seek pointer is set to the file top
		inode = i;
		iNumber = inumber;
		count = 1; // at least on thread is using this entry
		mode = m; // once access mode is set, it never changes
		if (mode.compareTo("a") == 0) // if mode is append,
			seekPtr = inode.length; // seekPtr points to the end of file
	}
}
