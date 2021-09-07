package com.kvoli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.kvoli.base.Packet;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientThread implements Runnable {
    private Socket s;
    private String id;
    private String roomid;

    public ClientThread(Socket s) throws IOException {
        this.s = s;
        DataInputStream inputStream = new DataInputStream(s.getInputStream());

        String content = null;
        while (!(content = inputStream.readUTF()).equals("done")) {
            handleContent(content);
        }
        inputStream.close();
    }

    public void run() {
        try {
            DataInputStream in = new DataInputStream(s.getInputStream());
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            Scanner kb = new Scanner(System.in);
            while (true) {
                System.out.printf("[%s] %s>", roomid, id);
                String input = kb.nextLine();
                String[] command = input.split(" ");

                switch (command[0]) {
                    case "#identitychange":
                        //sth to do
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
                roomid = packet.getRoomid();
                break;
            case "roomcontents":
                System.out.println(packet.getRoomid() + " contains " + packet.getIdentities());
                break;
        }
    }
}
