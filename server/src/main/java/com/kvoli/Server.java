package com.kvoli;

import com.kvoli.base.Base;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
  private int port;

  public static ArrayList<Socket> socketArrayList = new ArrayList<>();

  public void setPort(int port) {
    this.port = port;
  }

  static class CmdOption {
    @Option(name = "-p", hidden = true, usage = "Service listening port")
    private int port = 4444;
  }

  public static void main(String[] args) {
    CmdOption option = new CmdOption();
    CmdLineParser parser = new CmdLineParser(option);

    Server server = new Server();

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

  public static boolean isLocalPortUsing(int port) {
    boolean flag = false;
    try {
      Socket socket = new Socket("127.0.0.1", port);
      flag = true;
    } catch (IOException e) {
    }
    return flag;
  }

  public void handle() {
    ServerSocket serverSocket;

    try {
      serverSocket = new ServerSocket(port);
      System.out.printf("Listening on port %d\n", port);

      while (true) {
        Socket newSocket = serverSocket.accept();
        socketArrayList.add(newSocket);
        new Thread(new ServerThread(newSocket)).start();
      }
    } catch (IOException e) {
      System.out.printf("Error handling connections, %s\n", e.getMessage());
    }
  }
}
