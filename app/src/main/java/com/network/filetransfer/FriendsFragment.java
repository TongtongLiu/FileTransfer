package com.network.filetransfer;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.network.filetransfer.utils.BluetoothUtil;
import com.network.filetransfer.utils.NetworkUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendsFragment extends ListFragment {
    private static final String TAG = "FriendsFragments";

    private NetworkUtil networkUtil;
    private BluetoothUtil bluetoothUtil;

    final String[] from = new String[] {"name", "addr", "icon"};
    final int[] to = new int[] {R.id.text_friends_name, R.id.text_friends_addr, R.id.image_friends_icon};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "OnCreate");
        super.onCreate(savedInstanceState);

        networkUtil = new NetworkUtil(getActivity());
        bluetoothUtil = new BluetoothUtil(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        Log.v(TAG, "OnCreateView");
        initFragment(view);
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Log.v(TAG, "OnResume");
            initFragment(getView());
        } else {
            // Log.v(TAG, "OnPause");
        }
    }

    private void initFragment(View view) {
        if (view != null) {
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.layout_friends_disabled);
            if (!networkUtil.isWiFiConnected() && !bluetoothUtil.isBluetoothEnabled()) {
                layout.setVisibility(View.VISIBLE);
                this.setListAdapter(null);
                Button wifiButton = (Button) view.findViewById(R.id.button_wifi);
                wifiButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                });
                // TODO: bluetoothButton OnClick Event
            }
            else {
                layout.setVisibility(View.GONE);
                SimpleAdapter adapter = new SimpleAdapter(this.getActivity(), getFriendList(), R.layout.listitem_friends, from, to);
                this.setListAdapter(adapter);
            }
        }
    }

    public void onListItemClick(ListView parent, View view, int postion, long id) {
        Toast.makeText(getActivity(), "You are selecting " + postion, Toast.LENGTH_SHORT).show();
        // TODO: Click a friend and redirect to FoldFragment.
    }

    private List<Map<String, Object>> getFriendList() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map;
        List<FriendInfo> infoList = new ArrayList<>();

        Log.v(TAG, "getFriendList");
        infoList.add(new FriendInfo("MacBook", "192.168.0.2", R.mipmap.ic_pc));
        infoList.add(new FriendInfo("iPhone", "192.168.0.3", R.mipmap.ic_phone));

        for (int i = 0; i < infoList.size(); i++) {
            map = new HashMap<>();
            FriendInfo info = infoList.get(i);
            map.put("name", info.getName());
            map.put("addr", info.getAddr());
            map.put("icon", info.getIcon());
            list.add(map);
        }

        return list;
    }
}

class FriendInfo {
    private String name;
    private String addr;
    private int icon;

    public FriendInfo(String name, String addr, int icon) {
        setName(name);
        setAddr(addr);
        setIcon(icon);
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }
    public String getAddr() {
        return addr;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
    public int getIcon() {
        return icon;
    }
}
