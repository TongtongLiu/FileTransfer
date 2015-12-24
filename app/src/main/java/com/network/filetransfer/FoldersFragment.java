package com.network.filetransfer;

import android.app.ListFragment;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FoldersFragment extends ListFragment {
    private static final String TAG = "FolderFragments";

    final String[] from = new String[] {"name", "icon"};
    final int[] to = new int[] {R.id.text_folders_name, R.id.image_folders_icon};

    final String[] files_from = new String[] {"file_title", "path", "icon"};
    final int[] files_to = new int[] {R.id.text_all_files_title, R.id.text_all_files_path, R.id.image_all_files_icon};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folders, container,false);

        SimpleAdapter adapter = new SimpleAdapter(this.getActivity(), getFoldersList(), R.layout.listitem_folders, from, to);
        this.setListAdapter(adapter);

        return view;
    }

    public void onListItemClick(ListView parent, View view, int postion, long id) {
        //Toast.makeText(getActivity(), "You are selecting " + postion, Toast.LENGTH_SHORT).show();
        ListView listView = (ListView)parent;
        HashMap<String, Object> map = (HashMap<String, Object>)listView.getItemAtPosition(postion);
        if (map.containsKey("name")) {
            String name = map.get("name").toString();
            switch (name) {
                case "全部文件":
                    Uri uri = Uri.fromFile(new File("/sdcard"));
                    String mDir = uri.getPath();
                    SimpleAdapter all_files_adapter = new SimpleAdapter(this.getActivity(), getAllFilesList(mDir), R.layout.listitem_all_files, files_from, files_to);
                    this.setListAdapter(all_files_adapter);
                    break;
                case "图片":
                    SimpleAdapter pictures_adapter = new SimpleAdapter(this.getActivity(), getPicturesList(), R.layout.listitem_all_files, files_from, files_to);
                    this.setListAdapter(pictures_adapter);
                    break;
            }
        }
        else if (map.containsKey("file_title")) {
            String title = map.get("file_title").toString();
            String mDir = map.get("path").toString();
            if (mDir == "") {
                SimpleAdapter adapter = new SimpleAdapter(this.getActivity(), getFoldersList(), R.layout.listitem_folders, from, to);
                this.setListAdapter(adapter);
            }
            else {
                File f = new File(mDir);
                if (f.isDirectory()) {
                    SimpleAdapter adapter = new SimpleAdapter(this.getActivity(), getAllFilesList(mDir), R.layout.listitem_all_files, files_from, files_to);
                    this.setListAdapter(adapter);
                } else {
                    // send file
                }
            }
        }

    }

    private List<Map<String, Object>> getFoldersList() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map;
        List<FolderInfo> infoList = new ArrayList<>();

        Log.v(TAG, "data");
        infoList.add(new FolderInfo("全部文件", R.mipmap.ic_pc));
        infoList.add(new FolderInfo("图片", R.mipmap.ic_phone));
        infoList.add(new FolderInfo("音乐", R.mipmap.ic_phone));
        infoList.add(new FolderInfo("视频", R.mipmap.ic_phone));
        infoList.add(new FolderInfo("应用", R.mipmap.ic_phone));

        for (int i = 0; i < infoList.size(); i++) {
            map = new HashMap<>();
            FolderInfo info = infoList.get(i);
            map.put("name", info.getName());
            map.put("icon", info.getIcon());
            list.add(map);
        }

        return list;
    }

    private void getFiles(String mDir, String type, List<AllFilesInfo> infoList) {
        File[] files = new File(mDir).listFiles();
        for (int i = 0;i < files.length;i ++) {
            if (files[i].isDirectory() && files[i].getPath().indexOf("/.") == -1) {
                getFiles(files[i].getPath(), type, infoList);
            }
            else {
                switch (type) {
                    case "picture": {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(files[i].getPath(), options);
                        if (options.outWidth != -1) {
                            infoList.add(new AllFilesInfo(files[i].getName(), files[i].getPath(), R.mipmap.ic_phone));
                        }
                        break;
                    }
                }
            }
        }
    }

    private List<Map<String, Object>> getAllFilesList(String mDir) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map;
        List<AllFilesInfo> infoList = new ArrayList<>();
        File f = new File(mDir);
        File[] files = f.listFiles();

        Log.v(TAG, "data");

        if (!mDir.equals("/sdcard")) {
            infoList.add(new AllFilesInfo("Back to ../", f.getParent(), R.mipmap.ic_phone));
        }
        else {
            infoList.add(new AllFilesInfo("Back to ../", "", R.mipmap.ic_phone));
        }
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                infoList.add(new AllFilesInfo(files[i].getName(), files[i].getPath(), R.mipmap.ic_phone));
            }
        }

        for (int i = 0; i < infoList.size(); i++) {
            map = new HashMap<>();
            AllFilesInfo info = infoList.get(i);
            map.put("file_title", info.getTitle());
            map.put("path", info.getPath());
            map.put("icon", info.getIcon());
            list.add(map);
        }

        return list;
    }

    private List<Map<String, Object>> getPicturesList() {

        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map;
        List<AllFilesInfo> infoList = new ArrayList<>();
        Uri uri = Uri.fromFile(new File("/sdcard"));
        String mDir = uri.getPath();

        Log.v(TAG, "data");

        getFiles(mDir, "picture", infoList);

        for (int i = 0; i < infoList.size(); i++) {
            map = new HashMap<>();
            AllFilesInfo info = infoList.get(i);
            map.put("file_title", info.getTitle());
            map.put("path", info.getPath());
            map.put("icon", info.getIcon());
            list.add(map);
        }

        return list;
    }
}

class FolderInfo {
    private String name;
    private int icon;

    public FolderInfo(String name, int icon) {
        setName(name);
        setIcon(icon);
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
    public int getIcon() {
        return icon;
    }
}

class AllFilesInfo {
    private String title;
    private String path;
    private int icon;

    public AllFilesInfo(String title, String path, int icon) {
        setTitle(title);
        setPath(path);
        setIcon(icon);
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
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

