package com.network.filetransfer.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.network.filetransfer.MainActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class BroadcastServerThread implements Runnable {
    private static final String TAG = "BroadcastServerThread";

    private DatagramSocket server;
    private DatagramPacket packet;
    private Context context;
    private Handler handler;

    public BroadcastServerThread(DatagramSocket server, DatagramPacket packet, Context context, Handler handler) {
        this.server = server;
        this.packet = packet;
        this.context = context;
        this.handler = handler;
    }

    public void run() {
        // TODO: Handle the broadcast message
        if (packet.getAddress().toString().substring(1).equals(new BroadcastMessage(context).getWiFiLocalIPAdress())) { return; }

        if (packet.getPort() != BroadcastServer.SERVER_PORT) {
            byte[] reMessage = new BroadcastMessage(context).getLocalInfoString().getBytes();
            DatagramPacket rePacket = new DatagramPacket(reMessage, reMessage.length);
            rePacket.setAddress(packet.getAddress());
            rePacket.setPort(BroadcastServer.SERVER_PORT);
            try {
                server.send(rePacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String messageStr = new String(packet.getData(), packet.getOffset(), packet.getLength());
        JSONObject info = BroadcastMessage.parseJsonString(messageStr);
        Message message = new Message();
        message.what = MainActivity.MainHandler.broadcast;
        message.obj = info;
        handler.sendMessage(message);
    }
}
