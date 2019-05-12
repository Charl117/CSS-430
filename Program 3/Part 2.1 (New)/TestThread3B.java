// Author: Quan Trung Nghiem
// Date: 05/11/2019
// Test the runtime with disk threads

class TestThread3B extends Thread{
	
	//one disk block
	byte[] buffer = new byte[512]; 
		
	public void run(){
		for(int i = 0; i < 1000; i++){
			// write
			SysLib.rawwrite(i, buffer);	
			// read
			SysLib.rawread(i, buffer);  
		}
		SysLib.cout("disk finished...\n");
		SysLib.exit();	
	}
}
