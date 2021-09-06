package com.kvoli;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread implements Runnable {
    Socket s = null;
    BufferedReader br = null;

    public ServerThread(Socket s) throws IOException {
        this.s = s;

        if (Server.roomList.get("MainHall") != null) {
            Server.roomList.get("MainHall").add(s);
        } else {
            ArrayList<Socket> newSocket = new ArrayList<>();
            newSocket.add(s);
            Server.roomList.put("MainHall", newSocket);
        }

        PrintWriter printWriter = new PrintWriter(s.getOutputStream(), true);
        printWriter.printf("Connect to %s as %s.\n", s.getLocalAddress().getHostName(),
                Server.socketList.get(s));

        for (String room : Server.roomList.keySet()) {
            printWriter.printf("%s: %d guests\n", room, Server.roomList.get(room).size());
        }

        printWriter.printf("%s moves to MainHall\n", Server.socketList.get(s));

        for (String room : Server.roomList.keySet()) {
            printWriter.printf("%s contains ", room);
            for (Socket socket : Server.roomList.get(room)) {
                printWriter.printf("%s ", Server.socketList.get(socket));
            }
            printWriter.println();
        }
    }

    public void run() {
//        DataOutputStream out = null;
//        try {
//            out = new DataOutputStream(s.getOutputStream());
//            out.writeUTF("hello");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        try {
//            String content = null;
//
//            while ((content = readFromClient()) != null) {
//                for (Socket s : Server.socketList) {
//                    PrintStream ps = new PrintStream(s.getOutputStream());
//                    ps.println(content);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public String readFromClient() {
        try {
            return br.readLine();
        } catch (IOException e) {
            Server.socketList.remove(s);
        }
        return null;
    }
}
