package com.network.filetransfer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainHandler extends Handler {
    private static final String TAG = "MainHandler";

    public final static int broadcast = 0x1;
    public final static int bluetooth_search = 0x2;

    private final WeakReference<MainActivity> mActivity;
    private List<FriendsFragment> friendsFragmentList;

    public MainHandler(MainActivity activity) {
        mActivity = new WeakReference<>(activity);
        friendsFragmentList = new ArrayList<>();
    }

    public void addFriendsFragment(FriendsFragment fragment) {
        friendsFragmentList.add(fragment);
    }

    public void removeFriendsFragment(FriendsFragment fragment) {
        friendsFragmentList.remove(fragment);
    }

    @Override
    public void handleMessage(Message message) {
        // TODO: handle message from server thread
        Log.v(TAG, message.what + " " + message.obj.toString());
        switch (message.what) {
            case broadcast:
            case bluetooth_search:
                FriendsFragment fragment;
                for (int i = 0; i < friendsFragmentList.size(); i++) {
                    fragment = friendsFragmentList.get(i);
                    fragment.addFriend((JSONObject) message.obj);
                }
                break;
            default:
                break;
        }
    }
}