package com.network.filetransfer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.network.filetransfer.utils.TransferClient;

public class FoldersActivity extends Activity {
    private static final String TAG = "FoldersActivity";

    private String type;
    private String addr;
    private String name;
    private String file;
    private FoldersFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folders);

        Button backButton = (Button) findViewById(R.id.button_back);
        Button sendButton = (Button) findViewById(R.id.button_send);
        sendButton.setEnabled(false);
        type = getIntent().getStringExtra("type");
        addr = getIntent().getStringExtra("addr");
        name = getIntent().getStringExtra("name");

        backButton.setOnClickListener(new BackClickListener());
        sendButton.setOnClickListener(new SendClickListener());

        fragment = new FoldersFragment();
        getFragmentManager().beginTransaction().replace(R.id.folders_fragment, fragment).commit();
    }

    class BackClickListener implements View.OnClickListener {
        public void onClick(View v) {
            finish();
        }
    }

    class SendClickListener implements View.OnClickListener {
        public void onClick(View v) {
            file = fragment.file;
            // TODO send type addr file
            if (type.equals("Bluetooth")) {
                MainActivity.bluetoothUtil.sendFile(addr, file);
            }
            else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.v(TAG, "TransferClient Start");
                        TransferClient client = new TransferClient(fragment.getActivity(), addr, name, file, MainActivity.mainHandler);
                        client.run();
                    }
                }).start();
            }
            finish();
        }
    }
}

