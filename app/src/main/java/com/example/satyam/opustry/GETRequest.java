package com.example.satyam.opustry;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GETRequest extends AsyncTask<String , Void ,String> {
    private static final String TAG = "APICALL";
    private String server_response;
    private ResponseHandler resHandler;
    private int reqCode;

    public GETRequest(ResponseHandler responseHandler,int requestCode)
    {
        resHandler=responseHandler;
        reqCode = requestCode;
    }
    @Override
    protected String doInBackground(String... strings) {
        try {
            server_response = callApi(strings[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return server_response;
    }

    public static String callApi(String urlStr) throws IOException {
        String response;
        URL url;
        HttpURLConnection urlConnection = null;
        url = new URL(urlStr);
        Log.d(TAG,"calling url : "+urlStr);
        urlConnection = (HttpURLConnection) url.openConnection();
        int responseCode = urlConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            response = readStream(urlConnection.getInputStream());
            Log.v("CatalogClient", response);
            return response;
        }
        else
        {
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(s!=null)
            resHandler.onResponse(s,reqCode);
        else
            resHandler.onError(reqCode);
        Log.e("Response", "" + server_response);
    }

    private static String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }
}