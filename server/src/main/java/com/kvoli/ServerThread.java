package com.kvoli;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> map1 = new HashMap<>();
        map1.put("type", "newidentity");
        map1.put("former", "");
        map1.put("identity", Server.socketList.get(s));
        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map1));

        Map<String, Object> map2 = new HashMap<>();
        map2.put("type", "roomlist");
        Map<String, Object> innerMap = new HashMap<>();
        for (String room : Server.roomList.keySet()) {
            innerMap.put(room, Server.roomList.get(room).size());
        }
        map2.put("rooms", innerMap);
        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map2));

        Map<String, Object> map3 = new HashMap<>();
        map3.put("type", "roomchange");
        map3.put("identity", Server.socketList.get(s));
        map3.put("former", "");
        map3.put("roomid", "MainHall");
        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map3));

        Map<String, Object> map4 = new HashMap<>();
        map4.put("type", "roomcontents");
        map4.put("roomid", "MainHall");
        List<String> roomMember = new ArrayList<>();
        for (Socket socket : Server.roomList.get("MainHall")) {
            roomMember.add(Server.socketList.get(socket));
        }
        map4.put("identities", roomMember);
        map4.put("owner", "");
        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map4));

        out.flush();
    }

    public void run() {
    }
}
