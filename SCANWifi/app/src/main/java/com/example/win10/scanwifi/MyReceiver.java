package com.example.win10.scanwifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {

    private int error;

    public MyReceiver(){

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        if (intent.getIntExtra("supplicantError", 0) == WifiManager.ERROR_AUTHENTICATING) {
            MainActivity.num++;
            Log.d("zerujus", "ERROR_AUTHENTICATING");
        }

        Log.d("zerujus", "广播有用");
    }
}
