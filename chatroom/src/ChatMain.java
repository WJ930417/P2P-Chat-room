/*
 Julian Tompkins 500441681 Section 2
 Yuriy Demchuk 500379813 Section 3
Illya Gordiyenko 500404719 Section 4
 
 */

import javax.swing.JPanel;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JLabel;

/*
 * To change this template, choose Tools : Templates
 * and open the template in the editor.
 */

/*
 * Chat.java
 *
 * Created on Apr 14, 2011, 10:23:37 AM
 */

import java.net.*;
import java.io.*;
import java.util.*;

import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class ChatMain extends javax.swing.JFrame {

	InetAddress ip;
	String username;
	String serverIP;
	int dirport = 40060;
	int cliport = 40061;
	int hostingport;
	int rating = 1;
	Socket sock;
	BufferedReader reader;
	PrintWriter writer;
	ArrayList<String> userList = new ArrayList<>();
	ArrayList<String> preUserList = new ArrayList<>();

	ArrayList<String> allOnlineUsers = new ArrayList<>();
	ArrayList<String> allHostUsers = new ArrayList<>();

	Boolean isConnectedToChatServer = false;
	Boolean isConnectedToDirServer = false;

	DatagramSocket mainSocket;
	PeerServer p2p;

	/**
	 * Creates new form Chat.
	 */
	public ChatMain() {
		initRoomToJoin();
	}

	/**
	 * The main method.
	 *
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {

				new ChatMain().setVisible(true);

			}
		});
	}

	/** The chat text area. */
	private JTextArea chatTextArea;
	private JTextArea textAllUsersOnline;
	private JTextArea hostTextArea;
	/** The input text area. */
	private JTextArea inputTextArea;

	private JTextArea usersList;
	private JTextField usernameField;
	private JPanel contentPane;
	private JButton btnSend;
	private JButton btnDisconnect;
	private JTextField hostnameField;
	private JTextField usernameFieldOnline;
	private JTextField PortField;

	/**
	 * The Class IncomingReader.
	 */
	public class IncomingReader implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			String[] data;
			String stream, done = "Done", connect = "Connect", disconnect = "Disconnect", chat = "Chat";
			try {
				while ((stream = reader.readLine()) != null) {
					data = stream.split(":");
					if (data[2].equals(chat)) {
						chatTextArea.append(data[0] + ": " + data[1] + "\n");
						chatTextArea.setCaretPosition(chatTextArea.getDocument().getLength());
					} else if (data[2].equals(connect)) {

						chatTextArea.removeAll();
						userAdd(data[0]);

					} else if (data[2].equals(disconnect)) {

						userRemove(data[0]);

					} else if (data[2].equals(done)) {

						usersList.setText("");
						writeUsers();
						preUserList.clear();
						for (int i = 0; i < userList.size(); i++) {
							preUserList.add(userList.get(i));
						}

						userList.clear();
					}
				}
				chatTextArea.append("Host failed!\n");
				chatTextArea.append(preUserList.get(1) + " is the new host!!\n");

				// start new host server and add all alive clients
				changeHost();

			} catch (Exception ex) {

			}
		}
	}

	public void changeHost() {
		username = usernameFieldOnline.getText();
		try {
			DatagramSocket datagramSocket = new DatagramSocket();
			InetAddress receiverAddress = InetAddress.getLocalHost();

			String messageToSend = "CL:LI:" + username;

			byte[] buffer = messageToSend.getBytes();

			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, receiverAddress, dirport);
			datagramSocket.send(packet);

			datagramSocket.close();

		} catch (Exception ex) {

		}
		ListenThreadDir();

		if (username.equals(preUserList.get(1))) {
			p2p = new PeerServer(hostingport);
			p2p.setVisible(true);

			try {
				DatagramSocket datagramSocket = new DatagramSocket();
				String messageToSend = "CL:FH:" + username + ":" + serverIP + ":" + hostingport + ":" + rating;

				byte[] buffer = messageToSend.getBytes();
				InetAddress receiverAddress = InetAddress.getLocalHost();

				DatagramPacket packet = new DatagramPacket(buffer, buffer.length, receiverAddress, dirport);
				datagramSocket.send(packet);
				datagramSocket.close();

			} catch (Exception ex) {
				textAllUsersOnline.append("Cannot Connect! Try Again. \n");
			}
		}

		try {
			if (!username.equals(preUserList.get(1))) {
				Thread.sleep(100);
			}
			sock = new Socket(serverIP, hostingport);
			InputStreamReader streamreader = new InputStreamReader(sock.getInputStream());
			reader = new BufferedReader(streamreader);
			writer = new PrintWriter(sock.getOutputStream());
			writer.println(username + ":has connected.:Connect"); // Displays that user has connected
			writer.flush(); // flushes the buffer
			isConnectedToChatServer = true; // Used to see if the client is connected.
		} catch (Exception ex) {
			chatTextArea.append("Cannot Connect! Try Again. \n");
			usernameField.setEditable(true);
		}
		ListenThreadSer();
	}

	/**
	 * The Class Incoming directory Reader.
	 */

	public class IncomingDirReader implements Runnable {

		public void run() {
			try {

				mainSocket = new DatagramSocket(cliport);

				while (true) {
					byte[] receiveData = new byte[1024];
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					try {
						mainSocket.receive(receivePacket);
						// System.out.println("Did this occur?");
						processPacket(receiveData);
						receiveData = null;
						mainSocket.close();
					} catch (SocketTimeoutException e) {
						mainSocket.send(null);
						// continue;
					}
					// receivePacket = null;
				}

			} catch (Exception ex) {
				// textAllUsersOnline.append(ex.toString());
			}
		}
	}

	/**
	 * Listen thread from the directory server.
	 */
	public void ListenThreadDir() {
		Thread IncomingDirReader = new Thread(new IncomingDirReader());
		IncomingDirReader.start();
	}

	private void processPacket(byte[] buffer) {
		String messageReceived = new String(buffer);
		String[] messages = messageReceived.split(" ");

		String directoryMessage = "DI", serverMessage = "SE";

		for (String m : messages) {
			if (m.equals("")) {
				continue;
			}
			// System.out.println(m);
			String[] dataTotal = m.split(":");
			// System.out.println(dataTotal[0].trim());
			if (directoryMessage.equals(dataTotal[0].trim())) {
				printOnlineUsers(dataTotal);
			} else if (serverMessage.equals(dataTotal[0].trim())) {

			} else {
				// comArea.append("No Conditions were met, the message was not from the Client
				// or Server. \n");
			}
		}

	}

	private void printOnlineUsers(String[] dataTotal) {
		String online = "ON", offline = "OF", listAllUsers = "LI", hostingAServer = "HO", doneHostingAServer = "DH",
				joinServer = "JO", leaveServer = "LS";

		if (online.equals(dataTotal[1].trim())) {
			textAllUsersOnline.setText(null);
			for (int i = 2; i < dataTotal.length; i++) {

				allOnlineUsers.add(dataTotal[i]);
				textAllUsersOnline.append(dataTotal[i].trim() + "\n");
			}

		} else if (offline.equals(dataTotal[1].trim())) {
			// TODO: check if it exists...might crash otherwise
			// comAreaOnlineUsers.append("Deleting user:" + dataTotal[2].trim() + "\n");
			// onlineUsers.remove(dataTotal[2].trim());
			// comArea.append(onlineUsers.get(0));
		} else if (listAllUsers.equals(dataTotal[1].trim())) {
			// TODO: send out all the users who are online
		} else if (hostingAServer.equals(dataTotal[1].trim())) {
			hostTextArea.setText(null);
			for (int i = 2; i < dataTotal.length; i++) {
				allHostUsers.add(dataTotal[i]);
				if (i == 2 || i % 4 == 2) {
					hostTextArea.append(dataTotal[i].trim() + " R:" + dataTotal[i + 3].trim() + "\n");
				}
			}
		} else if (doneHostingAServer.equals(dataTotal[1].trim())) {
			// TODO: user stopped hosting a server
		} else if (joinServer.equals(dataTotal[1].trim())) {
			// TODO: user joined a server
		} else if (leaveServer.equals(dataTotal[1].trim())) {
			// TODO: user leaves a server
		} else {
			// comArea.append(dataTotal[1].trim().compareTo(offline) + "\n");
			textAllUsersOnline.append("No Conditions were met. \n");
		}

	}

	/**
	 * Listen thread for the chat server.
	 */
	public void ListenThreadSer() {
		Thread IncomingReader = new Thread(new IncomingReader());
		IncomingReader.start();
	}

	/**
	 * User add.
	 *
	 * @param data the data
	 */
	public void userAdd(String data) {
		userList.add(data);

	}

	/**
	 * User remove.
	 *
	 * @param data the data
	 */
	public void userRemove(String data) {
		chatTextArea.append(data + " has disConnectedToChatServer.\n");

	}

	/**
	 * Write users.
	 */
	public void writeUsers() {
		String[] tempList = new String[(userList.size())];
		userList.toArray(tempList);
		for (String token : tempList) {

			usersList.append(token + "\n");

		}

	}

	/**
	 * Send disconnect.
	 */
	public void sendDisconnect() {

		String bye = (username + ": :Disconnect");
		try {
			writer.println(bye); // Sends server the disconnect signal.
			writer.flush(); // flushes the buffer
		} catch (Exception e) {
			chatTextArea.append("Could not send Disconnect message.\n");
		}

	}

	/**
	 * Disconnect.
	 */
	public void Disconnect() {

		try {
			chatTextArea.append("DisConnectedToChatServer.\n");
			sock.close();
		} catch (Exception ex) {
			chatTextArea.append("Failed to disconnect. \n");
		}
		isConnectedToChatServer = false;

		usersList.setText("");

	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// @SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initChatClient() {

		setTitle("Chat Client");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 774, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setBounds(10, 11, 85, 14);
		contentPane.add(lblUsername);

		usernameField = new JTextField(username);
		usernameField.setEditable(false);
		usernameField.setBounds(75, 8, 86, 20);
		contentPane.add(usernameField);
		usernameField.setColumns(10);

		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.setBounds(332, 7, 99, 23);
		contentPane.add(btnDisconnect);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 205, 322, 46);
		contentPane.add(scrollPane);

		inputTextArea = new JTextArea();
		scrollPane.setViewportView(inputTextArea);

		btnSend = new JButton("Send");
		btnSend.setBounds(342, 205, 89, 46);
		contentPane.add(btnSend);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 39, 421, 155);
		contentPane.add(scrollPane_1);

		chatTextArea = new JTextArea();
		scrollPane_1.setViewportView(chatTextArea);

		chatTextArea.setColumns(20);
		chatTextArea.setEditable(false);
		chatTextArea.setFont(new java.awt.Font("Times New Roman", 0, 12)); // NOI18N
		chatTextArea.setLineWrap(true);
		chatTextArea.setRows(5);

		JLabel lblOnlineUsers = new JLabel("Online Users");
		lblOnlineUsers.setBounds(551, 11, 88, 14);
		contentPane.add(lblOnlineUsers);

		usersList = new JTextArea();
		usersList.setBounds(455, 39, 288, 212);
		contentPane.add(usersList);

		btnDisconnect.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				disconnectButtonActionPerformed(evt);
			}
		});

		btnSend.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				sendButtonActionPerformed(evt);
			}
		});

	}// </editor-fold>

	/**
	 * Inits the room to join.
	 */
	public void initRoomToJoin() {

		setTitle("Chat User");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 741, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		/* Go online and offline buttons - line 1 */
		JButton btnGoOnline = new JButton("Go Online:");
		btnGoOnline.setBounds(0, 10, 125, 30);
		contentPane.add(btnGoOnline);

		usernameFieldOnline = new JTextField();
		if (isConnectedToDirServer == true) {
			usernameFieldOnline.setText(username);
		}
		usernameFieldOnline.setText("Enter Username");
		usernameFieldOnline.setColumns(10);
		usernameFieldOnline.setBounds(125, 15, 110, 20);
		contentPane.add(usernameFieldOnline);

		JButton btnGoOffline = new JButton("Go Offline:");
		btnGoOffline.setBounds(245, 10, 125, 30);
		contentPane.add(btnGoOffline);

		/* Join fields - line 2 */
		JButton btnJoin = new JButton("Join:");
		btnJoin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				joinButtonActionPerformed(arg0);

			}
		});
		btnJoin.setBounds(0, 50, 125, 30);
		contentPane.add(btnJoin);

		hostnameField = new JTextField();
		hostnameField.setText("Enter HostName");
		hostnameField.setBounds(125, 55, 110, 20);
		contentPane.add(hostnameField);
		hostnameField.setColumns(10);

		/*
		 * Host Users - line 4 This one is temporary for now, since all it does is join
		 * the same server
		 */
		JButton btnHost = new JButton("Host");
		btnHost.setBounds(0, 198, 123, 23);
		contentPane.add(btnHost);

		/* Server Hosts online */
		JLabel lblHostUsers = new JLabel("Host Servers");
		lblHostUsers.setBounds(425, 38, 84, 14);
		contentPane.add(lblHostUsers);

		/* Users online */
		JLabel lblUsersOnline = new JLabel("Users Online");
		lblUsersOnline.setBounds(555, 38, 84, 14);
		contentPane.add(lblUsersOnline);

		PortField = new JTextField();
		PortField.setText("Enter Port #");
		PortField.setColumns(10);
		PortField.setBounds(125, 199, 110, 20);
		contentPane.add(PortField);

		textAllUsersOnline = new JTextArea();
		textAllUsersOnline.setBounds(534, 53, 110, 166);
		contentPane.add(textAllUsersOnline);

		JButton btnQueryForPeers = new JButton("Query For Peers");
		btnQueryForPeers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				queryButtonActionPerformed(arg0);
			}
		});
		btnQueryForPeers.setBounds(454, 14, 149, 23);
		contentPane.add(btnQueryForPeers);

		hostTextArea = new JTextArea();
		hostTextArea.setBounds(404, 53, 105, 166);
		contentPane.add(hostTextArea);

		btnGoOnline.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				goOnlineButtonActionPerformed(evt);
			}
		});

		btnGoOffline.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				goOfflineButtonActionPerformed(evt);
			}
		});

		btnHost.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				hostButtonActionPerformed(evt);
			}
		});
	}

	private void queryButtonActionPerformed(java.awt.event.ActionEvent evt) {
		username = usernameFieldOnline.getText();
		try {
			DatagramSocket datagramSocket = new DatagramSocket();
			InetAddress receiverAddress = InetAddress.getLocalHost();

			String messageToSend = "CL:LI:" + username;

			byte[] buffer = messageToSend.getBytes();

			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, receiverAddress, dirport);
			datagramSocket.send(packet);

			datagramSocket.close();

		} catch (Exception ex) {

		}
		ListenThreadDir();
	}

	/*
	 * This one is using the UDP since it acts directly with the Directory Server
	 */

	private void joinButtonActionPerformed(java.awt.event.ActionEvent evt) {
		username = usernameFieldOnline.getText();
		String hostname = hostnameField.getText();
		String portstr = "";
		String tempIP = "";
		for (int i = 0; i < allHostUsers.size(); i++) {

			if (hostname.equals(allHostUsers.get(i))) {

				portstr = allHostUsers.get(i + 2).trim();
				tempIP = allHostUsers.get(i + 1).trim();
			}

		}

		int portX = Integer.parseInt(portstr);
		hostingport = portX;

		if (isConnectedToChatServer == false && isConnectedToDirServer == true) {
			try {
				rating = rating + 1;
				DatagramSocket datagramSocket = new DatagramSocket();

				InetAddress receiverAddress = InetAddress.getLocalHost();
				serverIP = receiverAddress.getHostAddress();
				// System.out.println(serverIP);
				String messageToSend = "CL:JO:" + username + ":" + tempIP + ":" + portX + ":" + rating;
				// System.out.println("Sending message" + messageToSend);
				byte[] buffer = messageToSend.getBytes();

				DatagramPacket packet = new DatagramPacket(buffer, buffer.length, receiverAddress, dirport);
				datagramSocket.send(packet);
				datagramSocket.close();

			} catch (Exception ex) {

			}
		}

		initChatClient();
		if (isConnectedToChatServer == false && isConnectedToDirServer == true && portX != 0) {
			username = usernameField.getText();
			usernameField.setEditable(false);
			try {
				sock = new Socket(tempIP, portX);
				InputStreamReader streamreader = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(streamreader);
				writer = new PrintWriter(sock.getOutputStream());
				writer.println(username + ":has connected.:Connect"); // Displays that user has connected
				writer.flush(); // flushes the buffer
				isConnectedToChatServer = true; // Used to see if the client is connected.
			} catch (Exception ex) {
				chatTextArea.append("Cannot Connect! Try Again. \n");
				usernameField.setEditable(true);
			}

			ListenThreadSer();
		}
	}

	private void goOfflineButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// ListenThreadDir();
		// if (isConnectedToDirServer == false) {
		username = usernameFieldOnline.getText();

		try {
			DatagramSocket datagramSocket = new DatagramSocket();
			String messageToSend = "CL:OF:" + username;

			byte[] buffer = messageToSend.getBytes();
			InetAddress receiverAddress = InetAddress.getLocalHost();

			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, receiverAddress, dirport);
			datagramSocket.send(packet);

			datagramSocket.close();
			isConnectedToDirServer = false;
			usernameFieldOnline.setEditable(true);
			// writer.println(messageToSend);

		} catch (Exception ex) {
			textAllUsersOnline.append("Cannot Connect! Try Again. \n");
			usernameFieldOnline.setEditable(true);
		}
	}

	/*
	 * This one is using the UDP since it acts directly with the Directory Server
	 */
	private void goOnlineButtonActionPerformed(java.awt.event.ActionEvent evt) {

		if (isConnectedToDirServer == false) {
			username = usernameFieldOnline.getText();
			usernameFieldOnline.setEditable(false);
			// comAreaOnlineUsers.append("Hello There!");
			try {
				DatagramSocket datagramSocket = new DatagramSocket();
				String messageToSend = "CL:ON:" + username;

				byte[] buffer = messageToSend.getBytes();
				// byte[] receiveData = new byte[1024];
				InetAddress receiverAddress = InetAddress.getLocalHost();

				DatagramPacket packet = new DatagramPacket(buffer, buffer.length, receiverAddress, dirport);
				datagramSocket.send(packet);
				/*
				 * (DatagramPacket receivePacket = new DatagramPacket(receiveData,
				 * receiveData.length); datagramSocket.receive(receivePacket); String
				 * modifiedSentence = new String(receivePacket.getData());
				 * System.out.println("FROM SERVER:" + modifiedSentence);
				 */
				datagramSocket.close();
				isConnectedToDirServer = true;

				// writer.println(messageToSend);

			} catch (Exception ex) {
				textAllUsersOnline.append("Cannot Connect! Try Again. \n");
				usernameFieldOnline.setEditable(true);
			}
		} else if (isConnectedToDirServer == true) {
			textAllUsersOnline.append("You are already connected. \n");
		}
	}

	/**
	 * Host button action performed.
	 *
	 * @param evt the evt
	 */
	private void hostButtonActionPerformed(java.awt.event.ActionEvent evt) {

		username = usernameFieldOnline.getText();
		hostingport = Integer.parseInt(PortField.getText());
		if (isConnectedToChatServer == false && isConnectedToDirServer == true) {
			try {
				DatagramSocket datagramSocket = new DatagramSocket();

				InetAddress receiverAddress = InetAddress.getLocalHost();
				serverIP = receiverAddress.getHostAddress();
				// System.out.println(serverIP);
				String messageToSend = "CL:HO:" + username + ":" + serverIP + ":" + hostingport + ":" + rating;
				// System.out.println("Sending message" + messageToSend);
				byte[] buffer = messageToSend.getBytes();

				DatagramPacket packet = new DatagramPacket(buffer, buffer.length, receiverAddress, dirport);
				datagramSocket.send(packet);
				datagramSocket.close();

			} catch (Exception ex) {

			}
		}

		// TODO add your handling code here:
		if (isConnectedToChatServer == false && isConnectedToDirServer == true) {
			initChatClient();
			p2p = new PeerServer(hostingport);
			p2p.setVisible(true);

			username = usernameField.getText();
			usernameField.setEditable(false);

			try {
				sock = new Socket(serverIP, hostingport);
				InputStreamReader streamreader = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(streamreader);
				writer = new PrintWriter(sock.getOutputStream());
				writer.println(username + ":has connected.:Connect"); // Displays that user has connected
				writer.flush(); // flushes the buffer
				isConnectedToChatServer = true; // Used to see if the client is connected.
			} catch (Exception ex) {
				chatTextArea.append("Cannot Connect! Try Again. \n");
				usernameField.setEditable(true);
			}
			ListenThreadSer();
		} else if (isConnectedToChatServer == true) {
			chatTextArea.append("You are already connected. \n");
		}

	}

	/**
	 * Connect button action performed.
	 *
	 * @param evt the evt
	 */

	/**
	 * Disconnect button action performed.
	 *
	 * @param evt the evt
	 */
	private void disconnectButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// TODO add your handling code here:
		sendDisconnect();
		Disconnect();

		try {
			DatagramSocket datagramSocket = new DatagramSocket();

			String messageToSend = "CL:DH:" + preUserList.get(1) + ":" + serverIP + ":" + hostingport + ":" + rating;

			byte[] buffer = messageToSend.getBytes();
			InetAddress receiverAddress = InetAddress.getLocalHost();

			DatagramPacket packet = new DatagramPacket(buffer, buffer.length, receiverAddress, dirport);
			datagramSocket.send(packet);

			datagramSocket.close();

		} catch (Exception ex) {
			textAllUsersOnline.append("Cannot Connect! Try Again. \n");
			usernameFieldOnline.setEditable(true);
		}
		initRoomToJoin();

	}

	/**
	 * Send button action performed.
	 *
	 * @param evt the evt
	 */
	private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// TODO add handling code here:
		String nothing = "";
		if ((inputTextArea.getText()).equals(nothing)) {
			inputTextArea.setText("");
			inputTextArea.requestFocus();
		} else {
			writer.println(username + ":" + inputTextArea.getText() + ":" + "Chat");
			writer.flush(); // flushes the buffer
			inputTextArea.setText("");
			inputTextArea.requestFocus();
		}

		inputTextArea.setText("");
		inputTextArea.requestFocus();
	}

}