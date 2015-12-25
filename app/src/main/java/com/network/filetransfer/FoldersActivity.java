package com.network.filetransfer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FoldersActivity extends Activity {
    private String type;
    private String addr;
    private String file;
    private FoldersFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folders);

        Button backButton = (Button) findViewById(R.id.button_back);
        Button sendButton = (Button) findViewById(R.id.button_send);
        type = getIntent().getStringExtra("type");
        addr = getIntent().getStringExtra("addr");
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
        }
    }
}

