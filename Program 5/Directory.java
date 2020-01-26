// --------------------------- Directory.java--------------------------------
// Jessela Budiman, Cuong Vo, Quan Nghiem
// CSS 430 Section A
// Creation Date: 6/3/19
// Date of Last Modification: 6/8/19
// --------------------------------------------------------------------------
// Purpose:  The main purpose of directory is to store and manage files.
// creates two arrays fsize and fname
// fsize contain the sizes of these files in their respective locations
// size can be visualized as a simple list of numbers representing the
// different sizes of file stored int the fname array
// constructor given maxInumber for max stored files the fsize array can hold
// Fname contain the files that the directory is holding
// The Directory can read data from a byte array into the directory
// write from the directory back to the byte array
// --------------------------------------------------------------------------
public class Directory {
    private static int maxChars = 30; // max characters of each file name
    private static int MAX_BYTES = 60;
    private static int ALLOC_BYTE = 64;

    private int fsize[];        // each element stores a different file size.
    private int directorySize;        // size of directory
    private char fnames[][];    // each element stores a different file name.

    // -----------------------Directory(int)----------------------------------
    // constructor accepts int equal to the maxInumber, initialize attributes
    public Directory( int maxInumber )
    {
        fsize = new int[maxInumber];
        for ( int i = 0; i < maxInumber; i++ )
            fsize[i] = 0;
        directorySize = maxInumber;
        fnames = new char[maxInumber][maxChars];
        String root = "/";
        fsize[0] = root.length( );
        root.getChars( 0, fsize[0], fnames[0], 0 );
    }
    // -----------------------bytes2directory()--------------------------------
    // convert bytes to directory
    public void bytes2directory( byte data[] )
    {
        int offset = 0;
        for (int i = 0; i < directorySize; i++)
        {
            fsize[i] = SysLib.bytes2int(data, offset);
            offset += 4;
        }
        for (int i = 0; i < directorySize; i++)
        {
            String temp = new String(data, offset, MAX_BYTES);
            temp.getChars(0, fsize[i], fnames[i], 0);
            offset += MAX_BYTES;
        }
    }
    // -----------------------directory2bytes()--------------------------------
    // convert directory to bytes
    public byte[] directory2bytes( )
    {
        byte [] dir = new byte[ALLOC_BYTE * directorySize];
        int offset = 0;
        for (int i = 0; i < directorySize; i++)
        {
            SysLib.int2bytes(fsize[i], dir, offset);
            offset += 4;
        }
        for (int i = 0; i < directorySize; i++)
        {
            String temp = new String(fnames[i], 0, fsize[i]);
            byte [] bytes = temp.getBytes();
            System.arraycopy(bytes, 0, dir, offset, bytes.length);
            offset += MAX_BYTES;
        }
        return dir;
    }
    // -----------------------ialloc()-------------------------------------
    // allocate empty spot in directory
    public short ialloc( String filename )
    {
        for (short i = 0; i < directorySize; i++)
        {
            if (fsize[i] == 0)
            {
                int file = filename.length() > maxChars ?
                        maxChars : filename.length();
                fsize[i] = file;
                filename.getChars(0, fsize[i], fnames[i], 0);
                return i;
            }
        }
        return -1;
    }
    // -----------------------ifree()---------------------------------------
    // attempt to free the file passed in through node number
    public boolean ifree( short iNumber ) {
        if(iNumber < maxChars && fsize[iNumber] > 0){
            fsize[iNumber] = 0;
            return true;
        } else {
            return false;
        }
    }
    // -----------------------namei()----------------------------------------
    // get the node number of file name passed in
    public short namei( String filename )
    {
        for (short i = 0; i < directorySize; i++){
            if (filename.length() == fsize[i]){
                String temp = new String(fnames[i], 0, fsize[i]);
                if(filename.equals(temp)){
                    return i;
                }
            }
        }
        return -1;
    }
    // -----------------------printDir()--------------------------------------
    //print directory
    private void printDir(){
        for (int i = 0; i < directorySize; i++){
            SysLib.cout(i + ":  " + fsize[i] + " bytes - ");
            for (int j = 0; j < maxChars; j++){
                SysLib.cout(fnames[i][j] + " ");
            }
            SysLib.cout("\n");
        }
    }
}
