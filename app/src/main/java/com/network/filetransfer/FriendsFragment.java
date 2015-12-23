package com.network.filetransfer;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import com.baoyz.widget.PullRefreshLayout;
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

    static final String[] from = new String[] {"name", "addr", "icon"};
    static final int[] to = new int[] {R.id.text_friends_name, R.id.text_friends_addr, R.id.image_friends_icon};
    private List<Map<String, Object>> friendList;
    private SimpleAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "OnCreate");
        super.onCreate(savedInstanceState);

        networkUtil = new NetworkUtil(getActivity());
        bluetoothUtil = new BluetoothUtil(getActivity());

        friendList = new ArrayList<>();
        adapter = new SimpleAdapter(this.getActivity(), friendList, R.layout.listitem_friends, from, to);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        Log.v(TAG, "OnCreateView");
        if (getView() == null) {
            initFragment(view);
            searchFriends();
        }
        return view;
    }

    public void onListItemClick(ListView parent, View view, int postion, long id) {
        Toast.makeText(getActivity(), "You are selecting " + postion, Toast.LENGTH_SHORT).show();
        // TODO: Click a friend and redirect to FoldFragment.
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Log.v(TAG, "OnResume");
            initFragment(getView());
            searchFriends();
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
                    //(new Handler()).postDelayed(new Runnable() {
                    //    @Override
                    //    public void run() {
                    //        listviewLayout.setRefreshing(false);
                    //    }
                    //}, 3000);
                    friendList.clear();
                    searchFriends();
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
                // TODO: bluetoothButton OnClick Event

                this.setListAdapter(null);
                listviewLayout.setVisibility(View.GONE);
            }
            else {
                connectionLayout.setVisibility(View.GONE);
                this.setListAdapter(adapter);
                listviewLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void searchFriends() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "BroadcastClient Start");
                BroadcastClient client = new BroadcastClient();
                client.send();
            }
        });
    }

    private List<Map<String, Object>> getFriendList() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map;
        List<FriendInfo> infoList = new ArrayList<>();

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

    public void addFriend(JSONObject json) {
        Log.v(TAG, "addFriendList");
        Map<String, Object> map = new HashMap<>();
        FriendInfo info = new FriendInfo(json);
        map.put("name", info.getName());
        map.put("addr", info.getAddr());
        map.put("icon", info.getIcon());
        friendList.add(map);
        adapter.notifyDataSetChanged();
    }

    // TODO: Judge whether to remove a friend
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
    public FriendInfo(JSONObject json) {
        try {
            if (json.has("name")) { setName(json.getString("name")); }
            if (json.has("addr")) { setName(json.getString("addr")); }
            // TODO: Judge pc or phone
            setIcon(R.mipmap.ic_pc);
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
}
