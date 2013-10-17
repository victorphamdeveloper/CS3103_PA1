/*
   Simple command-line Jabber client (skeleton code).

   To compile:
   $ javac   -classpath .;commons-codec-1.8.jar   *.java

   To execute:
   $ java    -classpath .;commons-codec-1.8.jar \
      JabberMain  jabber_id  password  server_name  server_port 
                  [more Jabber ID details] ... 
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class containing the {@link #main(String[])} method.
 * 
 * <p>
 * This class creates an XMPP connection to the server specified in the
 * command-line arguments, using the {@link XmppConnection} class.
 */
public class JabberMain {
	public static JabberID jid;
	public static Vector<String> currentMessageLog;
	public static BufferedReader reader;
	public static BufferedWriter writer;
	public static LinkedList<String> unsendedMessages = new LinkedList<String>();

	/** Main method that starts off everything. */
	public static void main(String[] args) {
		InputStreamReader isr;
		BufferedReader br = null;
		isr = new InputStreamReader(System.in);
		br = new BufferedReader(isr);

		// Check if number of args are ok (multiple of 4)
		if (args.length < 4 || args.length % 4 != 0) {
			System.err.println("Usage: java JabberMain "
					+ "jabber_id password server_name server_port "
					+ "[more Jabber ID details] ... ");
			return;
		}
		initialize(args);
		while (true) {

			try {
				String curr;
				curr = br.readLine();
				if (curr.equals("@roster")) {
					connection.getRosterList();
				} else if (curr.startsWith("@chat")) {
					receiver = curr.substring(6);
					currentMessageLog = new Vector<String>();
				} else if (curr.startsWith("@end")) {
					backupMessageToServer();
				} else {
					if (receiver == null)
						System.out
								.println("Please specify a buddy to chat with");
					else {
						connection.sendMessage(receiver, curr);
						System.out.println(jid.getUsername() + ": " + curr);
						currentMessageLog.add(jid.getUsername() + ": " + curr);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Attempt to reconnect");
				try {
					connection.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				if (!reconnect())
					break;
			}
		}
		System.out.println("Good bye");

	}

	private static void initialize(String[] args) {
		try {
			System.out.println();

			// Get the list of Jabber IDs
			List<JabberID> jidList = getJidList(args);

			// In this assignment, handling one server is sufficient
			// Create an XMPP connection
			jid = jidList.get(0);
			connection = new XmppConnection(jid);

			// Connect to the Jabber server
			connection.connect();
			connection.sendPresence();

			Thread t = new Thread(new MessageListener());
			t.start();
			Thread p = new Thread(new PingManager());
			p.start();

		} catch (IOException e) {
			System.err.println("Attempt to reconnect:");
			reconnect();
			// e.printStackTrace();

			// If there is any exception, it gets thrown up here to main()
			// (or the run() method of your thread, if you use a thread).
			// In Task 2, you need to re-connect to the server, instead of
			// simply quitting.
		}
		System.out.println("============================================");
	}

	public static void sendUnsendedMessage() {
		System.out.println("Send unsended messages now !!!!!!!");
		while (!unsendedMessages.isEmpty()) {
			String message = unsendedMessages.poll();
			try {
				connection.sendMessage(receiver, message);
				System.out.println(jid.getUsername() + ": " + message);
				currentMessageLog.add(jid.getUsername() + ": " + message);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private static void backupMessageToServer() throws IOException,
			UnsupportedEncodingException {
		receiver = null;
		Socket socket = new Socket();
		String serverName = "localhost";
		int serverPort = 8189;
		// Attempt to connect to the server
		System.out.println("Connecting to " + serverName + " at port "
				+ serverPort);
		socket.connect(new InetSocketAddress(serverName, serverPort), 5000);
		System.out.println("Connected to " + serverName + " at port "
				+ serverPort);
		reader = new BufferedReader(new InputStreamReader(
				socket.getInputStream(), "UTF-8"));
		writer = new BufferedWriter(new OutputStreamWriter(
				socket.getOutputStream(), "UTF-8"));
		writer.write("fileName:" + jid.getUsername() + "_" + getTimeStamp());
		System.out.println("fileName:" + jid.getUsername() + "_"
				+ getTimeStamp());
		writer.newLine();
		for (String message : currentMessageLog) {
			writer.write(message);
			writer.newLine();
		}
		writer.write("END");
		writer.newLine();
		writer.flush();
		socket.close();
	}

	public static void receiveMessage(String person, String message) {
		if (person.equals(receiver)) {
			currentMessageLog.add(person + ": " + message);
		}
		System.out.println(person + ": " + message);
	}

	private static String getTimeStamp() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	private static int randomInt(int k) {
		int e = (int) (Math.pow(2, k) - 1); // Binary Exponentiation
		Random r = new Random();
		int res = r.nextInt(e) * 1000;
		return res;
	}

	public static boolean reconnect() {
		boolean reconnected = false;
		int k = 0, r = 0, n;
		for (n = 1; !reconnected && n <= 16; n++) {
			k = Math.min(n, 10); // Truncation
			r = randomInt(k);
			System.out.println(n + " attempt to reconnect in " + r / 1000
					+ " seconds");
			try {
				Thread.sleep(r);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			reconnected = connect();
			System.out.println("Reconnect " + reconnected);
			if (reconnected) {
				sendUnsendedMessage();
			}
		}
		if (n > 16)
			return false;
		return true;
	}

	public static boolean connect() {
		connection = new XmppConnection(jid);

		// Connect to the Jabber server
		try {
			connection.connect();
			connection.sendPresence();
			Thread t = new Thread(new MessageListener());
			t.start();
			Thread p = new Thread(new PingManager());
			p.start();
			return true;
		} catch (IOException e) {
			return false;
		}

	}

	/** Helper method that gets the list of Jabber IDs specified as args. */
	private static List<JabberID> getJidList(String[] args) {

		// Get the list of Jabber IDs
		List<JabberID> jidList = new ArrayList<JabberID>();
		for (int i = 0; i < args.length; i += 4) {

			// Try to convert the port number to int
			int port;
			try {
				port = Integer.parseInt(args[i + 3]);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(
						"Invalid port: Not a number", e);
			}

			// Add the Jabber ID to the list
			jidList.add(new JabberID(args[i], // Jabber ID: username@domain
					args[i + 1], // Password
					args[i + 2], // Server name
					port)); // Server port
		}

		return jidList;
	}

	/** XMPP connection. */
	public static XmppConnection connection = null;
	private static String receiver = null;
}
