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
class Producer extends Thread {
  // Reference to MessageServer.
  private MessageServer messageServer;

  // Reference to Connection.
  private Connection connection;

  // Input stream.
  private BufferedReader instream;

  Producer(Connection connection, MessageServer messageServer) {
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
      // Set the nickname.
      connection.nickname = instream.readLine();

      // Check that the server isn't full.
      if(messageServer.isFull()) {
        connection.consumer.addMessage("Server is at maximum capacity.");
        sleep(100);
        connection.close();
      } else {

        // Add the connection to the connections list
        messageServer.addConnection(connection);
        messageServer.addMessage(connection.nickname
                                 + " has joined the chatroom...");

        while(!isInterrupted()) {
          // Sleep for 100 milliseconds, don't want to hog the CPU.
          sleep(100);

          // Read in input.
          String message = instream.readLine();

          // If input is null the client has disconnected.
          if(message == null) {
            break;
          } else {
            message = message.trim();
          }

          // Check that the message isn't empty.
          if(message.equals("")) {
            connection.consumer.addMessage("You attempted to send an empty "
                                         + "message");
          } else {
            messageServer.addMessage(connection.nickname + " says : "
                                     + message);
          }
        }
      }
    } catch(Exception e) {
      System.out.println(e.getMessage());
    }
    interrupt();
  }

  public void interrupt() {
    messageServer.deleteConnection(connection);
    connection.close();
    
    try {
      connection.consumer.interrupt();
      connection.consumer.join();
    } catch(Exception e) { }

    super.interrupt();
  }

  /// Closes the instream.
  public void close() {
    try {
      instream.close();
    } catch(Exception e) { }
  }
}