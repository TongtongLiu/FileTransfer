package com.network.filetransfer.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;

public class BroadcastMessage {
    private Context context;

    public BroadcastMessage(Context context) {
        this.context = context;
    }

    public static String getLocalHostIP() {
        String ip;
        try {
            /**返回本地主机。*/
            InetAddress addr = InetAddress.getLocalHost();
            /**返回 IP 地址字符串（以文本表现形式）*/
            ip = addr.getHostAddress();
        } catch (Exception ex) {
            ip = "";
        }
        return ip;
    }

    public static String getLocalHostName() {
        String hostName;
        try {
            /**返回本地主机。*/
            InetAddress addr = InetAddress.getLocalHost();
            /**获取此 IP 地址的主机名。*/
            hostName = addr.getHostName();
        } catch (Exception ex) {
            hostName = "";
        }
        return hostName;
    }

    public String getWiFiLocalIPAdress() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return formatIpAddress(ipAddress);
    }

    private static String formatIpAddress(int ipAddress) {
        return (ipAddress & 0xFF ) + "." +
                ((ipAddress >> 8 ) & 0xFF) + "." +
                ((ipAddress >> 16 ) & 0xFF) + "." +
                ( ipAddress >> 24 & 0xFF) ;
    }

    public static String getDeviceName() {
        return android.os.Build.MODEL;
    }

    public JSONObject getLocalInfo() {
        try {
            JSONObject json = new JSONObject();
            json.put("addr", getWiFiLocalIPAdress());
            json.put("name", getDeviceName());
            json.put("type", "Phone");
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getLocalInfoString() {
        JSONObject json = getLocalInfo();
        if (json != null) {
            return json.toString();
        }
        else {
            return "";
        }
    }

    public static JSONObject parseJsonString(String str) {
        try {
            JSONObject json = new JSONObject(str);
            //String ip = json.getString("addr");
            //String name = json.getString("name");
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
