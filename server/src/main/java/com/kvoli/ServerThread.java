package com.kvoli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kvoli.base.Command;

import java.io.*;
import java.net.Socket;
import java.util.*;

/*
 * This is ServerThread class extended from Thread. It is used for dealing with client socket.
 * There are 8 protocols plus an initialization. All the details are stated in the README file.
 */
public class ServerThread implements Runnable {
    Socket s;

    public ServerThread(Socket s) {
        this.s = s;
    }

    public void run() {
        try {
            //add client to the MainHall
            if (Server.roomList.get("MainHall") != null) {
                Server.roomList.get("MainHall").add(s);
            } else {
                ArrayList<Socket> newSocket = new ArrayList<>();
                newSocket.add(s);
                Server.roomList.put("MainHall", newSocket);
            }

            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            ObjectMapper mapper = new ObjectMapper();

            //Initialization
            //NewIdentity message
            Map<String, Object> map1 = new HashMap<>();
            map1.put("type", "newidentity");
            map1.put("former", "");
            map1.put("identity", Server.socketList.get(s));
            out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map1));

            //RoomList message
            roomListMessage(out, mapper);

            //RoomChange message
            Map<String, Object> map2 = new HashMap<>();
            map2.put("type", "roomchange");
            map2.put("identity", Server.socketList.get(s));
            map2.put("former", "");
            map2.put("roomid", "MainHall");
            for (Socket socket : Server.roomList.get("MainHall")) {
                DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                outputStream.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map2));
            }

            //RoomContents message
            roomContentMessage(out, mapper, "MainHall");
            out.writeUTF("EOF");

            out.flush();

            DataInputStream in = new DataInputStream(s.getInputStream());

            mainLoop :
            while (true) {
                String require = in.readUTF();
                Command command = mapper.readValue(require, Command.class);

                //handle command messages send by clients
                switch (command.getType()) {
                    case "identitychange":
                        //new identity is valid
                        //hasn't been used before
                        //doesn't equal to the current one
                        if (command.getIdentity().matches("^[0-9a-zA-Z]+$")
                                && command.getIdentity().length() >= 3
                                && command.getIdentity().length() <= 16
                                && !Server.socketList.containsValue(command.getIdentity())
                                && !Server.socketList.get(s).equals(command.getIdentity())) {

                            //generate IdentityChange message
                            Map<String, Object> identityChangeMap1 = new HashMap<>();
                            identityChangeMap1.put("type", "newidentity");
                            identityChangeMap1.put("former", Server.socketList.get(s));
                            identityChangeMap1.put("identity", command.getIdentity());

                            //broadcast IdentityChange message
                            for (Socket s : Server.socketList.keySet()) {
                                DataOutputStream outputStream = new DataOutputStream(s.getOutputStream());
                                outputStream.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(identityChangeMap1));
                                outputStream.writeUTF("EOF");
                                outputStream.flush();
                            }

                            //update client identity
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
                        //find the client's current room
                        String former = null;
                        for (String room : Server.roomList.keySet()) {
                            if (Server.roomList.get(room).contains(s)) {
                                former = room;
                            }
                        }

                        //room existed and doesn't equal to the current one
                        if (Server.roomList.containsKey(command.getRoomid()) && !Objects.equals(former, command.getRoomid())) {
                            Map<String, Object> joinMap1 = new HashMap<>();
                            joinMap1.put("type", "roomchange");
                            joinMap1.put("identity", Server.socketList.get(s));
                            joinMap1.put("former", former);
                            joinMap1.put("roomid", command.getRoomid());

                            //broadcast RoomChange message to all the clients in the former room
                            ArrayList<Socket> formerRoom = Server.roomList.get(former);
                            formerRoom.remove(s);
                            for (Socket s : formerRoom) {
                                DataOutputStream outputStream = new DataOutputStream(s.getOutputStream());
                                outputStream.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(joinMap1));
                                outputStream.writeUTF("EOF");
                                outputStream.flush();
                            }

                            //broadcast RoomChange message to all the client in the new room
                            for (Socket s : Server.roomList.get(command.getRoomid())) {
                                DataOutputStream outputStream = new DataOutputStream(s.getOutputStream());
                                outputStream.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(joinMap1));
                                outputStream.writeUTF("EOF");
                                outputStream.flush();
                            }

                            //if client join MainHall
                            //also send RoomList message and RoomContents message
                            out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(joinMap1));
                            if (command.getRoomid().equals("MainHall")) {
                                roomListMessage(out, mapper);
                                roomContentMessage(out, mapper, "MainHall");
                            }

                            out.writeUTF("EOF");
                            out.flush();

                            //remove client from former room
                            //add client to the new room
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
                                roomContentMessage(out, mapper, "MainHall");
                            }

                            out.writeUTF("EOF");
                            out.flush();
                        }
                        break;
                    case "who":
                        roomContentMessage(out, mapper, command.getRoomid());
                        out.writeUTF("EOF");
                        out.flush();
                        break;
                    case "list":
                        roomListMessage(out, mapper);
                        out.writeUTF("EOF");
                        out.flush();
                        break;
                    case "createroom":
                        //whether the room's name is valid
                        //whether the room is existed already
                        if (command.getRoomid().matches("^[0-9a-zA-Z]+$")
                                && command.getRoomid().length() >= 3
                                && command.getRoomid().length() <= 32
                                && !Server.roomOwner.containsKey(command.getRoomid())) {
                            Server.roomOwner.put(command.getRoomid(), Server.socketList.get(s));
                            ArrayList<Socket> sockets = new ArrayList<>();
                            Server.roomList.put(command.getRoomid(), sockets);
                            roomListMessage(out, mapper);
                        } else {
                            out.writeUTF("EOF");
                        }
                        out.writeUTF("EOF");
                        out.flush();
                        break;
                    case "delete":
                        //whether the client owns the room
                        if (Server.socketList.get(s).equals(Server.roomOwner.get(command.getRoomid()))
                                && Server.roomList.get(command.getRoomid()).contains(s)) {
                            Map<String, Object> deleteMap = new HashMap<>();
                            deleteMap.put("type", "roomchange");
                            deleteMap.put("identity", Server.socketList.get(s));
                            deleteMap.put("former", command.getRoomid());
                            deleteMap.put("roomid", "MainHall");

                            //send RoomChange message to all the clients in the room which is
                            //going to be deleted
                            ArrayList<Socket> formerRoom = Server.roomList.get(command.getRoomid());
                            formerRoom.remove(s);
                            for (Socket s : formerRoom) {
                                DataOutputStream outputStream = new DataOutputStream(s.getOutputStream());
                                deleteMap.replace("identity", Server.socketList.get(s));
                                outputStream.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(deleteMap));

                                //broadcast RoomChange message in the MainHall
                                for (Socket socket : Server.roomList.get("MainHall")) {
                                    DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                                    output.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(deleteMap));
                                    output.writeUTF("EOF");
                                    output.flush();
                                }

                                //move clients to the MainHall
                                //send RoomList message
                                //send RoomContents message
                                Server.roomList.get("MainHall").add(s);
                                roomListMessage(outputStream, mapper);
                                roomContentMessage(outputStream, mapper, command.getRoomid());

                                outputStream.writeUTF("EOF");
                                outputStream.flush();
                            }

                            //remove room form room list
                            Server.roomList.get("MainHall").add(s);
                            Server.roomList.remove(command.getRoomid());
                            Server.roomOwner.remove(command.getRoomid());

                            roomListMessage(out, mapper);
                        } else {
                            out.writeUTF("EOF");
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

                        //broadcast Message in the current room
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

    //deal with Quit message
    private void quit() {
        //update room owner list
        for (String room : Server.roomOwner.keySet()) {
            if (Server.roomOwner.get(room).equals(Server.socketList.get(s))) {
                Server.roomOwner.replace(room, "");

                //if the room is empty, delete that room
                if (Server.roomList.get(room).isEmpty()) {
                    Server.roomList.remove(room);
                }
            }
        }

        //remove client from current room
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
        //remove client from current socket kist
        Server.socketList.remove(s);
    }

    //generate RoomList message
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

    //generate RoomContents message
    private void roomContentMessage(DataOutputStream out, ObjectMapper mapper, String roomid) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("type", "roomcontents");
        map.put("roomid", roomid);
        List<String> roomMember = new ArrayList<>();
        for (Socket socket : Server.roomList.get(roomid)) {
            if (Server.roomOwner.containsValue(Server.socketList.get(socket))) {
                roomMember.add(Server.socketList.get(socket) + '*');
            } else {
                roomMember.add(Server.socketList.get(socket));
            }
        }
        map.put("identities", roomMember);
        map.put("owner", Server.roomOwner.get(roomid));
        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map));
    }
}
