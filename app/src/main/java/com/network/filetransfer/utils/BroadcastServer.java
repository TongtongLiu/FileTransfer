package com.network.filetransfer.utils;

import android.os.Handler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class BroadcastServer {
    static final int PORT = 7777;
    private Handler handler;

    public BroadcastServer(Handler handler) {
        this.handler = handler;
    }

    public void start() {
        try {
            DatagramSocket server = new DatagramSocket(PORT);
            while (true) {
                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                server.receive(packet);
                new Thread(new BroadcastServerThread(server, packet, handler)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
