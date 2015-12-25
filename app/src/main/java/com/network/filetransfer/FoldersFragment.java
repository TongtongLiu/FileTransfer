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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoldersFragment extends ListFragment {
    private static final String TAG = "FolderFragments";

    private int oldPosition = -1;
    
    final String[] from = new String[] {"name", "path", "icon"};
    final int[] to = new int[] {R.id.text_folders_name, R.id.text_folders_path, R.id.image_folders_icon};
    List<Map<String, Object>> fileList;
    SimpleAdapter adapter;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folders, container, false);

        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        
        fileList = new ArrayList<>();
        String mDir = Uri.fromFile(new File("/sdcard")).getPath();
        getFoldersList(mDir, fileList);
        adapter = new SimpleAdapter(this.getActivity(), fileList, R.layout.listitem_folders, from, to);
        this.setListAdapter(adapter);

        return view;
    }

    public void onListItemClick(ListView parent, View view, int position, long id) {
        ListView listView = (ListView)parent;
        HashMap<String, Object> map = (HashMap<String, Object>)listView.getItemAtPosition(position);
        String mDir = map.get("path").toString();
        File f = new File(mDir);
        if (f.isDirectory()) {
            getFoldersList(f.getPath(), fileList);
            adapter.notifyDataSetChanged();
        } else {
            // ========================================================================
            if (getActivity().findViewById(R.id.button_send) == null) {
                Intent intent = new Intent(getActivity(), FriendsActivity.class);
                intent.putExtra("file", mDir);
                startActivity(intent);
            }
            else {
                ImageView imageView;
                if (oldPosition >= 0) {
                    View itemView = parent.getChildAt(oldPosition);
                    imageView = (ImageView) itemView.findViewById(R.id.image_folders_selected);
                    imageView.setVisibility(View.INVISIBLE);
                }
                if (oldPosition != position) {
                    imageView = (ImageView) view.findViewById(R.id.image_folders_selected);
                    imageView.setVisibility(View.VISIBLE);
                    oldPosition = position;
                } else {
                    oldPosition = -1;
                }
            }
            // ========================================================================
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
