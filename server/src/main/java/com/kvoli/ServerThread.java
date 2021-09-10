package com.kvoli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvoli.base.Command;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ServerThread implements Runnable {
    Socket s;

    public ServerThread(Socket s) throws IOException, InterruptedException {
        this.s = s;
    }

    public void run() {
        try {
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
            for (Socket socket : Server.roomList.get("MainHall")) {
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                outputStream.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map3));
            }

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

            out.writeUTF("EOF");

            out.flush();

            DataInputStream in = new DataInputStream(s.getInputStream());

            while (true) {
                String require = in.readUTF();
                Command command = mapper.readValue(require, Command.class);

                switch (command.getType()) {
                    case "identitychange":
                        if (command.getIdentity().matches("^[0-9a-zA-Z]+$")
                                && command.getIdentity().length() >= 3
                                && command.getIdentity().length() <= 16
                                && !Server.socketList.containsValue(command.getIdentity())) {
                            Map<String, Object> identityChangeMap1 = new HashMap<>();
                            identityChangeMap1.put("type", "newidentity");
                            identityChangeMap1.put("former", Server.socketList.get(s));
                            identityChangeMap1.put("identity", command.getIdentity());
                            out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(identityChangeMap1));
                            out.flush();

                            if (!Objects.equals(Server.socketList.get(s), command.getIdentity())) {
                                for (Socket s : Server.socketList.keySet()) {
                                    DataOutputStream outputStream = new DataOutputStream(s.getOutputStream());
                                    outputStream.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map1));
                                    outputStream.flush();
                                }
                                Server.roomOwner.forEach((key, value) -> {
                                    if (Objects.equals(value, Server.socketList.get(s))) {
                                        Server.roomOwner.replace(key, command.getIdentity());
                                    }
                                });
                                Server.socketList.replace(s, command.getIdentity());
                            }
                        } else {
                            Map<String, Object> identityChangeMap2 = new HashMap<>();
                            identityChangeMap2.put("type", "newidentity");
                            identityChangeMap2.put("former", Server.roomList.get(s));
                            identityChangeMap2.put("identity", Server.roomList.get(s));
                            out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(identityChangeMap2));
                            out.flush();
                            break;
                        }
                        break;
                    case "join":
                        //sth to do
                    case "who":
                        //sth to do
                    case "list":
                        //sth to do
                    case "createroom":
                        //sth to do
                    case "delete":
                        //sth to do
                    case "quit":
                        //sth to do
                    default:
                        //sth to do
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
