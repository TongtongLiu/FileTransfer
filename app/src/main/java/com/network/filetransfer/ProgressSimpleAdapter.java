package com.network.filetransfer;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import java.util.List;
import java.util.Map;

public class ProgressSimpleAdapter extends SimpleAdapter {
    List<Map<String, Object>> list;

    public ProgressSimpleAdapter(Context context, List<? extends Map<String, ?>> data,
                                 int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        list = (List<Map<String, Object>>) data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View localView = super.getView(position, convertView, parent);
        ProgressBar progressBar = (ProgressBar) localView.findViewById(R.id.bar_transfer);
        Map<String, Object> map = list.get(position);
        progressBar.setMax(((Long) map.get("size")).intValue());
        progressBar.setProgress(((Long) map.get("transferedSize")).intValue());
        return localView;
    }
}
