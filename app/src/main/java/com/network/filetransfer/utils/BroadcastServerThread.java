package com.network.filetransfer.utils;

import android.os.Handler;
import android.os.Message;

import com.network.filetransfer.MainActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class BroadcastServerThread implements Runnable {
    private DatagramSocket server;
    private DatagramPacket packet;
    private Handler handler;

    public BroadcastServerThread(DatagramSocket server, DatagramPacket packet, Handler handler) {
        this.server = server;
        this.packet = packet;
        this.handler = handler;
    }

    public void run() {
        // TODO: Handle the broadcast message
        byte[] reMessage = BroadcastMessage.getLocalInfoString().getBytes();
        DatagramPacket rePacket = new DatagramPacket(reMessage, reMessage.length);
        rePacket.setAddress(packet.getAddress());
        rePacket.setPort(packet.getPort());
        try {
            server.send(rePacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String messageStr = new String(packet.getData(), packet.getOffset(), packet.getLength());
        JSONObject info = BroadcastMessage.parseJsonString(messageStr);
        Message message = new Message();
        message.what = MainActivity.MainHandler.broadcast;
        message.obj = info;
        handler.sendMessage(message);
    }
}
