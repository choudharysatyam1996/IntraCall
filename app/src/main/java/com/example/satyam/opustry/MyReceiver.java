package com.example.satyam.opustry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import java.io.IOException;

import static android.content.Context.WIFI_SERVICE;

public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = "Receiver";

    private static String getWifiIpaddressByWifiManager(Context ctxApp) {
        WifiManager wifiMgr = (WifiManager) ctxApp.getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        String ipAddress = Formatter.formatIpAddress(ip);
        return ipAddress;
    }

    private void updateIpAddress(Context ctx) {
        try {
            GETRequest.callApi(Config.SERVER_IP_ADDRESS+Config.API_SAVE_IP+"?mailid="+Config.getEmail(ctx)+"&ipaddress"+getWifiIpaddressByWifiManager(ctx));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.v(TAG,"Boot completed intent Received");
        Intent i= new Intent(context, CallService.class);
        i.putExtra("KEY1", "Value to be used by the service");
        context.startService(i);
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateIpAddress(context);
            }
        }).start();
    }
}
