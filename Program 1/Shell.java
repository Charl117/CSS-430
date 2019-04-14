// Author: Quan Nghiem
// Description: read command lines and execute based on delimiters ";" for sequential execution and "&" for concurrent execution

// Assumptions: User will not input an exit command after any delimiter
//              Exit commands will always be the first command in the command line

class Shell extends Thread {
	private int threadCount;
	private String[] args;

	public Shell() {
		// counter for the number of Shell
		threadCount = 1;
		// null argument starter
		args = null;
	} // end of Shell()

	// -----------------------run()---------------------------------
	// Method run prompt the user for command input and run it
	// using the helper
	// method execute()
	public void run() {
		for (;;) {
			// Prompt the user to input command in
			SysLib.cout("shell[" + threadCount + "]%");
			StringBuffer sb = new StringBuffer();
			SysLib.cin(sb);
			String input = sb.toString().trim();

			// Convert string input into string array
			args = SysLib.stringToArgs(input);

			// Check for null input. Continue if true.
			if (args.length < 1) {
				continue;
			}

			// Check for exit command from the user
			if (args[0].toLowerCase().contentEquals("exit")) {
				SysLib.cout("Exiting shell\n");
				// Sync then exit shell completely
				break;
			}

			// Execute the command and check for delimiter with the helper
			// function execute()
			else {
				execute(input);
			}

			// After finishing the thread above, increase the threadCount, and
			// start a new thread
			threadCount++;

		} // end of for loop

		SysLib.sync();
		SysLib.exit(); // Exit current Shell.
	} // end of method run()

	// -----------------------execute()--------------------------------
	// Method execute() takes in a String input
	// Split the based on the delimiter ";" into array of string
	// Call the checkConcurrent for delimiter "&" in each element
	// of the string array
	// Parameter: String input
	private void execute(String input) {
		for (String sequential : input.split(";")) {

			// Check for empty command. Go to next iteration if empty
			if (sequential.length() < 1) {
				continue;
			}

			// Check for concurrent delimiter "&"
			boolean isConcurrent = checkConcurrent(sequential);

			// Execute the sequential commands if there is no delimiter "&"
			if (!isConcurrent) {
				// Create a child thread and pid holds the thread ID
				int pid = SysLib.exec(SysLib.stringToArgs(sequential));

				// Check if thread ID is -1. If so, failed to create the thread
				if (pid < 0) {
					SysLib.cerr("Failed to execute command: " + sequential + "\n");
				}

				// Wait for the current process to complete
				else {
					while (SysLib.join() != pid) {
						continue;
					}
				}
			}
		} // end of for loop
	} // end of method execute()

	// -----------------------checkConcurrent()--------------------------------
	// Method checkConcurrent() takes in a String input
	// Check if String input contains "&"
	// If not then return false. Else create and run thread concurrently
	// and return true
	// Split the based on the delimiter "&" into array of string
	// Parameter: String input
	// Return boolean true or false
	private boolean checkConcurrent(String input) {
		if (input.contains("&")) {
			// Counter for the number of concurrently running thread
			int count = 0;
			
			// Enhanced for loop creates String to put the
			// splitted string input in
			for (String concurrent : input.split("&")) {
				// Check for empty command. Go to next iteration if empty
				if (concurrent.length() < 1) {
					continue;
				}

				// Create a child thread
				// Failed to execute if thread ID is -1
				if (SysLib.exec(SysLib.stringToArgs(concurrent)) < 0) {
					SysLib.cerr("Failed to execute command: " + concurrent + "\n");
				}

				// Created succesfully. Increased child thread count by 1
				else {
					count++;
				}
			}

			// Wait for all the child threads to finish
			for (int i = 0; i < count; i++) {
				SysLib.join();
			}

			// There is delimiter "&"
			return true;
		}

		// No delimiter "&"
		else {
			return false;
		}
	}
}
