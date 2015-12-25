package com.network.filetransfer.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.network.filetransfer.ui.MainHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class TransferClient {
    private static final String TAG = "TransferClient";
    static int CACHE = 4096;

    private Context context;
    private String addr;
    private String name;
    private String file;
    private Handler handler;

    private Socket client;
    private BufferedReader in;
    private BufferedWriter out;
    private String reply;

    public TransferClient(Context context, String addr, String name, String file, Handler handler) {
        this.context = context;
        this.addr = addr;
        this.name = name;
        this.file = file;
        this.handler = handler;
    }

    private void connect() {
        try {
            Log.v(TAG, "Connect Start");
            client = new Socket(addr, TransferServer.PORT);
            Log.v(TAG, "Connect End");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            message.what = MainHandler.transfer_send;
            message.obj = json;
            handler.sendMessage(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            connect();
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

            String origin = BluetoothAdapter.getDefaultAdapter().getName();
            File fp = new File(file);
            send(origin);
            send(fp.getName());
            send("" + fp.length());
            updateUI(name, fp.getName(), fp.length(), 0);

            InputStream filein = new BufferedInputStream(new FileInputStream(file), 8 * 1024 * 1024);
            OutputStream fileout = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()));
            byte[] buffer = new byte[CACHE];
            int count = filein.read(buffer, 0, CACHE);
            int total = 0;
            int rate = Integer.parseInt(PreferenceUtil.getPrefString(context, "settings_transfer_rate_key", "256"));
            int inteval = 1000 / (rate * 1024 / CACHE);
            while (count >= 0) {
                fileout.write(buffer, 0, count);
                fileout.flush();
                total += count;
                Thread.sleep(inteval);

                updateUI(name, fp.getName(), fp.length(), total);

                count = filein.read(buffer);
            }
            filein.close();
            fileout.close();

            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
