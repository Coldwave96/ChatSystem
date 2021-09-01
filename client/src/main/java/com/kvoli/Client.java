package com.kvoli;

import com.kvoli.base.Base;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.net.Socket;

public class Client {
  private int port;
  private String ip;

  public void setPort(int port) {
    this.port = port;
  }

  public void setIp(String serverIp) {
    this.ip = serverIp;
  }

  static class CmdOption {
    @Option(name = "-h", usage = "Server ipaddress")
    private String serverIp;

    @Option(name = "-p", hidden = true, usage = "Service listening port")
    private int port = 4444;
  }

  public static void main(String[] args) {
    CmdOption option = new CmdOption();
    CmdLineParser parser = new CmdLineParser(option);

    Client client = new Client();

    try {
      parser.parseArgument(args);

      if (option.serverIp == null) {
        System.out.println("Hostname must not be null.");
        System.exit(0);
      } else {
        client.setIp(option.serverIp);
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

  public void handle() {
    try {
      Socket client = new Socket(ip, port);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
