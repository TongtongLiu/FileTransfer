package com.network.filetransfer.utils;

import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.network.filetransfer.MainHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class TransferServerThread implements Runnable {
    private static final String TAG = "TransferServerThread";

    private Socket server;
    private Handler handler;
    private BufferedReader in;
    private BufferedWriter out;
    private String reply;

    public TransferServerThread(Socket server, Handler handler) {
        this.server = server;
        this.handler = handler;
    }

    private void send(String request) {
        try {
            Log.v(TAG, request);
            out.write(request + "\r\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String receive() {
        try {
            reply = in.readLine();
            Log.v(TAG, reply);
            return reply;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void updateUI(String name, String file, long size, long transferedSize) {
        try {
            JSONObject json = new JSONObject();
            json.put("origin", name);
            json.put("name", file);
            json.put("size", size);
            json.put("transferedSize", transferedSize);
            Message message = new Message();
            message.what = MainHandler.transfer_receive;
            message.obj = json;
            handler.sendMessage(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));

            String name = receive();
            String file = receive();
            long size = Long.parseLong(receive());
            updateUI(name, file, size, 0);

            String saved_file = Uri.fromFile(new File("/sdcard")).getPath() + File.separator + "Download" + File.separator + file;
            Log.v(TAG, saved_file);

            InputStream filein = new DataInputStream(new BufferedInputStream(server.getInputStream()));
            OutputStream fileout = new BufferedOutputStream(new FileOutputStream(file));
            byte[] buffer = new byte[TransferClient.CACHE];
            int count = filein.read(buffer, 0, TransferClient.CACHE);
            int total = 0;
            while (count >= 0) {
                fileout.write(buffer, 0, count);
                fileout.flush();
                total += count;
                updateUI(name, file, size, total);
                count = filein.read(buffer);
            }
            filein.close();
            fileout.close();

            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
