package com.kvoli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.kvoli.base.Packet;

import java.io.*;
import java.net.Socket;

public class ClientThread implements Runnable {
    private Socket s;
    private String id;

    public ClientThread(Socket s) throws IOException {
        this.s = s;
        DataInputStream inputStream = new DataInputStream(s.getInputStream());

        while (true) {
            String content = inputStream.readUTF();
            handleContent(content);
        }
    }

    public void run() {
    }

    public void handleContent(String content) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Packet packet = mapper.readValue(content, Packet.class);

        switch (packet.getType()) {
            case "newidentity":
                System.out.println("Connect to " + s.getRemoteSocketAddress() + " as " + packet.getIdentity());
                id = packet.getIdentity();
                break;
            case "roomlist":
                for (String room : packet.getRooms().keySet()) {
                    System.out.println(room + ": " + packet.getRooms().get(room) + " guests");
                }
                break;
            case "roomchange":
                System.out.println(packet.getIdentity() + " moves to " + packet.getRoomid());
                break;
            case "roomcontents":
                System.out.println(packet.getRoomid() + " contains " + packet.getIdentities());
                break;
        }
    }
}
