package com.kvoli;

import java.io.*;
import java.net.Socket;

public class ClientThread implements Runnable {
    private Socket s;

    public ClientThread(Socket s) throws IOException {
        this.s = s;
        DataInputStream inputStream = new DataInputStream(s.getInputStream());

        String content = inputStream.readUTF();
        while (true) {
            System.out.println(content);
            content = inputStream.readUTF();
        }
    }

    public void run() {
    }
}
