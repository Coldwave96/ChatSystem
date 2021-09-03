package com.kvoli;

import com.kvoli.base.Base;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Server {
  private int port;
  private boolean handler_alive = false;

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

  public static String ridicule(String in) {
    Random random = new Random();
    StringBuilder sb = new StringBuilder();
    for (char c : in.toCharArray()) {
      sb.append(random.nextBoolean() ? c : Character.toUpperCase(c));
    }
    return sb.toString();
  }

  public void handle() {
    ServerSocket serverSocket;

    try {
      serverSocket = new ServerSocket(port);

      System.out.printf("Listening on port %d\n", port);
      handler_alive = true;

      while (handler_alive) {
        Socket newSocket = serverSocket.accept();
        EchoConnection conn = new EchoConnection(newSocket);

        if (conn != null) {
          System.out.printf("Accepted new connection from %s:%d\n", newSocket.getLocalAddress().getCanonicalHostName(), newSocket.getPort());
          conn.run();
        } else {
          handler_alive = false;
        }
      }
    } catch (IOException e) {
      System.out.printf("Error handling conns, %s\n", e.getMessage());
      handler_alive = false;
    }
  }

  class EchoConnection {
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private boolean connection_alive;

    public EchoConnection(Socket socket) throws IOException {
      this.socket = socket;
      this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      this.writer = new PrintWriter(socket.getOutputStream());
    }

    public void close() {
      try {
        reader.close();
        writer.close();
        socket.close();
      } catch (IOException e) {
        System.out.println("Error closing conn");
      }
    }

    public void run() {
      connection_alive = true;
      while (connection_alive) {
        try {
          String inputLine = reader.readLine();
          if (inputLine == null) {
            connection_alive = false;
          } else {
            System.out.printf("%d: %s\n", socket.getPort(), inputLine);
            writer.print(ridicule(inputLine));
            writer.println();
            writer.flush();
          }
        } catch (IOException e) {
          connection_alive = false;
        }
      }
      close();
    }
  }
}
