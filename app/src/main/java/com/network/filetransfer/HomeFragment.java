package com.network.filetransfer;

import android.app.ListFragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.baoyz.widget.PullRefreshLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends ListFragment {
    private static final String TAG = "HomeFragments";

    static final String[] from = new String[] {"origin", "name", "icon"};
    static final int[] to = new int[] {R.id.text_transfer_origin, R.id.text_transfer_name, R.id.image_transfer_icon};
    private List<Map<String, Object>> transferList;
    private SimpleAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "OnCreate");
        super.onCreate(savedInstanceState);

        transferList = new ArrayList<>();
        adapter = new SimpleAdapter(this.getActivity(), transferList, R.layout.listitem_home, from, to);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container,false);
        Log.v(TAG, "OnCreateView");
        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        if (getView() == null) {
            initFragment(view);
        }
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
            Log.v(TAG, "initFragment");
            LinearLayout transferLayout = (LinearLayout) view.findViewById(R.id.layout_home_disabled);
            final PullRefreshLayout listviewLayout = (PullRefreshLayout) view.findViewById(R.id.listview_home);
            listviewLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Log.v(TAG, "onRefresh");
                    listviewLayout.setRefreshing(true);
                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            listviewLayout.setRefreshing(false);
                        }
                    }, 3000);
                }
            });
            listviewLayout.setRefreshStyle(PullRefreshLayout.STYLE_RING);
            if (anyTransfer()) {
                transferLayout.setVisibility(View.GONE);
                this.setListAdapter(adapter);
                listviewLayout.setVisibility(View.VISIBLE);
            }
            else {
                transferLayout.setVisibility(View.VISIBLE);
                this.setListAdapter(null);
                listviewLayout.setVisibility(View.GONE);
            }
        }
    }

    //public void onListItemClick(ListView parent, View view, int postion, long id) {
    //    Toast.makeText(getActivity(), "You are selecting " + postion, Toast.LENGTH_SHORT).show();
    //}

    private boolean anyTransfer() {
        return transferList.size() > 0;
    }

    //private void getTransferList(List<Map<String, Object>> list) {
    //    Map<String, Object> map;
    //    List<TransferInfo> infoList = new ArrayList<>();
    //
    //    Log.v(TAG, "getTransferList");
    //    infoList.add(new TransferInfo("test.png", "ME-MiPhone", new Date(), R.mipmap.ic_png));
    //    infoList.add(new TransferInfo("test.doc", "Nexus CLY", new Date(), R.mipmap.ic_other));
    //
    //    for (int i = 0; i < infoList.size(); i++) {
    //        map = new HashMap<>();
    //        TransferInfo info = infoList.get(i);
    //        map.put("name", info.getName());
    //        map.put("origin", info.getOrigin());
    //        map.put("time", info.getTime());
    //        map.put("icon", info.getIcon());
    //        list.add(map);
    //    }
    //}

    public void addTransfer(JSONObject json) {
        Log.v(TAG, "addTransfer");
        Map<String, Object> map = new HashMap<>();
        TransferInfo info = new TransferInfo(json);
        map.put("name", info.getName());
        map.put("origin", info.getOrigin());
        map.put("size", info.getSize());
        map.put("transferedSize", info.getTransferedSize());
        map.put("icon", info.getIcon());
        Map<String, Object> cmpmap;
        boolean isExist = false;
        for (int i = 0; i < transferList.size(); i++) {
            cmpmap = transferList.get(i);
            if (cmpmap.get("name").equals(map.get("name")) &&
                    cmpmap.get("origin").equals(map.get("origin")) &&
                    cmpmap.get("size").equals(map.get("size"))) {
                Log.v(TAG, "An Existed Transfer");
                transferList.get(i).put("transferedSize", map.get("transferedSize"));
                isExist = true;
                break;
            }
        }
        if (!isExist) {
            Log.v(TAG, "A New Transfer");
            transferList.add(map);
        }
        adapter.notifyDataSetChanged();
        if (this.getListAdapter() == null) {
            this.setListAdapter(adapter);
            View view = getView();
            if (view != null) {
                LinearLayout transferLayout = (LinearLayout) getView().findViewById(R.id.layout_home_disabled);
                PullRefreshLayout listviewLayout = (PullRefreshLayout) getView().findViewById(R.id.listview_home);
                transferLayout.setVisibility(View.GONE);
                listviewLayout.setVisibility(View.VISIBLE);
            }
        }
    }

}

class TransferInfo {
    private String name;
    private String origin;
    private long time;
    private int icon;
    private long size;
    private long transferedSize;

    public static final long ONE_MINUTE = 60 * 1000;
    public static final long ONE_HOUR = 60 * ONE_MINUTE;
    public static final long ONE_DAY = 24 * ONE_HOUR;
    public static final long ONE_MONTH = 30 * ONE_DAY;
    public static final long ONE_YEAR = 12 * ONE_MONTH;

    public TransferInfo(String name, String origin, int icon, long size, long transferedSize) {
        setName(name);
        setOrigin(origin);
        setTime(System.currentTimeMillis());
        setIcon(icon);
        setSize(size);
        setTransferedSize(transferedSize);
    }

    public TransferInfo(JSONObject json) {
        try {
            if (json.has("name")) { setName(json.getString("name")); }
            if (json.has("origin")) { setOrigin(json.getString("origin")); }
            if (json.has("size")) { setSize(json.getLong("size")); }
            if (json.has("transferedSize")) { setTransferedSize(json.getLong("transferedSize")); }
            time = System.currentTimeMillis();
            icon = R.mipmap.ic_other;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    public void setOrigin(String origin) { this.origin = origin; }
    public String getOrigin() { return origin; }

    public void setTime(long time) { this.time = time; }
    public String getTime() {
        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - time;
        long timeIntoFormat;
        String timeString;

        if (timePassed < ONE_MINUTE) {
            timeString = "Now";
        } else if (timePassed < ONE_HOUR) {
            timeIntoFormat = timePassed / ONE_MINUTE;
            timeString = timeIntoFormat + "分钟前";
        } else if (timePassed < ONE_DAY) {
            timeIntoFormat = timePassed / ONE_HOUR;
            timeString = timeIntoFormat + "小时前";
        } else if (timePassed < ONE_MONTH) {
            timeIntoFormat = timePassed / ONE_DAY;
            timeString = timeIntoFormat + "天前";
        } else if (timePassed < ONE_YEAR) {
            timeIntoFormat = timePassed / ONE_MONTH;
            timeString = timeIntoFormat + "个月前";
        } else {
            timeIntoFormat = timePassed / ONE_YEAR;
            timeString = timeIntoFormat + "年前";
        }
        return timeString;
    }

    public void setIcon(int icon) { this.icon = icon; }
    public int getIcon() { return icon; }

    public void setSize(long size) { this.size = size; }
    public long getSize() { return size; }

    public void setTransferedSize(long transferedSize) { this.transferedSize = transferedSize; }
    public long getTransferedSize() { return transferedSize; }
}
