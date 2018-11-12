package com.example.satyam.opustry;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.security.SecureRandom;

public class DialActivity extends AppCompatActivity implements ResponseHandler{
    private static final String TAG = "DIAL_ACTIVITY";
    private static final int REQ_TEST = 1;
    private static final int REQ_IP_KEY = 2;
    public static boolean CALLING = false;
    public static boolean TRANSFERING = false;

    private EditText dialIp;
    private Button dial;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dial);
        initView();
        startCallService(this);
        new GETRequest(this,REQ_TEST).execute("http://www.google.com");
    }

    private void initView() {
        dial = findViewById(R.id.dial);
        dialIp = findViewById(R.id.dial_ip);
        dial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Todo API CALL Get public key and ip addr of the peer
                //Todo Add public key to intent
                startCallActivity(dialIp.getText().toString());
            }
        });
    }
    private void startCallService(Context context) {
        Log.v(TAG,"Attempting to start service from activity");
        Intent i= new Intent(context, CallService.class);
        i.putExtra("KEY1", "Value to be used by the service");
        context.startService(i);
    }
    private void startCallActivity(String socketAddr) {

        Intent intent = new Intent(this,CallActivity.class);
        /*intent.putExtra("userId",userId);
        intent.putExtra("name",name);
        intent.putExtra("action", DialActivity.INCOMING_CALL);
        intent.putExtra("photoUrl", photo);*/
        byte[] key = new byte[16];
        new SecureRandom().nextBytes(key);
        intent.putExtra("key",key);
        intent.putExtra("incoming",false);

        intent.putExtra("peer",socketAddr);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onResponse(String response, int reqCode) {
        switch (reqCode)
        {
            case REQ_TEST:
            {
                Log.d(TAG,response);
                break;
            }
            case REQ_IP_KEY:
            {
                //TOdo Start Call activity by putting ip address and public key and own emailaddress in intent
            }
        }
    }

    @Override
    public void onError(int reqCode) {
        switch (reqCode)
        {
            case REQ_TEST:
                Log.d(TAG,"REQ_TEST");
        }
    }
}
