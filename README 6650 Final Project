README 6650 Final Project
- P2P chat room application

How To Compile Application:
Compile all the java files using command javac filename.java
> javac PeerServer.java DirectoryServer.java ChatMain.java

How to run application:
Execute the jar files in the packet, DirectoryServer.jar and ChatMain.jar.
> java -jar DirectoryServer.jar
> java -jar ChatMain.jar
Run class files created after compiling the java files, the execute with command: 
> java DirectoryServer 
> java ChatMain
Run the files directly from IDE after importing them.

Using the Application steps:
(It is rather complex running the application, please follow strictly with instructions)
Execute the DirectoryServer.jar file first and click the “start” button to start the directory service, then execute multiple ChatMain.jar files. ( Each execution of ChatMain.jar is a start of a peer node).
In the Chat User interface, enter a username in the textbox then click the “Go Online” button to join the directory server. 
You may click the “Go Offline” button to test if required.
Once you have joined the directory server you can now host a chat room.
Enter the port for which you want to host a chat room, and click the “Host” button.
Two new windows should appear, (“Peer2PeerServer” and “Chat Client”) and you should be connected in a chat room, capable of typing or disconnecting.
Clicking disconnect will return you to the main menu where you will have to input your information again.
At this point, execute another ChatMain.jar. Enter a new username and click the button “Go Online”.
Click the “query for peers” button, the Host Servers and Users online fields should be populated with online hosts and online users’ list.
Once you see a user hosting a room in the Host Servers text field, (format should be: username R:X where X is the number of members in the room) you can now join that specific hosted room.
Enter the username of the person you see in the Host Servers field into the text field next to the “Join” button. Click the “Join” button.
You should now be in a chat room with that user, and be able to chat with him by entering a message and clicking the “Send” button.
For private 1:1 communication of the one of the Online Users, add “@<name>”, a space before the message and send. Only the user with the stated name would receive that message and should “(private)” annotation.

Fault Tolerance Demo:
Using the application as previously stated, start 3 Chat User interfaces and join the 3 members in the same chat room. One of them would host a room.
Close the Peer2PeerServer interface or the host’s Chat Client interface, which means an overall fail of the host peer node.
You would see a new Peer2PeerServer interface immediately pop up and from the chat history with some host change log like following:
Host failed!
Tim is the new host!!
Tim: has connected.
Bob: has connected.
The failed user is also now removed from the Online Users and we have a new host server running on the side of the next user in the chat room.
You can start a new ChatMain to rejoin the host to the same room, but now the host name would change to the current host’s name.

Key Notes:
You must be connected to the directory server using the “Go Online” button to use the “Host” , “Join” and “Query For Peers” buttons.
You must use the “Query For Peers” button and get all necessary information appearing in the text fields in order to use the ”Join” button.
Number of current members of a chat room is shown in the Host Servers text field next to the username (R:X).
