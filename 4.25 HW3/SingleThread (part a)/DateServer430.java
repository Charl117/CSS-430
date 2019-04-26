import java.net.*;
import java.io.*;

// This Server creates a new thread for each request (part a)
public class DateServer430 {

	public static void main(String[] args) {
		try {
			ServerSocket sock = new ServerSocket(8080);
			/* now listen for connections */
			while (true) {
				Socket client = sock.accept();
				// Create a thread for the client.
				// Invoke the serviceRequest class.
				new Thread(new serviceRequest(client)).start();
			}
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
	}

	// This private static class implements Runnable
	// Execute the request created in the thread when call
	// Invoking run() which print out the Thread Name and ID
	// Print out Date to the socket.
	// Close the socket. 
	private static class serviceRequest implements Runnable {
		private Socket client;

		public serviceRequest(Socket client) {
			this.client = client;
		}

		public void run() {
			try {
				// Will not have pool information
				System.out.println("Thread Name: " + Thread.currentThread().getName());
				System.out.println("Thread ID: " + Thread.currentThread().getId());
				PrintWriter pout = new PrintWriter(client.getOutputStream(), true);
				/* write the Date to the socket */
				pout.println(new java.util.Date().toString());
				/* close the socket and return */
				client.close();
				client = null;
			} catch (IOException ioe) {
				System.err.println(ioe);
			}
		}
	}
}
