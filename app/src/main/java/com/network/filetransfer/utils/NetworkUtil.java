package com.network.filetransfer.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class NetworkUtil {
    private Context context;
    private ConnectivityManager connectivityManager;
    private WifiManager wifiManager;

    public NetworkUtil(Context context) {
        this.context = context;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public boolean isNetworkConnected() {
        NetworkInfo[] infos = connectivityManager.getAllNetworkInfo();
        if (infos != null) {
            for (NetworkInfo info : infos) {
                if (info.isConnected()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isWiFiConnected() {
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return (info != null && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public boolean isMobileConnected() {
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }
}
