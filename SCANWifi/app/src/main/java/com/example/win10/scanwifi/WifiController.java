package com.example.win10.scanwifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import static android.content.Context.WIFI_SERVICE;

/**
 * wifi 自动连接控制类
 * Created by win 10 on 2018/9/10.
 */

public class WifiController {

    private Context context;
    private WifiManager wm;

    public WifiController(Context context){
        //获取wifiManager服务
        this.context = context;
        wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
    }

    public void openWifi() {
        int status = wm.getWifiState();
        if (status == WifiManager.WIFI_STATE_DISABLED) {
            wm.setWifiEnabled(true);
        }
    }

    /**
     * 枚举类型
     */
    public enum WifiCipherType {
        WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }

    /**
     * 按照type创建符合标准的config
     * @param SSID
     * @param Password
     * @param Type
     * @return
     */
    public WifiConfiguration createWifiInfo(String SSID, String Password,
                                             WifiCipherType Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        // nopass
        if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        // wep
        if (Type == WifiCipherType.WIFICIPHER_WEP) {
            if (!TextUtils.isEmpty(Password)) {
                if (isHexWepKey(Password)) {
                    config.wepKeys[0] = Password;
                } else {
                    config.wepKeys[0] = "\"" + Password + "\"";
                }
            }
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        // wpa
        if (Type == WifiCipherType.WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            // 此处需要修改否则不能自动重联
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        Log.d("zerujus", "config " + config.toString());
        return config;
    }

    private static boolean isHexWepKey(String wepKey) {
        final int len = wepKey.length();

        // WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
        if (len != 10 && len != 26 && len != 58) {
            return false;
        }

        return isHex(wepKey);
    }

    private static boolean isHex(String key) {
        for (int i = key.length() - 1; i >= 0; i--) {
            final char c = key.charAt(i);
            if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a'
                    && c <= 'f')) {
                return false;
            }
        }

        return true;
    }

    /**
     * 连接连接
     */
    public int connect(WifiConfiguration config) {
        synchronized (this) {
            if (config != null) {
                wm.removeNetwork(config.networkId);
            }

            int netID = wm.addNetwork(config);
            boolean connStatus = wm.enableNetwork(netID, true);
            Log.d("zerujus", "connStatus: " + connStatus);

            int status = wm.getWifiState();
            Log.d("zerujus", "status: " + status);
            if (status == WifiManager.WIFI_STATE_ENABLED) {
                return netID;
            }

            return -1;
        }
    }

    /**
     * 断开连接
     * @param netID
     */
    public void disconnect(int netID){
        wm.disconnect();
        wm.removeNetwork(netID);
        Log.d("zerujus", "disconnect");
    }
}
