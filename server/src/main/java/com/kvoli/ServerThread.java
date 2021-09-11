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

            roomListMessage(out, mapper);

            Map<String, Object> map2 = new HashMap<>();
            map2.put("type", "roomchange");
            map2.put("identity", Server.socketList.get(s));
            map2.put("former", "");
            map2.put("roomid", "MainHall");
            for (Socket socket : Server.roomList.get("MainHall")) {
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                outputStream.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map2));
            }

            roomContentMessage(out, mapper);
            out.writeUTF("EOF");

            out.flush();

            DataInputStream in = new DataInputStream(s.getInputStream());

            mainLoop :
            while (true) {
                String require = in.readUTF();
                Command command = mapper.readValue(require, Command.class);

                switch (command.getType()) {
                    case "identitychange":
                        if (command.getIdentity().matches("^[0-9a-zA-Z]+$")
                                && command.getIdentity().length() >= 3
                                && command.getIdentity().length() <= 16
                                && !Server.socketList.containsValue(command.getIdentity())
                                && !Server.socketList.get(s).equals(command.getIdentity())) {

                            Map<String, Object> identityChangeMap1 = new HashMap<>();
                            identityChangeMap1.put("type", "newidentity");
                            identityChangeMap1.put("former", Server.socketList.get(s));
                            identityChangeMap1.put("identity", command.getIdentity());

                            for (Socket s : Server.socketList.keySet()) {
                                DataOutputStream outputStream = new DataOutputStream(s.getOutputStream());
                                outputStream.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(identityChangeMap1));
                                outputStream.writeUTF("EOF");
                                outputStream.flush();
                            }
                            Server.roomOwner.forEach((key, value) -> {
                                if (Objects.equals(value, Server.socketList.get(s))) {
                                    Server.roomOwner.replace(key, command.getIdentity());
                                }
                            });
                            Server.socketList.replace(s, command.getIdentity());
                        } else {
                            Map<String, Object> identityChangeMap2 = new HashMap<>();
                            identityChangeMap2.put("type", "newidentity");
                            identityChangeMap2.put("former", Server.socketList.get(s));
                            identityChangeMap2.put("identity", Server.socketList.get(s));
                            out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(identityChangeMap2));
                            out.writeUTF("EOF");
                            out.flush();
                            break;
                        }
                        break;
                    case "join":
                        String former = null;
                        for (String room : Server.roomList.keySet()) {
                            if (Server.roomList.get(room).contains(s)) {
                                former = room;
                            }
                        }

                        if (Server.roomList.containsKey(command.getRoomid()) && !Objects.equals(former, command.getRoomid())) {
                            Map<String, Object> joinMap1 = new HashMap<>();
                            joinMap1.put("type", "roomchange");
                            joinMap1.put("identity", Server.socketList.get(s));
                            joinMap1.put("former", former);
                            joinMap1.put("roomid", command.getRoomid());

                            ArrayList<Socket> formerRoom = Server.roomList.get(former);
                            formerRoom.remove(s);
                            for (Socket s : formerRoom) {
                                DataOutputStream outputStream = new DataOutputStream(s.getOutputStream());
                                outputStream.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(joinMap1));
                                outputStream.writeUTF("EOF");
                                outputStream.flush();
                            }

                            for (Socket s : Server.roomList.get(command.getRoomid())) {
                                DataOutputStream outputStream = new DataOutputStream(s.getOutputStream());
                                outputStream.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(joinMap1));
                                outputStream.writeUTF("EOF");
                                outputStream.flush();
                            }

                            out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(joinMap1));
                            if (command.getRoomid().equals("MainHall")) {
                                roomListMessage(out, mapper);
                                roomContentMessage(out, mapper);
                            }

                            out.writeUTF("EOF");
                            out.flush();

                            Server.roomList.get(former).remove(s);
                            Server.roomList.get(command.getRoomid()).add(s);
                        } else {
                            Map<String, Object> joinMap2 = new HashMap<>();
                            joinMap2.put("type", "roomchange");
                            joinMap2.put("identity", Server.socketList.get(s));
                            joinMap2.put("former", former);
                            joinMap2.put("roomid", former);
                            out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(joinMap2));

                            if (command.getRoomid().equals("MainHall")) {
                                roomListMessage(out, mapper);
                                roomContentMessage(out, mapper);
                            }

                            out.writeUTF("EOF");
                            out.flush();
                        }
                        break;
                    case "who":
                        roomContentMessage(out, mapper);
                        out.writeUTF("EOF");
                        out.flush();
                        break;
                    case "list":
                        roomListMessage(out, mapper);
                        out.writeUTF("EOF");
                        out.flush();
                        break;
                    case "createroom":
                        if (command.getRoomid().matches("^[0-9a-zA-Z]+$")
                                && command.getRoomid().length() >= 3
                                && command.getRoomid().length() <= 32
                                && !Server.roomOwner.containsKey(command.getRoomid())) {
                            Server.roomOwner.put(command.getRoomid(), Server.socketList.get(s));
                            ArrayList<Socket> sockets = new ArrayList<>();
                            Server.roomList.put(command.getRoomid(), sockets);
                            roomListMessage(out, mapper);
                        } else {
                            roomContentMessage(out, mapper);
                        }
                        out.writeUTF("EOF");
                        out.flush();
                        break;
                    case "delete":
                        if (Server.socketList.get(s).equals(Server.roomOwner.get(command.getRoomid()))
                                && Server.roomList.get(command.getRoomid()).contains(s)) {
                            Map<String, Object> deleteMap = new HashMap<>();
                            deleteMap.put("type", "roomchange");
                            deleteMap.put("identity", Server.socketList.get(s));
                            deleteMap.put("former", command.getRoomid());
                            deleteMap.put("roomid", "MainHall");

                            ArrayList<Socket> formerRoom = Server.roomList.get(command.getRoomid());
                            formerRoom.remove(s);
                            for (Socket s : formerRoom) {
                                DataOutputStream outputStream = new DataOutputStream(s.getOutputStream());
                                deleteMap.replace("identity", Server.socketList.get(s));
                                outputStream.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(deleteMap));

                                for (Socket socket : Server.roomList.get("MainHall")) {
                                    DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                                    output.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(deleteMap));
                                    output.writeUTF("EOF");
                                    output.flush();
                                }

                                Server.roomList.get("MainHall").add(s);
                                roomListMessage(outputStream, mapper);
                                roomContentMessage(outputStream, mapper);

                                outputStream.writeUTF("EOF");
                                outputStream.flush();
                            }

                            Server.roomList.get("MainHall").add(s);
                            Server.roomList.remove(command.getRoomid());
                            Server.roomOwner.remove(command.getRoomid());

                            roomListMessage(out, mapper);
                        } else {
                            roomContentMessage(out, mapper);
                        }
                        out.writeUTF("EOF");
                        out.flush();
                        break;
                    case "quit":
                        quit();
                        break mainLoop;
                    case "message":
                        Map<String, Object> message = new HashMap<>();
                        message.put("type", "message");
                        message.put("identity", Server.socketList.get(s));
                        message.put("content", command.getContent());

                        String roomId = null;
                        for (String room : Server.roomList.keySet()) {
                            if (Server.roomList.get(room).contains(s)) {
                                roomId = room;
                            }
                        }

                        for (Socket socket : Server.roomList.get(roomId)) {
                            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                            outputStream.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(message));
                            outputStream.writeUTF("EOF");
                            outputStream.flush();
                        }
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            quit();

        }
    }

    private void quit() {
        for (String room : Server.roomOwner.keySet()) {
            if (Server.roomOwner.get(room).equals(Server.socketList.get(s))) {
                Server.roomOwner.replace(room, "");

                if (Server.roomList.get(room).isEmpty()) {
                    Server.roomList.remove(room);
                }
            }
        }

        for (String room : Server.roomList.keySet()) {
            if (Server.roomList.get(room).contains(s)) {
                Server.roomList.get(room).remove(s);

                if (Server.roomList.get(room).isEmpty()
                        && Server.roomOwner.get(room).equals("")
                        && !room.equals("MainHall")) {
                    Server.roomList.remove(room);
                    Server.roomOwner.remove(room);
                }
            }
        }
        Server.socketList.remove(s);
    }

    private void roomListMessage(DataOutputStream out, ObjectMapper mapper) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "roomlist");
        Map<String, Object> innerMap = new HashMap<>();
        for (String room : Server.roomList.keySet()) {
            innerMap.put(room, Server.roomList.get(room).size());
        }
        map.put("rooms", innerMap);
        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map));
    }

    private void roomContentMessage(DataOutputStream out, ObjectMapper mapper) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "roomcontents");
        map.put("roomid", "MainHall");
        List<String> roomMember = new ArrayList<>();
        for (Socket socket : Server.roomList.get("MainHall")) {
            roomMember.add(Server.socketList.get(socket));
        }
        map.put("identities", roomMember);
        map.put("owner", "");
        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map));
    }
}
