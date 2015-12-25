package com.network.filetransfer;

import android.app.ListFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoldersFragment extends ListFragment {
    private static final String TAG = "FolderFragments";
    
    final String[] from = new String[] {"name", "path", "icon"};
    final int[] to = new int[] {R.id.text_folders_name, R.id.text_folders_path, R.id.image_folders_icon};
    List<Map<String, Object>> fileList;
    MySimpleAdapter adapter;

    public String file;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folders, container, false);

        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        
        fileList = new ArrayList<>();
        String mDir = Uri.fromFile(new File("/sdcard")).getPath();
        getFoldersList(mDir, fileList);
        adapter = new MySimpleAdapter(this.getActivity(), fileList, R.layout.listitem_folders, from, to);
        this.setListAdapter(adapter);

        return view;
    }

    public void onListItemClick(ListView parent, View view, int position, long id) {
        ListView listView = (ListView)parent;
        HashMap<String, Object> map = (HashMap<String, Object>)listView.getItemAtPosition(position);
        file = map.get("path").toString();
        File f = new File(file);
        Button send_button = (Button) getActivity().findViewById(R.id.button_send);

        if (f.isDirectory()) {
            if (send_button != null) {
                send_button.setEnabled(false);
            }
            getFoldersList(f.getPath(), fileList);
            adapter.clearSelectedItem();
            file = "";
            adapter.notifyDataSetChanged();
        }
        else {
            if (send_button == null) {
                Intent intent = new Intent(getActivity(), FriendsActivity.class);
                intent.putExtra("file", file);
                startActivity(intent);
            }
            else {
                boolean isSelected = adapter.setSelectedItem(position);
                adapter.notifyDataSetInvalidated();
                if (isSelected) {
                    send_button.setEnabled(true);
                }
                else {
                    send_button.setEnabled(false);
                    file = "";
                }
            }
        }
    }

    private void getFoldersList(String mDir, List<Map<String, Object>> fileList) {
        File f = new File(mDir);
        File[] files = f.listFiles();
        Map<String, Object> map;
        List<FolderInfo> infoList = new ArrayList<>();

        Log.v(TAG, "data");
        if (!mDir.equals("/sdcard")) {
            infoList.add(new FolderInfo("Back to ../", f.getParent(), R.mipmap.ic_folder));
        }
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    infoList.add(new FolderInfo(files[i].getName(), files[i].getPath(), R.mipmap.ic_folder));
                }
                else {
                    infoList.add(new FolderInfo(files[i].getName(), files[i].getPath(), R.mipmap.ic_file));
                }
            }
        }

        fileList.clear();
        for (int i = 0; i < infoList.size(); i++) {
            map = new HashMap<>();
            FolderInfo info = infoList.get(i);
            map.put("name", info.getName());
            map.put("path", info.getPath());
            map.put("icon", info.getIcon());
            fileList.add(map);
        }

    }
}

class FolderInfo {
    private String name;
    private String path;
    private int icon;

    public FolderInfo(String name, String path, int icon) {
        setName(name);
        setIcon(icon);
        setPath(path);
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setPath(String path) {
        this.path = path;
    }
    public String getPath() {
        return path;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
    public int getIcon() {
        return icon;
    }
}
