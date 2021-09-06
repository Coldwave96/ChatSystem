package com.kvoli;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientThread implements Runnable {
    private Socket s;
    BufferedReader br = null;

    public ClientThread(Socket s) throws IOException {
        this.s = s;
        br = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    public void run() {
//        DataInputStream in = null;
//        try {
//            in = new DataInputStream(s.getInputStream());
//            System.out.println(in.readUTF());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            String content = null;

            while ((content = br.readLine()) != null) {
                System.out.println(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
