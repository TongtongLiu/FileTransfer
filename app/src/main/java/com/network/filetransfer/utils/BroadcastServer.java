package com.network.filetransfer.utils;

import android.content.Context;
import android.os.Handler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class BroadcastServer {
    static final int SERVER_PORT = 7777;
    private Context context;
    private Handler handler;

    public BroadcastServer(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    public void start() {
        try {
            DatagramSocket server = new DatagramSocket(SERVER_PORT);
            while (true) {
                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                server.receive(packet);
                new Thread(new BroadcastServerThread(server, packet, context, handler)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
