/*
Julian Tompkins 500441681 Section 2
Yuriy Demchuk 500379813 Section 3
Illya Gordiyenko 500404719 Section 4
 
 */

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

// TODO: Auto-generated Javadoc
/**
 * The Class DirectoryServer.
 */
@SuppressWarnings("serial")
public class DirectoryServer extends JFrame {

	private JPanel contentPane;
	private JTextArea comArea;

	ArrayList<String> onlineUsers = new ArrayList<>();

	ArrayList<String> hostUsers = new ArrayList<>();
	ArrayList<String> cheathostUsers = new ArrayList<>();

	int port = 40060;
	int cliport = 40061;

	DatagramSocket datagramSocket;
	// = {"user1", "user2"};

	/*
	 * Creates the Frame
	 */

	public DirectoryServer() {
		setTitle("Directory Server");
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

		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startButtonActionPerformed(e);
			}
		});
		btnStart.setBounds(48, 221, 89, 23);
		contentPane.add(btnStart);

		JButton btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				stopButtonActionPerformed(arg0);
			}
		});
		btnStop.setBounds(248, 221, 89, 23);
		contentPane.add(btnStop);
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DirectoryServer frame = new DirectoryServer();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * The Class ServerStart. This one act only on UDP, for now all it does is print
	 * out the name of the new user, one at a time
	 */
	public class ServerStartWaitForPacket implements Runnable {

		public void run() {
			try {
				/*
				 * TODO This thing thought an exception cuz it tries it over and over again so
				 * what needs to be done is spDHlit this try and the one inside in to two
				 * separate classes this way you establish connection once but wait for
				 * receive(packet) many times
				 */
				datagramSocket = new DatagramSocket(port);
				// datagramSocket.setSoTimeout(10000); //time out was in one of the question...
				// thought it would be good to include

				while (true) {
					byte[] buffer = new byte[1024];
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

					try {
						datagramSocket.receive(packet);
						processPacket(buffer);
						buffer = null;
					} catch (SocketTimeoutException e) {
						datagramSocket.send(null);
						// continue;
					}
				} // end while
			} // end try
			catch (Exception ex) {
				// comArea.append(ex.toString());
				// comArea.append("Error making a connection. \n");
			} // end catch

		} // end go()

	}

	private void processPacket(byte[] buffer) {

		String clientMessage = "CL", serverMessage = "SE";
		String messageRecieved = new String(buffer);
		String[] dataTotal = messageRecieved.split(":");

		if (clientMessage.equals(dataTotal[0].trim())) {
			userUpdate(dataTotal);
		} else if (serverMessage.equals(dataTotal[0].trim())) {

		}
	}

	/**
	 * User add.
	 *
	 * @param dataTotal the data
	 */
	public void userUpdate(String[] dataTotal) {
		String online = "ON", offline = "OF", listAllUsers = "LI", hostingAServer = "HO", doneHostingAServer = "DH",
				joinServer = "JO", leaveServer = "LS";

		// dataTotal = dataTotal.split(":");

		comArea.append("Received:" + dataTotal[0] + ":" + dataTotal[1] + ":" + dataTotal[2] + "\n");

		if (online.equals(dataTotal[1].trim())) {
			onlineUsers.add(dataTotal[2].trim());
			// comArea.append("online users at check: " + onlineUsers.get(0) + "\n");

			comArea.append("Added User:" + dataTotal[2].trim() + "\n");

		} else if (offline.equals(dataTotal[1].trim())) {
			// TODO: check if it exists...might crash otherwise
			comArea.append("Deleting user:" + dataTotal[2].trim() + "\n");
			onlineUsers.remove(dataTotal[2].trim());
			// tellEveryoneAllOnlineUsers();
		} else if (listAllUsers.equals(dataTotal[1].trim())) {
			// TODO: send out all the users who are online
			tellEveryoneAllOnlineUsers();

		} else if (hostingAServer.equals(dataTotal[1].trim())) {
			// TODO: user started hosting a server
			// String temp = dataTotal[2];
			// System.out.println(dataTotal[2].split(":"));

			comArea.append(
					"Hosting: " + dataTotal[2] + ":" + dataTotal[3] + ":" + dataTotal[4] + ":" + dataTotal[5] + "\n");
			for (int i = 2; i < dataTotal.length; i++) {
				cheathostUsers.add(dataTotal[i]);
			}

			hostUsers.add(dataTotal[2].trim() + ":" + dataTotal[3].trim() + ":" + dataTotal[4].trim() + ":"
					+ dataTotal[5].trim());
			comArea.append("User Hosting Check:" + hostUsers.get(0) + "\n");
		} else if (doneHostingAServer.equals(dataTotal[1].trim())) {
			// TODO: user stopped hosting a server

			comArea.append("Done Hosting: " + dataTotal[2] + ":" + dataTotal[3] + ":" + dataTotal[4] + "\n");

			for (int i = 0; i < hostUsers.size(); i++) {

				// comArea.append(dataTotal[4].trim());

				if (hostUsers.get(i).contains(dataTotal[4].trim())) {
					String temp = hostUsers.get(i);

					// comArea.append("Yes it contains it");
					int rate = Integer.parseInt(
							temp.substring(temp.lastIndexOf(dataTotal[4].trim()) + dataTotal[4].trim().length() + 1));
					rate--;
					String rates = Integer.toString(rate);
					String temp2 = temp.substring(0, (temp.length() - 1));
					// System.out.println(temp2);
					hostUsers.set(i, temp2 + rates);
					// System.out.println(hostUsers.get(i));
				}

			}

			hostUsers.remove(dataTotal[2].trim());
			comArea.append("User Hosting Check:" + hostUsers.get(0) + "\n");

		} else if (joinServer.equals(dataTotal[1].trim())) {
			comArea.append("User Joined: " + dataTotal[2] + ":" + dataTotal[3] + ":" + dataTotal[4] + ":" + dataTotal[5]
					+ "\n");
			for (int i = 0; i < hostUsers.size(); i++) {

				// comArea.append(dataTotal[4].trim());

				if (hostUsers.get(i).contains(dataTotal[4].trim())) {
					String temp = hostUsers.get(i);
					// comArea.append("Yes it contains it");
					int rate = Integer.parseInt(
							temp.substring(temp.lastIndexOf(dataTotal[4].trim()) + dataTotal[4].trim().length() + 1));
					rate++;
					String rates = Integer.toString(rate);
					String temp2 = temp.substring(0, (temp.length() - 1));
					// System.out.println(temp2);
					hostUsers.set(i, temp2 + rates);
					// System.out.println(hostUsers.get(i));
				}
			}

			// TODO: user joined a server
		} else if (leaveServer.equals(dataTotal[1].trim())) {
			// TODO: user leaves a server
		} else {
			comArea.append(dataTotal[1].trim().compareTo(offline) + "\n");
			comArea.append("No Conditions were met. \n");
		}

	}

	private void tellEveryoneAllOnlineUsers() {
		// sends message to everyone connected to server

		String allUserInOneString = "DI:ON";
		String allHostInOneString = "DI:HO";

		try {
			// String[] tempArray = (String[]) onlineUsers.toArray();
			// comArea.append("WTF?!?");
			// comArea.append("Online users: " + onlineUsers.get(0) + "\n");

			for (int i = 0; i < onlineUsers.size(); i++) {
				allUserInOneString = allUserInOneString + ":" + onlineUsers.get(i);
			}

			if (!hostUsers.isEmpty()) {

				for (int i = 0; i < hostUsers.size(); i++) {
					allHostInOneString = allHostInOneString + ":" + hostUsers.get(i);
				}
			}
			comArea.append("Sending to Client: " + allUserInOneString + "\n");
			comArea.append("Sending to Client: " + allHostInOneString + "\n");

			String message = allUserInOneString + " " + allHostInOneString;

			byte[] buffer = message.getBytes();

			InetAddress receiverAddress = InetAddress.getLocalHost();

			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, receiverAddress, cliport);

			datagramSocket.send(packet);

			comArea.append("Message Sent to Client:" + allUserInOneString + "\n");

			comArea.append("Message Sent to Client:" + allHostInOneString + "\n");

			allUserInOneString = "DI:ON";

			allHostInOneString = "DI:HO";

		} catch (Exception ex) {
			comArea.append("Error telling everyone. \n");
		} // end catch

	}

	/**
	 * Tell everyone.
	 *
	 * @param message the message
	 */

	/**
	 * Start button action performed.
	 *
	 * @param evt the evt
	 */
	private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// TODO add your handling code here:
		Thread starter = new Thread(new ServerStartWaitForPacket());
		starter.start();
		comArea.append("Server started.\n");
	}

	/**
	 * Stop button action performed.
	 *
	 * @param evt the evt
	 */
	private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// TODO add your handling code here:
		// tellEveryone("Server:is stopping and all users will be
		// disconnected.\n:Chat");
		comArea.append("Directory Server does not really stop :) \n");
	}

}
