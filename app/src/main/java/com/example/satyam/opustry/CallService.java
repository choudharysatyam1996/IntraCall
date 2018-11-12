package com.example.satyam.opustry;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CallService extends Service {

    public static final int CONTROL_PORT = 7313;
    private static final String TAG = "CallService";
    private ServerSocket controlSocket;
    private Thread listenerThread;
    private PowerManager pm;
    private PowerManager.WakeLock wl;
    private WifiManager.WifiLock wifiLock;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"Oncreate called!");
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CallService:wakeLock");
        if(!wl.isHeld())
            wl.acquire();
        Log.d(TAG,"WakeLock Acquired");

        WifiManager wMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiLock = wMgr.createWifiLock(WifiManager.WIFI_MODE_FULL, "MyWifiLock");
        if(!wifiLock.isHeld())
            wifiLock.acquire();
        
        if(listenerThread == null || !listenerThread.isAlive() || listenerThread.isInterrupted()) {
            listenerThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        String TAG = listenerThread.getName();
                        Log.d(TAG,"Thread started");
                        controlSocket = new ServerSocket(CONTROL_PORT);
                        Socket socket = null;
                        while (!listenerThread.isInterrupted()) {
                            Log.d(TAG,"Listening for connection");
                            socket = controlSocket.accept();
                            Log.d(TAG,"Connection accepted from "+socket.getRemoteSocketAddress().toString());
                            //String rcvd = new String(CallActivity.readPackedData(socket.getInputStream()));
                            //Todo Rececive his email address and get its public key & ipaddress
                            //Todo receive encrypted key and decrypt it with own public key
                            //todo decrypt this key with senders public key

                            byte[] key = CallActivity.readPackedData(socket.getInputStream());
                            CallActivity.packAndWriteData(socket.getOutputStream(),"Hi i got the key".getBytes());
                            startCallActivity(socket.getInetAddress().getHostAddress(),key);
                            socket.close();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                        e.printStackTrace();
                    }
                }
            });
            listenerThread.setName("CALL_LISTENER");
            listenerThread.start();
        }
    }

    private void startCallActivity(String socketAddr, byte[] key) {
        Intent intent = new Intent(this,CallActivity.class);
        /*intent.putExtra("userId",userId);
        intent.putExtra("name",name);
        intent.putExtra("action", DialActivity.INCOMING_CALL);
        intent.putExtra("photoUrl", photo);*/
        intent.putExtra("key",key);
        intent.putExtra("peer",socketAddr);
        intent.putExtra("incoming",true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"Attempting to start service");
        if(flags == Service.START_FLAG_REDELIVERY) {
            Log.d(TAG,"Service Re-Started");
        }
        else {
            Log.d(TAG, "Service Started");
        }
        Toast.makeText(this,"Call Service started", Toast.LENGTH_LONG).show();
        return Service.START_REDELIVER_INTENT;
    }

}
