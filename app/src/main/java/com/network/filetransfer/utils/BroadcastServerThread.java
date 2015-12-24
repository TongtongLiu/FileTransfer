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
import java.net.InetAddress;

public class BroadcastServerThread implements Runnable {
    private static final String TAG = "BroadcastServerThread";

    private DatagramPacket packet;
    private Context context;
    private Handler handler;

    public BroadcastServerThread(DatagramPacket packet, Context context, Handler handler) {
        this.packet = packet;
        this.context = context;
        this.handler = handler;
    }

    public void run() {
        // TODO: Handle the broadcast message
        Log.v(TAG, "Receive A Broadcast Message");
        if (packet.getAddress().toString().substring(1).equals(new BroadcastMessage(context).getWiFiLocalIPAdress())) { return; }

        if (packet.getPort() == BroadcastClient.CLIENT_PORT) {
            DatagramSocket socket = null;
            try {
                byte[] reMessage = new BroadcastMessage(context).getLocalInfoString().getBytes();
                DatagramPacket rePacket = new DatagramPacket(reMessage, reMessage.length);
                rePacket.setAddress(packet.getAddress());
                rePacket.setPort(BroadcastServer.SERVER_PORT);
                socket = new DatagramSocket();
                socket.send(rePacket);
                Log.v(TAG, "Reply To Broadcast Message");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                if (socket != null) {
                    socket.close();
                }
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
