package com.example.satyam.opustry;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Config {
    public static final String SERVER_IP_ADDRESS = "http://172.17.74.70:8080/voipCALL/webapi/mailService/";
    public static final String API_SAVE_IP = "saveIpAddress";
    public static final String API_REGISTER_USER = "sendMail";
    public static final String API_GET_KEY_IP = "getKeyAndIp";//?mailid
    public static final String API_VERIFY_OTP = "verifyotp";//?mailid
    public static String getEmail(Context ctx)
    {
        SharedPreferences preferences = ctx.getSharedPreferences("settings",Context.MODE_PRIVATE);
        return preferences.getString("email","satyamp@iitk.ac.in");
    }
    public static String getSharedPrefernce(Context ctx,String key, String def)
    {
        SharedPreferences preferences = ctx.getSharedPreferences("settings",Context.MODE_PRIVATE);
        return preferences.getString(key,def);
    }
    public static void setSharedPrefernce(Context ctx,String key, String val)
    {
        SharedPreferences preferences = ctx.getSharedPreferences("settings",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key,val);
        editor.commit();
    }

    public static String getPubKey(Context ctx)
    {
        SharedPreferences preferences = ctx.getSharedPreferences("settings",Context.MODE_PRIVATE);
        if(preferences.contains("pubkey"))
        {
            return preferences.getString("pubkey","");
        }
        else
        {
            KeyPair pair = RSAEncryption.generateKeyPair();
            PrivateKey prvkey = pair.getPrivate();
            PublicKey pubkey = pair.getPublic();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("pubkey",Base64.encodeToString(pubkey.getEncoded(),Base64.DEFAULT));
            editor.putString("prvkey",Base64.encodeToString(prvkey.getEncoded(),Base64.DEFAULT));
            editor.commit();
        }
        return preferences.getString("pubkey","satyamp@iitk.ac.in");
    }
}
