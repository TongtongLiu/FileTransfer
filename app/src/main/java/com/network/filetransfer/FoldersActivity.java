package com.network.filetransfer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FoldersActivity extends Activity {
    private String type;
    private String addr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Button backButton = (Button) findViewById(R.id.folders_button_back);
        Button sendButton = (Button) findViewById(R.id.folders_button_send);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folders);
        type = getIntent().getStringExtra("type");
        addr = getIntent().getStringExtra("addr");

        backButton.setOnClickListener(new BackClickListener());
        sendButton.setOnClickListener(new SendClickListener());
    }

    class BackClickListener implements View.OnClickListener {
        public void onClick(View v) {
            finish();
        }
    }

    class SendClickListener implements View.OnClickListener {
        public void onClick(View v) {

        }
    }
}

