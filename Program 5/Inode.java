
//------------------------------------------------------------
// File name: Inode.java
// Date: 6/8/2019 
// Author: Cuong Vo
//         Jessela Budiman
//         Quan Nghiem
// Describe: Each inode describes one file. Our inode is a simplified version
//           of the Unix inode. It includes 12 pointers of the index block.
//          The first 11 of these pointers point to direct blocks.
//          The last pointer points to an indirect block.
//          In addition, each inode must include (1) the length of the
//          corresponding file, (2) the number of file (structure) table
//          entries that point to this inode, and (3) the flag to indicate
//          if it is unused (= 0), used(= 1), or in some other status
//          (= 2, 3, 4, ...). 16 inodes can be stored in one block.
//
//------------------------------------------------
public class Inode {
    private final static int iNodeSize = 32;       // fix to 32 bytes
    public final static int directSize = 11;      // # direct pointers
    private final static int byteSize = 512;
    private final static int blockSize = 16;
    private final static int intBlock = 4;
    private final static int shortBlock = 2;
    private final static int ERROR = -1;

    public int length;                             // file size in bytes
    public short count;                            // # file-table entries pointing to this
    public short flag;                             // 0 = unused, 1 = used, ...
    public short direct[] = new short[directSize]; // direct pointers
    public short indirect;                         // a indirect pointer

    Inode( ) {                                     // a default constructor
        length = 0;
        count = 0;
        flag = 1;
        for ( int i = 0; i < directSize; i++ )
            direct[i] = -1;
        indirect = -1;
    }

    Inode( short iNumber ) {
        int blockID = 1 + iNumber / blockSize;
        byte[] byteNode = new byte[byteSize];
        SysLib.rawread(blockID,byteNode);
        int offset = (iNumber % 16) * iNodeSize;
        length = SysLib.bytes2int(byteNode, offset);
        offset += intBlock;
        count = SysLib.bytes2short(byteNode, offset);
        offset += shortBlock;
        flag = SysLib.bytes2short(byteNode, offset);
        offset += shortBlock;
        for (int i = 0; i < directSize; i++){
            direct[i] = SysLib.bytes2short(byteNode, offset);
            offset += shortBlock;
        }
        indirect = SysLib.bytes2short(byteNode, offset);
        offset += shortBlock;
    }

    int toDisk( short iNumber ) {
        byte [] byteNode = new byte[iNodeSize];
        int offset = 0;
        SysLib.int2bytes(length, byteNode, offset);
        offset += intBlock;
        SysLib.short2bytes(count, byteNode, offset);
        offset += shortBlock;
        SysLib.short2bytes(flag, byteNode, offset);
        offset += shortBlock;
        for (int i = 0; i < directSize; i++){
            SysLib.short2bytes(direct[i], byteNode, offset);
            offset += shortBlock;
        }
        SysLib.short2bytes(indirect, byteNode, offset);
        offset += shortBlock;
        int blockID = 1 + iNumber / blockSize;
        byte[] newByteNode = new byte[byteSize];
        SysLib.rawread(blockID,newByteNode);
        offset = (iNumber % blockSize) * iNodeSize;
        System.arraycopy(byteNode, 0, newByteNode, offset, iNodeSize);
        SysLib.rawwrite(blockID,newByteNode);
        return 0;
    }

    int registerTargetBlock(int entryIndex, short offset){
        int target = entryIndex/byteSize;
        if (target < directSize) {
            if(direct[target] >= 0) {
                return -1;
            }
            if ((target > 0 ) && (direct[target - 1 ] == -1)){
                return -2;
            }
            direct[target] = offset;
            return 0;
        }
        if (indirect < 0){
            return -3;
        }
        else {
            byte[] byteNode = new byte[byteSize];
            SysLib.rawread(indirect,byteNode);
            int blockSpace = (target - directSize) * 2;
            if ( SysLib.bytes2short(byteNode, blockSpace) > 0){
                return ERROR;
            }
            else
            {
                SysLib.short2bytes(offset, byteNode, blockSpace);
                SysLib.rawwrite(indirect, byteNode);
            }
        }
        return 0;
    }

    boolean setIndexBlock(short blockID){
        for (int i = 0; i < directSize; i++){
            if (direct[i] == ERROR){
                return false;
            }
        }
        if (indirect != ERROR) return false;
        indirect = blockID;
        byte[ ] byteNode = new byte[byteSize];
        for(int i = 0; i < (byteSize/2); i++)
            SysLib.short2bytes((short) ERROR, byteNode, i * 2);
        SysLib.rawwrite(blockID, byteNode);
        return true;
    }

    int findTargetBlock(int offset){
        int target = offset / byteSize;
        if (target < directSize) return direct[target];
        if (indirect < 0) return -1;
        byte[] byteNode = new byte[byteSize];
        SysLib.rawread(indirect, byteNode);
        int blockSpace = (target - directSize) * 2;
        return SysLib.bytes2short(byteNode, blockSpace);
    }

    byte[] unregisterIndexBlock(){
        if (indirect >= 0)
        {
            byte[] byteNode = new byte[byteSize];
            SysLib.rawread(indirect, byteNode);
            indirect = -1;
            return byteNode;
        }
        else return null;
    }
}
