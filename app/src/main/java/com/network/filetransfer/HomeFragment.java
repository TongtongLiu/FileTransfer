package com.network.filetransfer;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends ListFragment {
    private static final String TAG = "HomeFragments";

    final String[] from = new String[] {"origin", "time", "name", "icon"};
    final int[] to = new int[] {R.id.text_transfer_origin, R.id.text_transfer_time,
                                R.id.text_transfer_name, R.id.image_transfer_icon};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "OnCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container,false);
        Log.v(TAG, "OnCreateView");


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
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.layout_home_disabled);
            if (anyTransfer()) {
                layout.setVisibility(View.GONE);
                SimpleAdapter adapter = new SimpleAdapter(this.getActivity(), getTransferList(), R.layout.listitem_home, from, to);
                this.setListAdapter(adapter);
            }
            else {
                layout.setVisibility(View.VISIBLE);
                this.setListAdapter(null);
            }
        }
    }

//    public void onListItemClick(ListView parent, View view, int postion, long id) {
//        Toast.makeText(getActivity(), "You are selecting " + postion, Toast.LENGTH_SHORT).show();
//    }

    private boolean anyTransfer() {
        return false;
    }

    private List<Map<String, Object>> getTransferList() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map;
        List<TransferInfo> infoList = new ArrayList<>();

        Log.v(TAG, "getTransferList");
        infoList.add(new TransferInfo("test.png", "MacBook", new Date(), R.mipmap.ic_png));
        infoList.add(new TransferInfo("test.doc", "iPhone", new Date(), R.mipmap.ic_other));

        for (int i = 0; i < infoList.size(); i++) {
            map = new HashMap<>();
            TransferInfo info = infoList.get(i);
            map.put("name", info.getName());
            map.put("origin", info.getOrigin());
            map.put("time", info.getTime());
            map.put("icon", info.getIcon());
            list.add(map);
        }

        return list;
    }
}

class TransferInfo {
    private String name;
    private String origin;
    private Date time;
    private int icon;

    public TransferInfo(String name, String origin, Date time, int icon) {
        setName(name);
        setOrigin(origin);
        setTime(time);
        setIcon(icon);
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }
    public String getOrigin() {
        return origin;
    }

    public void setTime(Date time) {
        this.time = time;
    }
    public String getTime() {
        return "Now";
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
    public int getIcon() {
        return icon;
    }
}
