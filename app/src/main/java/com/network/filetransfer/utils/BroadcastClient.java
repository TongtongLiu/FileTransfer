package com.network.filetransfer.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class BroadcastClient {
    static final int PORT = 7777;

    public void send() {
        try {
            DatagramSocket socket = new DatagramSocket(PORT);
            InetAddress inetAddr = InetAddress.getByName("255.255.255.255");
            byte[] message = BroadcastMessage.getLocalInfoString().getBytes();
            DatagramPacket packet = new DatagramPacket(message, message.length, inetAddr, PORT);
            socket.send(packet);
        }  catch (IOException e) {
            e.printStackTrace();
        }
    }
}
