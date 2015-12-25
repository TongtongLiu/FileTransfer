package com.network.filetransfer.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.network.filetransfer.MainActivity;
import com.network.filetransfer.MainHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class BluetoothUtil {
    private Context context;
    private BluetoothAdapter adapter;
    private BluetoothManager manager;
    private Handler handler;
    private UUID mmUUID;

    // Create a BroadcastReceiver for ACTION_FOUND
    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", device.getName());
                    jsonObject.put("addr", device.getAddress());
                    jsonObject.put("type", "Bluetooth");
                    Message message = new Message();
                    message.what = MainHandler.bluetooth_search;
                    message.obj = jsonObject;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public BluetoothUtil(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        adapter = BluetoothAdapter.getDefaultAdapter();
        manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        String uuid = "4bbd4690-ab36-4ed2-9a8e-40723b1790c3";
        mmUUID = UUID.fromString(uuid);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
    }

    public boolean isBluetoothEnabled() {
        return (adapter != null && adapter.isEnabled());
    }

    public void searchBluetoothDevice() {
        adapter.cancelDiscovery();
        adapter.startDiscovery();
    }

    public void destroy() {
        context.unregisterReceiver(mReceiver);
    }

    public void openServer() {
        AcceptThread acceptThread = new AcceptThread();
        acceptThread.start();
    }

    public void sendFile(String MAC_addr, String file) {
        BluetoothDevice device = adapter.getRemoteDevice(MAC_addr);
        ConnectThread connectThread = new ConnectThread(device, file);
        connectThread.start();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                String NAME = Build.MODEL;
                tmp = adapter.listenUsingRfcommWithServiceRecord(NAME, mmUUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    BluetoothDevice device = socket.getRemoteDevice();
                    BluetoothReceiveFile bluetoothReceiveFile = new BluetoothReceiveFile(socket, device);
                    bluetoothReceiveFile.start();
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String file;

        public ConnectThread(BluetoothDevice device, String file) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            this.file = file;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(mmUUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            adapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            }
            catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            BluetoothSendFile bluetoothSendFile = new BluetoothSendFile(mmSocket, file);
            bluetoothSendFile.start();
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private class BluetoothSendFile extends Thread {
        private static final String TAG = "BlueToothSendFile";

        private final BluetoothSocket mmSocket;
        private BufferedReader in;
        private BufferedWriter out;

        private long fileSize;
        private long transferredSize;
        private String deviceName;
        private String fileName;
        private String filePath;
        int CACHE = 4096;

        public BluetoothSendFile(BluetoothSocket socket, String file) {
            mmSocket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            File f = new File(file);
            deviceName = socket.getRemoteDevice().getName();
            fileName = f.getName();
            fileSize = f.length();
            filePath = file;
        }

        public void run() {
            try {
                send(fileName);
                send("" + fileSize);
                transferredSize = 0;
                updateUI(deviceName, fileName, fileSize, transferredSize);

                // Send File Content
                InputStream filein = new BufferedInputStream(new FileInputStream(filePath), 8 * 1024 * 1024);
                OutputStream fileout = new DataOutputStream(new BufferedOutputStream(mmSocket.getOutputStream()));
                byte[] buffer = new byte[CACHE];
                int count = filein.read(buffer, 0, CACHE);
                int rate = Integer.parseInt(PreferenceUtil.getPrefString(context, "settings_transfer_rate_key", "256"));
                int inteval = 1000 / (rate * 1024 / CACHE);
                while (count >= 0) {
                    fileout.write(buffer, 0, count);
                    fileout.flush();
                    transferredSize += count;
                    Thread.sleep(inteval);
                    updateUI(deviceName, fileName, fileSize, transferredSize);
                    count = filein.read(buffer);
                }
                filein.close();
                fileout.close();
                //mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
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

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private class BluetoothReceiveFile extends Thread {
        private static final String TAG = "BluetoothReceiveFile";

        private final BluetoothSocket mmSocket;
        private BufferedReader in;
        private BufferedWriter out;

        private String fileName;
        private long fileSize;
        private long tranferredSize;
        private String deviceName;
        int CACHE = 4096;
        private String reply;

        public BluetoothReceiveFile(BluetoothSocket socket, BluetoothDevice device){
            mmSocket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            deviceName = device.getName();
        }

        public void run() {
            try {
                fileName = receive();
                fileSize = Long.parseLong(receive());
                tranferredSize = 0;
                updateUI(deviceName, fileName, fileSize, tranferredSize);
                // Read File Content
                String saved_file = Environment.getExternalStorageDirectory().getPath() + File.separator + "Download" + File.separator + fileName;
                Log.v(TAG, saved_file);
                InputStream filein = new DataInputStream(new BufferedInputStream(mmSocket.getInputStream()));
                OutputStream fileout = new BufferedOutputStream(new FileOutputStream(saved_file));
                byte[] buffer = new byte[TransferClient.CACHE];
                int count = filein.read(buffer, 0, TransferClient.CACHE);
                while (count >= 0) {
                    fileout.write(buffer, 0, count);
                    fileout.flush();
                    tranferredSize += count;
                    updateUI(deviceName, fileName, fileSize, tranferredSize);
                    count = filein.read(buffer);
                }
                filein.close();
                fileout.close();
                mmSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
                try {
                    mmSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            openServer();
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


        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
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
            message.what = MainHandler.bluetooth_receivefile;
            message.obj = json;
            handler.sendMessage(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}

