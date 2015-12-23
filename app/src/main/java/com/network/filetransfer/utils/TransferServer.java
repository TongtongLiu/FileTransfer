package com.network.filetransfer.utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TransferServer {
    private static final int PORT = 7776;
    public static ArrayList<Socket> socketList = new ArrayList<>();

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        while (true) {
            Socket socket = serverSocket.accept();
            socketList.add(socket);
            new Thread(new TransferServerThread(socket)).start();
        }
    }
}
