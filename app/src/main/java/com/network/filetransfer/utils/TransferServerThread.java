package com.network.filetransfer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class TransferServerThread implements Runnable {
    private Socket socket = null;
    private BufferedReader br = null;

    public TransferServerThread(Socket socket) throws IOException {
        this.socket = socket;
        br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
    }

    public void run() {
        try {
            String content = null;
            while ((content = readFromClient()) != null) {
                // TODO: Handle the transfer from client
                for (Socket s : TransferServer.socketList) {
                    OutputStream os = s.getOutputStream();
                    os.write((content + "\n").getBytes("utf-8"));
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readFromClient() {
        try {
            return br.readLine();
        }
        catch (IOException e) {
            TransferServer.socketList.remove(socket);
        }
        return null;
    }
}
