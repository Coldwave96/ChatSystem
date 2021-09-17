package com.kvoli;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * This is the main class of the server. First server will check the given port is free or not.
 * Then the server will listen on the port and waiting for the connection from clients.
 * Once a socket connection comes, the server will start a thread to handle this socket.
 * While the main class still listen on the port.
 */
public class Server {
  private int port; //listen port
  private int guestId = 1; //create initial identity for new clients automatically

  //store all the current sockets
  public static HashMap<Socket, String> socketList = new HashMap<>(); //<socket, identity>
  //store all the clients of each room
  public static HashMap<String, ArrayList<Socket>> roomList = new HashMap<>(); //<room, sockets>
  //store each room and its owner
  public static HashMap<String, String> roomOwner = new HashMap<>(); //<room, owner>

  public void setPort(int port) {
    this.port = port;
  }

  //command line setter
  static class CmdOption {
    @Option(name = "-p", hidden = true, usage = "Service listening port")
    private int port = 4444;
  }

  public static void main(String[] args) {
    CmdOption option = new CmdOption();
    CmdLineParser parser = new CmdLineParser(option);

    Server server = new Server();
    roomOwner.put("MainHall", "");
    roomList.put("MainHall", null);

    try {
      parser.parseArgument(args);

      if (option.port < 1 || option.port > 65535) {
        System.out.println("Port error, please check.");
        System.exit(0);
      } else if (isLocalPortUsing(option.port)) {
        System.out.printf("Port %d is occupied. Please try another one.\n", option.port);
        System.exit(0);
      } else {
        server.setPort(option.port);
      }
    } catch (CmdLineException e) {
      System.out.println("Command line error: " + e.getMessage());
      return;
    }

    server.handle();
  }

  //check whether the given port is used or not
  public static boolean isLocalPortUsing(int port) {
    boolean flag = false;
    try {
      Socket socket = new Socket("127.0.0.1", port);
      flag = true;
    } catch (IOException e) {
    }
    return flag;
  }

  //handle server socket and start new thread dealing with client socket
  public void handle() {
    ServerSocket serverSocket;

    try {
      serverSocket = new ServerSocket(port);
      System.out.printf("Listening on port %d\n", port);

      while (true) {
        Socket newSocket = serverSocket.accept();
        socketList.put(newSocket, "guest" + guestId);
        new Thread(new ServerThread(newSocket)).start();

        guestId += 1;
      }
    } catch (Exception e) {
      System.out.printf("Error handling connections, %s\n", e.getMessage());
    }
  }
}
