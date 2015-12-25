package com.network.filetransfer.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import com.network.filetransfer.R;

import java.util.List;
import java.util.Map;

public class ChoiceSimpleAdapter extends SimpleAdapter {
    private int selectItem = -1;

    public ChoiceSimpleAdapter(Context context, List<? extends Map<String, ?>> data,
                               int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
    }

    public boolean setSelectedItem(int selectItem) {
        if (this.selectItem == selectItem) {
            this.selectItem = -1;
            return false;
        }
        else {
            this.selectItem = selectItem;
            return true;
        }
    }

    public void clearSelectedItem() {
        selectItem = -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View localView = super.getView(position, convertView, parent);
        if (position == selectItem) {
            //localView.setBackgroundColor(parent.getResources().getColor(R.color.selected_listitem));
            localView.findViewById(R.id.image_selected).setVisibility(View.VISIBLE);
        }
        else {
            //localView.setBackgroundColor(Color.TRANSPARENT);
            localView.findViewById(R.id.image_selected).setVisibility(View.INVISIBLE);
        }
        return localView;
    }
}
