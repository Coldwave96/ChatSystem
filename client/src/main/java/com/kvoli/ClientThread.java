package com.kvoli;

import java.io.*;
import java.net.Socket;

public class ClientThread implements Runnable {
    private Socket s;

    public ClientThread(Socket s) throws IOException {
        this.s = s;

        DataInputStream inputStream = new DataInputStream(s.getInputStream());
        System.out.println(inputStream.readUTF());

        String content = inputStream.readLine();
        while (content != null) {
            System.out.println(content);
            content = inputStream.readLine();
        }
    }

    public void run() {
    }
}
