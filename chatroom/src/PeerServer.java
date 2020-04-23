
/*
 Julian Tompkins 500441681 Section 2
 Yuriy Demchuk 500379813 Section 3
Illya Gordiyenko 500404719 Section 4
 
 */

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JTextField;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class PeerServer extends JFrame {

	private JPanel contentPane;
	private JTextArea comArea;
	ArrayList<PrintWriter> clientMessages;
	ArrayList<String> onlineUsers;
	int port;
	private JTextField txtEnterPort;

	public PeerServer(int portt) {
		port = portt;
		setTitle("Peer2PeerServer");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 414, 199);
		contentPane.add(scrollPane);

		comArea = new JTextArea();
		scrollPane.setViewportView(comArea);

		txtEnterPort = new JTextField();
		txtEnterPort.setText("Hosting on Port:" + port);
		txtEnterPort.setBounds(48, 222, 289, 20);
		contentPane.add(txtEnterPort);
		txtEnterPort.setEditable(false);
		txtEnterPort.setColumns(10);

		Thread starter = new Thread(new ServerStart());
		starter.start();
		comArea.append("Server started. \n");

	}

	/*
	
	
	 */
	public class HandleClients implements Runnable {
		BufferedReader reader;
		Socket TCPsock;
		PrintWriter client;

		public HandleClients(Socket cSock, PrintWriter clientX) {
			// new inputStreamReader and then add it to a BufferedReader
			client = clientX;
			try {
				TCPsock = cSock;
				InputStreamReader readerX = new InputStreamReader(TCPsock.getInputStream());
				reader = new BufferedReader(readerX);
			} catch (Exception ex) {
				comArea.append("Cannot start StreamReader \n");
			}

		} // end HandleClients()

		public void run() {
			String message, connect = "Connect", disconnect = "Disconnect", chat = "Chat";
			String[] data;

			try {
				while ((message = reader.readLine()) != null) {

					comArea.append("Received: " + message + "\n");
					data = message.split(":");

					if (data[2].equals(connect)) {
						tellEveryone((data[0] + ":" + data[1] + ":" + chat));
						userAdd(data[0]);
					} else if (data[2].equals(disconnect)) {
						tellEveryone((data[0] + ":has disconnected." + ":" + chat));
						userRemove(data[0]);
						break;

					} else if (data[2].equals(chat)) {
						tellEveryone(message);
					} else {
						comArea.append("Message unreadable \n");
					}

				}
			} catch (Exception ex) {
				comArea.append("Lost a connection. \n");
				ex.printStackTrace();
				clientMessages.remove(client);
			}
		}
	} // end class HandleClients

	/*
	 * Server Start has: run() that set-up the sockets to run and listens to the
	 * threads
	 */
	public class ServerStart implements Runnable {
		@SuppressWarnings("resource")
		public void run() {
			clientMessages = new ArrayList<>();
			onlineUsers = new ArrayList<>();
			try {
				ServerSocket sSock = new ServerSocket(port);
				while (true) {

					Socket clientSock = sSock.accept();
					PrintWriter printer = new PrintWriter(clientSock.getOutputStream());
					clientMessages.add(printer);
					Thread listener = new Thread(new HandleClients(clientSock, printer));
					listener.start();

				}
			} catch (Exception ex) {
				comArea.append("Error making a connection. \n");
			}
		}
	}

	public void userAdd(String data) {
		String message, add = ": :Connect", done = "Server: :Done", name = data;

		onlineUsers.add(name);

		String[] tempList = new String[(onlineUsers.size())];
		onlineUsers.toArray(tempList);

		for (String token : tempList) {

			message = (token + add);
			tellEveryone(message);
		}
		tellEveryone(done);
	}

	public void userRemove(String data) {
		String message, add = ": :Connect", done = "Server: :Done", name = data;
		onlineUsers.remove(name);
		String[] tempList = new String[(onlineUsers.size())];
		onlineUsers.toArray(tempList);

		for (String token : tempList) {
			message = (token + add);
			tellEveryone(message);
		}
		tellEveryone(done);
	}

	public void tellEveryone(String message) {
		// sends message to everyone connected to server
		Iterator<PrintWriter> it = clientMessages.iterator();

		while (it.hasNext()) {
			try {
				PrintWriter printer = (PrintWriter) it.next();
				printer.println(message);
				comArea.append("Sending: " + message + "\n");
				printer.flush();
				comArea.setCaretPosition(comArea.getDocument().getLength());

			} // end try
			catch (Exception ex) {
				comArea.append("cannot send message \n");
			}
		}
	}

}
