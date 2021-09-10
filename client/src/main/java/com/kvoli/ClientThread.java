package com.kvoli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.kvoli.base.Packet;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class ClientThread implements Runnable {
    private Socket s;
    private String id;
    private String roomid;

    public ClientThread(Socket s) {
        this.s = s;
    }

    public void run() {
        try {
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            ObjectMapper mapper = new ObjectMapper();

            while (true) {
                String content = in.readUTF();
                if (!content.equals("EOF")) {
                    handleContent(content);
                } else {
                    break;
                }
            }

            Scanner kb = new Scanner(System.in);
            while (true) {
                System.out.printf("[%s] %s>", roomid, id);
                String input = kb.nextLine();
                String[] command = input.split(" ");

                switch (command[0]) {
                    case "#identitychange":
                        Map<String, Object> map = new HashMap<>();
                        map.put("type", "identitychange");
                        map.put("identity", command[1]);
                        out.writeUTF(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map));
                        out.flush();

                        String response = in.readUTF();
                        handleContent(response);
                        break;
                    case "#join":
                        //sth to do
                    case "#who":
                        //sth to do
                    case "#list":
                        //sth to do
                    case "#createroom":
                        //sth to do
                    case "#deleteroom":
                        //sth to do
                    case "#quit":
                        //sth to do
                    default:
                        //sth yo do
                        break;
                }

                if (command[0].equals("#quit")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
                System.out.println(packet.getIdentity() + " moves to " + packet.getRoomid());
                roomid = packet.getRoomid();
                break;
            case "roomcontents":
                System.out.println(packet.getRoomid() + " contains " + packet.getIdentities());
                break;
        }
    }
}
