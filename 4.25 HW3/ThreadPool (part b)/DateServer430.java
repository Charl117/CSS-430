import java.net.*;
import java.io.*;
import java.util.concurrent.*;

//This Server uses a Java-based thread-pool for each request (part b)
public class DateServer430 {

	public static void main(String[] args) {
		// Create a threadpool with three threads in it.
		ExecutorService executor = Executors.newFixedThreadPool(3);
		try {
			ServerSocket sock = new ServerSocket(8080);
			/* now listen for connections */
			while (!Thread.currentThread().isInterrupted()) {
				Socket client = sock.accept();
				// submit the request to a thread in the threadpool.
				executor.submit(new serviceRequest(client));
			}
		} catch (IOException ioe) {
			System.err.println(ioe);
		} finally {
			// shutdown the thread-pool when we are done
			executor.shutdown();
		}
	}

	// This private static class implements Runnable
	// Execute the request created in the thread when call
	// Invoking run() which print out the Thread Name and ID. Different with part a) is that the thread name will have the pool it orginated from
	// Ex: Thread 1 - Pool 1
	// Print out Date to the socket.
	// Close the socket.
	private static class serviceRequest implements Runnable {
		private Socket client;

		public serviceRequest(Socket client) {
			this.client = client;
		}

		public void run() {
			try {
				// Print out the thread. Will have the pool information, that's where it's
				// different from the single thread.
				System.out.println("Thread Name: " + Thread.currentThread().getName());
				System.out.println("Thread ID: " + Thread.currentThread().getId());
				System.out.println(" ");
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
