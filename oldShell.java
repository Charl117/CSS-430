// Author: Quan Nghiem
// Description: read command lines and execute based on delimiters ";" for sequential execution and "&" for concurrent execution

// Assumptions: User will not input an exit command after any delimiter
//              Exit commands will always be the first command in the command line

class Shell extends Thread {
	// counter for the number of Shell
	private int threadCount;
	private String[] args;

	public Shell() {
		threadCount = 1;
		args = null;
	} // end of Shell()
    
    	//-----------------------run()------------------------------------------------------------------------
    	// Method run prompt the user for command input and run it using the helper method execute()
	public void run() {
        boolean nextShell = false;
		exitShell: for(;;) {
            		// Prompt the user to input command in
			SysLib.cout("Shell[" + threadCount + "]%");
			StringBuffer sb = new StringBuffer();
			SysLib.cin(sb);
			
            		// Read the string buffer to command and catch any exception
			try {
                		// Convert the String Buffer into String array
				args = SysLib.stringToArgs(sb.toString());
			}
			catch (Exception e) {
				SysLib.cerr("Invalid command, exiting shell.\n");
				break;
			}
			
			// Check for null input. Continue if true.
			if (args.length < 1) {
				continue;
			}
			
			// Check for exit command from the user
			if (args[0].toLowerCase().contentEquals("exit")) {
				SysLib.cout("Exiting shell\n");
				break exitShell;
			}
			
			// Execute the command and check for delimiter with the helper function execute()
			else {
				nextShell = execute(sb.toString());
			}
			
			// Check if there is a "&" at the end
			if (nextShell) {
                		threadCount --;
			}
			
			// After finishing the thread above, increase the threadCount, and start a new thread
			threadCount ++;
		} // end of for loop
		
		SysLib.sync();
		SysLib.exit(); // Exit current Shell. 
	} // end of method run()
    
    	//--------------------------execute(String)---------------------------------------------------------
    	// Method takes in a parameter of string input and split it into String array using the string split
    	// method based on the delimiter "&" and ";"
    	// Parameter: String input. Accept a string and split it based on delimiters
	private boolean execute(String input) {
		// Split the string into array based on the ";" delimiter
		String[] temp = input.split(";");
		
		// This string will contains the sequential command 
		String seq;

		// Iterate through the temp array containing splitted commands
		for (int i = 0; i < temp.length; i++) {
			seq = temp[i];
			
			// Check for empty command. Trim all the white space. Go to next iteration if empty
			if (seq.length() < 1 || seq.trim().length() < 1) {
				continue;
			}
            
            		// Check for delimiter "&" in the seq string. If found, split the string based on delimiter "&"
            		// Proceed to do concurrent commands
			if (seq.contains("&")) {
                		// Counter for the number of concurrently running thread
				int count = 0;
				
				// Enhanced for loop creates similar String array to put the splitted string seq in
				String[] temp2 = seq.split("&");
				
				// This string will contains the concurrent command 
				String con;
				
				// Iterate through the temp2 array containing splitted commands
				for (int z = 0; z < temp2.length; z++ ) {
                    			con = temp2[z];
                    
					// Check for empty command. Trim all the white space. Go to next iteration if empty
					if (con.length() < 1 || con.trim().length() < 1) {
						continue;
					}
                    
                    			// Create a child thread
                    			// Check for failure to create a child thread
					if (SysLib.exec(SysLib.stringToArgs(con)) < 0) {
						SysLib.cerr("Failed to execute command: " + con + "\n");
					} 
					
					// Created succesfully and increase the number of thread running
					else {
						count++;
					}
				}
                
                		// If there is the delimiter "&" at the end of the user input
                		// Execute any child thread then wait for the user input
				if (i == (temp.length - 1) && (seq.lastIndexOf("&") == seq.length() - 1)) {
                    			SysLib.join();
					return true;
				}
                
               			// Wait for all the child threads to finish
				else {
					for (int j = 0; j < count; j++) {
						SysLib.join();
					}
				}
			}

			// Execute the sequential commands
			else {
				// Create a child thread and pidSeq holds the thread ID
				int pidSeq = SysLib.exec(SysLib.stringToArgs(seq));
				
				// Check if thread ID is -1. If so, failed to create the thread
				if (pidSeq < 0) {
					SysLib.cerr("Failed to execute command: " + seq + "\n");
				} 
				
				// Wait for the current process to complete
				else {
					while(SysLib.join() != pidSeq) {
						continue;
					}
				}
			}
		} // End of for loop
		return false;
	} // End of method execute()

} // End of Shell class
