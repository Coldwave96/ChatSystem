package com.kvoli;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.net.Socket;

/*
 * This is the main class of the client. After started the client program, client will try to
 * connect to the given server. Once socket connection established, client will apply for a
 * thread to handle the socket connection.
 */
public class Client {
  private int port; //server port
  private String ip; //server ip

  public void setPort(int port) {
    this.port = port;
  }

  public void setIp(String serverIp) {
    this.ip = serverIp;
  }

  //command line setter
  static class CmdOption {
    @Option(name = "-p", hidden = true, usage = "Service listening port")
    private int port = 4444;
  }

  public static void main(String[] args) throws Exception {
    CmdOption option = new CmdOption();
    CmdLineParser parser = new CmdLineParser(option);

    Client client = new Client();

    String[] optionArgs = new String[2];

    try {
      if (args[0] == null) {
        System.out.println("Hostname must not be null.");
        System.exit(0);
      } else if (args.length == 1) {
        client.setIp(args[0]);
      } else if (args.length == 3) {
        client.setIp(args[0]);
        optionArgs[0] = args[1];
        optionArgs[1] = args[2];
        parser.parseArgument(optionArgs);
      } else {
        System.out.println("Command line error. Please check.");
        System.exit(0);
      }

      if (option.port < 1 || option.port > 65535) {
        System.out.println("Port error, please check.");
        System.exit(0);
      } else {
        client.setPort(option.port);
      }
    } catch (CmdLineException e) {
      System.out.println("Command line error: " + e.getMessage());
      return;
    }

    client.handle();
  }

  //start a thread to handle the socket connection
  public void handle() throws Exception {
    Socket client = new Socket(ip, port);
    new Thread(new ClientThread(client)).start();
  }
}
