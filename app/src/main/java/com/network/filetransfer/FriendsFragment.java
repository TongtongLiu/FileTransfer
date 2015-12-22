package com.network.filetransfer;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        if (!networkUtil.isWiFiConnected() && !bluetoothUtil.isBluetoothEnabled()) {
            view = inflater.inflate(R.layout.fragment_friends_disabled, container, false);
        }
        else {
            view = inflater.inflate(R.layout.fragment_friends, container, false);
            SimpleAdapter adapter = new SimpleAdapter(this.getActivity(), getFriendsList(), R.layout.listitem_friends, from, to);
            this.setListAdapter(adapter);
        }
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        networkUtil = new NetworkUtil(getActivity());
        bluetoothUtil = new BluetoothUtil(getActivity());
    }

    public void onListItemClick(ListView parent, View view, int postion, long id) {
        Toast.makeText(getActivity(), "You are selecting " + postion, Toast.LENGTH_SHORT).show();
    }

    private List<Map<String, Object>> getFriendsList() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map;
        List<FriendInfo> infoList = new ArrayList<>();

        Log.v(TAG, "data");
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
