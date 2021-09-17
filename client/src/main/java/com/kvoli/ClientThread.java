package com.kvoli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.kvoli.base.Packet;

import java.io.*;
import java.net.Socket;
import java.util.*;

/*
 * This is ClientThread class extended from Tread class. It is used for handling socket
 * connection with the server. At meantime, the thread will also wait for the user input.
 *
 * The client supports 7 command plus a message.
 * #identitychange: user could change identity
 * #join: user could join a room
 * #who: user could request for a list of all the clients in a room
 * #list: user could request for a list of all the rooms in the server
 * #createroom: user could create a new room
 * #deleteroom: user could delete the room which he or she created
 * #quit: user could exit the chat system
 *
 * All the other input will be regarded as message.
 */
public class ClientThread implements Runnable {
    private Socket s; //socket
    private String id; //client identity
    private String roomid; //room's name

    public ClientThread(Socket s) {
        this.s = s;
    }

    public void run() {
        try {
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            ObjectMapper mapper = new ObjectMapper();

            Scanner kb = new Scanner(System.in);

            mainLoop:
            while (true) {
                //handle previous response messages before user input
                while (true) {
                    try {
                        String content = in.readUTF();
                        handleContent(content);
                    } catch (Exception e) {
                        break;
                    }
                }

                //user input
                System.out.printf("[%s] %s>", roomid, id);
                String input = kb.nextLine();
                String[] command = input.split(" ");

                //deal with commands and messages
                switch (command[0].toLowerCase()) {
                    case "#identitychange":
                        Map<String, Object> map1 = new HashMap<>();
                        map1.put("type", "identitychange");
                        map1.put("identity", command[1]);
                        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map1));
                        out.flush();
                        break;
                    case "#join":
                        Map<String, Object> map2 = new HashMap<>();
                        map2.put("type", "join");
                        map2.put("roomid", command[1]);
                        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map2));
                        out.flush();
                        break;
                    case "#who":
                        Map<String, Object> map3 = new HashMap<>();
                        map3.put("type", "who");
                        map3.put("roomid", command[1]);
                        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map3));
                        out.flush();
                        break;
                    case "#list":
                        Map<String, Object> map4 = new HashMap<>();
                        map4.put("type", "list");
                        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map4));
                        out.flush();
                        break;
                    case "#createroom":
                        Map<String, Object> map5 = new HashMap<>();
                        map5.put("type", "createroom");
                        map5.put("roomid", command[1]);
                        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map5));
                        out.flush();

                        String response = in.readUTF();
                        if (response.equals("EOF")) {
                            System.out.println("Room " + command[1] + " is invalid or already in use.");
                        } else {
                            Packet packet = mapper.readValue(response, Packet.class);
                            if (packet.getType().equals("roomlist")) {
                                System.out.println("Room " + command[1] + " created.");
                            }
                        }
                        break;
                    case "#delete":
                        Map<String, Object> map6 = new HashMap<>();
                        map6.put("type", "delete");
                        map6.put("roomid", command[1]);
                        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map6));
                        out.flush();

                        roomid = "MainHall";

                        String deleteResponse = in.readUTF();
                        if (deleteResponse.equals("EOF")) {
                            System.out.println("Error! Room " + command[1] + " does not existed " +
                                    "or you are not the owner of room " + command[1]);
                        } else {
                            Packet deletePacket = mapper.readValue(deleteResponse, Packet.class);
                            if (deletePacket.getType().equals("roomlist")) {
                                handleContent(deleteResponse);
                            }
                        }
                        break;
                    case "#quit":
                        Map<String, Object> map7 = new HashMap<>();
                        map7.put("type", "quit");
                        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map7));
                        out.flush();
                        break mainLoop;
                    default:
                        Map<String, Object> map8 = new HashMap<>();
                        map8.put("type", "message");
                        map8.put("content", input);
                        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map8));
                        out.flush();
                        break;
                }
            }
            in.close();
            out.close();
            System.exit(0);
        } catch (IOException e) {
            System.out.println("Something wrong happens. Please check!");
            System.exit(0);
        }
    }

    //a method using for handling response message
    public void handleContent(String content) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Packet packet = mapper.readValue(content, Packet.class);

        switch (packet.getType()) {
            case "newidentity":
                if (Objects.equals(packet.getFormer(), "")) {
                    System.out.println("Connect to " + s.getRemoteSocketAddress() + " as " + packet.getIdentity());
                    id = packet.getIdentity();
                } else if (packet.getFormer() != null && packet.getFormer().equals(packet.getIdentity())) {
                    System.out.println("Requested identity invalid or in use.");
                } else {
                    System.out.println(packet.getFormer() + " is now " + packet.getIdentity());
                }
                break;
            case "roomlist":
                for (String room : packet.getRooms().keySet()) {
                    System.out.println(room + ": " + packet.getRooms().get(room) + " guests");
                }
                break;
            case "roomchange":
                if (packet.getFormer().equals("MainHall") || Objects.equals(packet.getFormer(), "")) {
                    System.out.println(packet.getIdentity() + " moves to " + packet.getRoomid());
                } else {
                    System.out.println(packet.getIdentity() + " moved from " + packet.getFormer() + " to " + packet.getRoomid());
                }
                roomid = packet.getRoomid();
                break;
            case "roomcontents":
                System.out.println(packet.getRoomid() + " contains " + packet.getIdentities());
                break;
            case "message":
                System.out.println(packet.getIdentity() + ": " + packet.getContent());
                break;
        }
    }
}
