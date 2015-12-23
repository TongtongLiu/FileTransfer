package com.network.filetransfer.utils;

import android.os.Handler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TransferServer {
    static final int PORT = 7776;
    public static ArrayList<Socket> socketList = new ArrayList<>();
    private Handler handler;

    public TransferServer(Handler handler) {
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
