package com.kvoli;

import com.kvoli.base.Base;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

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
      ClientThread ch = new ClientThread(client);
      ch.start();
      ch.connect();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  class ClientThread extends Thread {
    // 接收消息不断的接收，用线程来处理

    private Socket client;//客户机对象
    private BufferedReader br;//根据输入流字节读取字符串时用到的对象
    private OutputStream os;
    private InputStream is;
    private String[] strArray;
    private String msg;
    private Scanner sc;//实现通过控制台输入消息的对象

    //构造方法   将实例化的客户机对象传入线程类中，同时根据客户机的到输入输出流
    public ClientThread(Socket client) {
      this.client = client;
      try {
        os = client.getOutputStream();
        is = client.getInputStream();
        br = new BufferedReader(new InputStreamReader(is));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    //在线程run方法中不断接收服务器发来的消息
    public void run() {
      this.recieve();
    }

    //连接
    public void connect() {
      try {
        msg = client.getLocalPort()+"connect sucessfully"+"\r\n";//得到启动客户机程序的端口
        os.write(msg.getBytes());//发送消息提示成功构建与服务器的联系
        sc = new Scanner(System.in);//没有输入会处于阻塞状态，不会再执行下去，考虑用线程
        msg = sc.nextLine();// 从控制台输入的消息
        System.out.println("消息是"+msg);
        strArray = msg.split("@");//字符串数组保存用@字符隔开的字符串，消息输入时要注意符合这样的规则
        System.out.println("========="+strArray.length);
        send(msg);
        close();
      } catch (Exception ef) {
        ef.printStackTrace();
      }
    }

    public void recieve() {
      String inputS;
      try {
        inputS = this.readString(is);
        System.out.println(inputS);
        while (!inputS.equals("bye")) {
          inputS = this.readString(is).trim();// 接受下一句话 trim? 去掉默认添加的字符
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    //发送消息的方法
    public void send(String msg){
      try{
        while (!strArray[1].equals("bye")) {
          os.write((msg + "\r\n").getBytes()); // \r\n代表一句话的结束，是与服务器通信协议的一部分
                  msg = sc.nextLine();
        }
        os.write(msg.getBytes());
      }catch(Exception e){
        e.printStackTrace();
      }
    }

    String inputS;

    public String readString(InputStream is) {
      try {
        inputS = br.readLine();
        System.out.println( inputS);
      } catch (IOException e) {
        e.printStackTrace();
      }
      inputS = inputS.trim();

      return inputS;
    }

    //关闭流
    public void close() {
      try {
        os.close();
        is.close();
        client.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
