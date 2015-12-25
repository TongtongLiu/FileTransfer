package com.network.filetransfer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.network.filetransfer.utils.TransferClient;

public class FriendsActivity extends Activity {
    private static final String TAG = "FriendsActivity";

    private String type;
    private String addr;
    private String name;
    private String file;
    private FriendsFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        Button backButton = (Button) findViewById(R.id.button_back);
        Button sendButton = (Button) findViewById(R.id.button_send);
        sendButton.setEnabled(false);
        file = getIntent().getStringExtra("file");
        backButton.setOnClickListener(new BackClickListener());
        sendButton.setOnClickListener(new SendClickListener());

        fragment = new FriendsFragment();
        getFragmentManager().beginTransaction().replace(R.id.friends_fragment, fragment).commit();
        MainActivity.mainHandler.addFriendsFragment(fragment);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MainActivity.mainHandler.removeFriendsFragment(fragment);
    }

    class BackClickListener implements View.OnClickListener {
        public void onClick(View v) {
            finish();
        }
    }

    class SendClickListener implements View.OnClickListener {
        public void onClick(View v) {
            type = fragment.type;
            addr = fragment.addr;
            name = fragment.name;
            if (type.equals("WiFi")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.v(TAG, "TransferClient Start");
                        TransferClient client = new TransferClient(fragment.getActivity(), addr, name, file, MainActivity.mainHandler);
                        client.run();
                    }
                }).start();
            }
            else {
                MainActivity.bluetoothUtil.sendFile(addr, file);
            }
            finish();
        }
    }
}

