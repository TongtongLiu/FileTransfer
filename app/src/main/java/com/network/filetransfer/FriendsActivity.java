package com.network.filetransfer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FriendsActivity extends Activity {
    private String type;
    private String addr;
    private String file;
    private FriendsFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        Button backButton = (Button) findViewById(R.id.button_back);
        Button sendButton = (Button) findViewById(R.id.button_send);
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
            // TODO send type addr file
        }
    }
}

