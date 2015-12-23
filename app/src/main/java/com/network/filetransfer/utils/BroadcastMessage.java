package com.network.filetransfer.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;

public class BroadcastMessage {
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

    public static JSONObject getLocalInfo() {
        try {
            JSONObject json = new JSONObject();
            json.put("addr", getLocalHostIP());
            json.put("name", getLocalHostName());
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getLocalInfoString() {
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
