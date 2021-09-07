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

        DataOutputStream out = new DataOutputStream(s.getOutputStream());

        out.writeUTF("NewIdentity message");

        out.writeUTF("Connect to " + s.getLocalAddress().getHostName() + " as " + Server.socketList.get(s));

        for (String room : Server.roomList.keySet()) {
            out.writeUTF(room + ": " +  Server.roomList.get(room).size() +" guests");
        }

        out.writeUTF(Server.socketList.get(s) + " moves to MainHall");

        for (String room : Server.roomList.keySet()) {
            StringBuilder roomMember = new StringBuilder(room + " contains ");
            for (Socket socket : Server.roomList.get(room)) {
                roomMember.append(Server.socketList.get(socket)).append(" ");
            }
            out.writeUTF(String.valueOf(roomMember));
        }
        out.flush();
    }

    public void run() {
    }
}
