package com.network.filetransfer.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.widget.ArrayAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothUtil {
    private Context context;
    private BluetoothAdapter adapter;
    private BluetoothManager manager;
    private ArrayAdapter<JSONObject> arrayAdapter;

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", device.getName());
                    jsonObject.put("addr", device.getAddress());
                    jsonObject.put("type", "Bluetooth");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public BluetoothUtil(Context context) {
        this.context = context;
        adapter = BluetoothAdapter.getDefaultAdapter();
        manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

    }

    public boolean isBluetoothEnabled() {
        return (adapter != null && adapter.isEnabled());
    }

    public boolean isBluetoothSupported() {return (adapter != null); }

    public void searchBluetoothDevice() {
        if (adapter.startDiscovery()) {

        }
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
    }

    public void queryPairedDevice() {
        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", device.getName());
                    jsonObject.put("addr", device.getAddress());
                    jsonObject.put("type", "Bluetooth");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ArrayAdapter<JSONObject> getArrayAdapter() {return arrayAdapter; }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                UUID MY_UUID =  UUID.randomUUID();
                String NAME = Build.MODEL;
                tmp = adapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
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
                    BluetoothReceiveFile bluetoothReceiveFile = new BluetoothReceiveFile(socket);
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

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                UUID MY_UUID =  UUID.randomUUID();
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
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
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            File file = new File("1.txt");
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

        public BluetoothSendFile(BluetoothSocket socket, File file) {
            mmSocket = socket;
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
            mmfile = file;
        }

        public void run() {
            byte[] fileBytes = new byte[1024];  // buffer store for the stream
            try {
                fileBytes = fullyReadFileToBytes(mmfile); // bytes returned from read()
                write(fileBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public byte[] fullyReadFileToBytes(File file) throws IOException {
            int size = (int) file.length();
            byte bytes[] = new byte[size];
            byte tmpBuff[] = new byte[size];
            FileInputStream fs = new FileInputStream(file);
            try {
                int read = fs.read(bytes, 0, size);
                if (read < size) {
                    int remain = size - read;
                    while (remain > 0) {
                        read = fs.read(tmpBuff, 0, remain);
                        System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                        remain -= read;
                    }
                }
            }  catch (IOException e){
                throw e;
            }
            finally {
                fs.close();
            }
            return bytes;
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
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

        public BluetoothReceiveFile(BluetoothSocket socket) {
            mmSocket = socket;
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
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                } catch (IOException e) {
                    break;
                }
            }

        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

}

