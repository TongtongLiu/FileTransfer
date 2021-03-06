package com.network.filetransfer.ui;

import android.app.ListFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.network.filetransfer.R;
import com.network.filetransfer.adapters.ChoiceSimpleAdapter;
import com.network.filetransfer.utils.BluetoothUtil;
import com.network.filetransfer.utils.BroadcastClient;
import com.network.filetransfer.utils.NetworkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendsFragment extends ListFragment {
    private static final String TAG = "FriendsFragments";

    private NetworkUtil networkUtil;
    private BluetoothUtil bluetoothUtil;

    static final String[] from = new String[] {"name", "addr", "icon", "type"};
    static final int[] to = new int[] {R.id.text_friends_name, R.id.text_friends_addr, R.id.image_friends_icon, R.id.text_friends_type};
    private static List<Map<String, Object>> friendList;
    private ChoiceSimpleAdapter adapter;

    public String type;
    public String addr;
    public String name;

    private int REQUEST_DISCOVERALBLE_BT = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "OnCreate");
        super.onCreate(savedInstanceState);

        networkUtil = new NetworkUtil(getActivity());
        bluetoothUtil = MainActivity.bluetoothUtil;

        if (friendList == null) {
            friendList = new ArrayList<>();
        }
        adapter = new ChoiceSimpleAdapter(this.getActivity(), friendList, R.layout.listitem_friends, from, to);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        Log.v(TAG, "OnCreateView");
        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        if (getView() == null) {
            initFragment(view);
        }
        return view;
    }

    public void onListItemClick(ListView parent, View view, int position, long id) {
        Button send_button = (Button) getActivity().findViewById(R.id.button_send);
        type = ((TextView) view.findViewById(R.id.text_friends_type)).getText().toString();
        addr = ((TextView) view.findViewById(R.id.text_friends_addr)).getText().toString();
        name = ((TextView) view.findViewById(R.id.text_friends_name)).getText().toString();

        if (send_button == null) {
            Intent intent = new Intent(getActivity(), FoldersActivity.class);
            intent.putExtra("type", type);
            intent.putExtra("addr", addr);
            intent.putExtra("name", name);
            startActivity(intent);
            MainActivity activity = (MainActivity) getActivity();
            activity.clearChioce();
            activity.iconChange(0);
        }
        else {
            boolean isSelected = adapter.setSelectedItem(position);
            adapter.notifyDataSetInvalidated();
            if (isSelected) {
                send_button.setEnabled(true);
            }
            else {
                send_button.setEnabled(false);
                type = "";
                addr = "";
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Log.v(TAG, "OnResume");
            View view = getView();
            if (view != null) {
                initFragment(view);
            }
        } else {
            // Log.v(TAG, "OnPause");
        }
    }

    private void initFragment(View view) {
        if (view != null) {
            Log.v(TAG, "initFragment");
            LinearLayout connectionLayout = (LinearLayout) view.findViewById(R.id.layout_friends_disabled);
            final PullRefreshLayout listviewLayout = (PullRefreshLayout) view.findViewById(R.id.listview_friends);
            listviewLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Log.v(TAG, "onRefresh");
                    listviewLayout.setRefreshing(true);
                    friendList.clear();
                    if (networkUtil.isWiFiConnected()) {
                        searchFriends();
                    }
                    adapter.notifyDataSetChanged();
                    listviewLayout.setRefreshing(false);
                }
            });
            listviewLayout.setRefreshStyle(PullRefreshLayout.STYLE_RING);
            if (!networkUtil.isWiFiConnected() && !bluetoothUtil.isBluetoothEnabled()) {
                connectionLayout.setVisibility(View.VISIBLE);
                Button wifiButton = (Button) view.findViewById(R.id.button_wifi);
                wifiButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                });
                Button bluetoothButton = (Button) view.findViewById(R.id.button_bluetooth);
                bluetoothButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                        startActivityForResult(discoverableIntent, REQUEST_DISCOVERALBLE_BT);
                    }
                });
                this.setListAdapter(null);
                listviewLayout.setVisibility(View.GONE);
            }
            else {
                connectionLayout.setVisibility(View.GONE);
                this.setListAdapter(adapter);
                listviewLayout.setVisibility(View.VISIBLE);
                if (networkUtil.isWiFiConnected()) {
                    searchFriends();
                }
            }
        }
    }

    private void onActivityResult() {

    }

    private void searchFriends() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "BroadcastClient Start");
                BroadcastClient client = new BroadcastClient(getActivity());
                client.send();
            }
        }).start();
        // search bluetooth
        if (bluetoothUtil.isBluetoothEnabled()) {
            bluetoothUtil.searchBluetoothDevice();
        }
    }

    private List<Map<String, Object>> getFriendList() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map;
        List<FriendInfo> infoList = new ArrayList<>();

        infoList.add(new FriendInfo("MacBook", "192.168.0.2", R.mipmap.ic_pc_wifi, "WiFi"));
        infoList.add(new FriendInfo("iPhone", "192.168.0.3", R.mipmap.ic_phone_wifi, "WiFi"));

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

    public void addFriend(JSONObject json) {
        Log.v(TAG, "addFriendList");
        Map<String, Object> map = new HashMap<>();
        FriendInfo info = new FriendInfo(json);
        map.put("name", info.getName());
        map.put("addr", info.getAddr());
        map.put("icon", info.getIcon());
        map.put("type", info.getType());
        if (!friendList.contains(map)) {
            Log.v(TAG, "A New User");
            friendList.add(map);
        }
        adapter.notifyDataSetChanged();
    }

    // TODO: Judge whether to remove a friend
}

class FriendInfo {
    private String name;
    private String addr;
    private int icon;
    private String type;

    public FriendInfo(String name, String addr, int icon, String type) {
        setName(name);
        setAddr(addr);
        setIcon(icon);
        setType(type);
    }
    public FriendInfo(JSONObject json) {
        try {
            if (json.has("name")) { setName(json.getString("name")); }
            if (json.has("addr")) { setAddr(json.getString("addr")); }
            if (json.has("type")) {
                setType(json.getString("type"));
            }
            else {
                setType("Bluetooth");
            }
            if (json.has("icon")) {
                String iconStr = json.getString("icon");
                if (type.equals("WiFi")) {
                    if (iconStr.equals("Phone")) {
                        setIcon(R.mipmap.ic_phone_wifi);
                    }
                    else {
                        setIcon(R.mipmap.ic_pc_wifi);
                    }
                }
                else {
                    if (iconStr.equals("Phone")) {
                        setIcon(R.mipmap.ic_phone_bluetooth);
                    }
                    else {
                        setIcon(R.mipmap.ic_pc_bluetooth);
                    }
                }
            }
            else {
                setIcon(R.mipmap.ic_bluetooth);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    public void setAddr(String addr) { this.addr = addr; }
    public String getAddr() { return addr; }

    public void setIcon(int icon) { this.icon = icon; }
    public int getIcon() { return icon; }

    public void setType(String type) { this.type = type; }
    public String getType() {return type; }
}
