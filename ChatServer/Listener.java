/*
-* This project is our own work. We have not recieved assistance beyond what is
-* normal, and we have cited any sources from which we have borrowed. We have
-* not given a copy of our work, or a part of our work, to anyone. We are aware
-* that copying or giving a copy may have serious consequences.
-*
-* @author Ian Duffy, 11356066
-* @author Richard Kavanagh, 11482928
-* @author Darren Brogan, 11424362
-*/

import java.util.*;
import java.io.*;

/**
 * Listen for messages from the client. Forward them to the MessageServer.
 */
class Listener extends Thread {
  // Reference to MessageServer.
  private MessageServer messageServer;

  // Reference to Connection.
  private Connection connection;

  // Input stream.
  private BufferedReader instream;

  Listener(Connection connection, MessageServer messageServer) {
    // Setup necessary references.
    this.connection = connection;
    this.messageServer = messageServer;

    // Setup the instream.
    try {
      instream = new BufferedReader(
        new InputStreamReader(connection.socket.getInputStream()));
    } catch(Exception e) { }
  }

  /// Listen for messages.
  public void run() {
    try {
      connection.nickname = instream.readLine();
      if(messageServer.isFull()) {
        connection.sender.addMessage("Server is at maximum capacity.");
        sleep(100);
        connection.close();
      } else {
        messageServer.addConnection(connection);
        messageServer.addMessage(connection.nickname
                                 + " has joined the chatroom...");
        while(!isInterrupted()) {
          String message = instream.readLine();

          if(message == null) {
            break;
          } else {
            message = message.trim();
          }

          if(message.equals("")) {
            connection.sender.addMessage("You attempted to end an empty message");
          } else {
            message = connection.nickname + " says : " + message;
            messageServer.addMessage(message);
          }
        }
      }
    } catch(Exception e) { }

    connection.close();
    connection.sender.interrupt();
    messageServer.deleteConnection(connection);
  }

  /// Closes the instream.
  public void close() {
    try {
      instream.close();
    } catch(Exception e) { }
  }
}