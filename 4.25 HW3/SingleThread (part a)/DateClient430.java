import java.net.*;
import java.io.*;

public class DateClient430 {
	public static void main(String[] args) {
		// Run this 5 times for testing purpose. Unchanged from the sample code from Professor Pisan. 
		for (int i = 0; i < 5; i++) {
			try {
				Socket sock = new Socket("127.0.0.1", 8080);
				InputStream in = sock.getInputStream();
				BufferedReader bin = new BufferedReader(new InputStreamReader(in));
				String line;
				while ((line = bin.readLine()) != null) {
					System.out.println(line);
				}
				sock.close();
			} catch (IOException ie) {
			}
		}
	}
}
