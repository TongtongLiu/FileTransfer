package com.network.filetransfer.utils;

import android.content.Context;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class BroadcastClient {
    static final int CLIENT_PORT = 7776;
    private Context context;

    public BroadcastClient(Context context) {
        this.context = context;
    }

    public void send() {
        try {
            DatagramSocket socket = new DatagramSocket(CLIENT_PORT);
            InetAddress inetAddr = InetAddress.getByName("255.255.255.255");
            byte[] message = new BroadcastMessage(context).getLocalInfoString().getBytes();
            DatagramPacket packet = new DatagramPacket(message, message.length, inetAddr, BroadcastServer.SERVER_PORT);
            socket.send(packet);
            socket.close();
        }  catch (IOException e) {
            e.printStackTrace();
        }
    }
}
