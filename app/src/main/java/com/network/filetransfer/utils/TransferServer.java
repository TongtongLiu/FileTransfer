package com.network.filetransfer.utils;

import android.content.Context;
import android.os.Handler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TransferServer {
    static final int PORT = 7775;
    public static ArrayList<Socket> socketList = new ArrayList<>();
    private Context context;
    private Handler handler;

    public TransferServer(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                socketList.add(socket);
                new Thread(new TransferServerThread(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
