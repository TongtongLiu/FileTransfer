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
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.network.filetransfer.MainActivity;
import com.network.filetransfer.MainHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", file);
            jsonObject.put("origin", device.getName());
            jsonObject.put("time", new Date());
            jsonObject.put("type", "Bluetooth");
            Message message = new Message();
            message.what = MainHandler.bluetooth_sendfile;
            message.obj = jsonObject;
            handler.sendMessage(message);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
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
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final File mmfile;

        public BluetoothSendFile(BluetoothSocket socket, String file) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mmfile = new File(file);
        }

        public void run() {
            try {
                // Send File Name
                byte[] nameBuffer = new byte[4096];
                byte[] fileName = mmfile.getName().getBytes();
                for (int i = 0;i < fileName.length;i ++) {
                    nameBuffer[i] = fileName[i];
                }
                mmOutStream.write(nameBuffer);
                mmOutStream.flush();
                // Send File Content
                FileInputStream filein = new FileInputStream(mmfile);
                DataInputStream fileInput = new DataInputStream(new BufferedInputStream(filein));
                byte[] buffer = new byte[4096];
                int read = 0;
                while ((read = fileInput.read(buffer)) != -1)
                {
                    mmOutStream.write(buffer, 0, read);
                }
                mmOutStream.flush();
                fileInput.close();
                mmSocket.close();
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
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private File file;
        private BluetoothDevice device;

        public BluetoothReceiveFile(BluetoothSocket socket, BluetoothDevice device) {
            mmSocket = socket;
            this.device = device;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[4096];
            int read = 0;
            try {
                read = mmInStream.read(buffer);
                byte[] nameBuffer = new byte[read];
                for (int i = 0;i < read;i ++) {
                    nameBuffer[i] = buffer[i];
                }
                String fileName = new String(nameBuffer);
                String path = context.getExternalFilesDir(null).toString();
                String filePath = path + File.separator + fileName;
                file = new File(path, fileName);
                file.createNewFile();
                DataOutputStream fileOutput = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(filePath))));
                while ((read = mmInStream.read(buffer)) != -1)
                {
                    fileOutput.write(buffer, 0, read);
                }
                fileOutput.flush();
                fileOutput.close();
                mmSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", file);
                jsonObject.put("origin", device.getName());
                jsonObject.put("time", new Date());
                jsonObject.put("type", "Bluetooth");
                Message message = new Message();
                message.what = MainHandler.bluetooth_sendfile;
                message.obj = jsonObject;
                handler.sendMessage(message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            openServer();
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

}

