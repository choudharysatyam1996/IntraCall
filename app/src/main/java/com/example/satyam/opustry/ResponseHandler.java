package com.example.satyam.opustry;

public interface ResponseHandler {
    void onResponse(String response,int reqCode);
    void onError(int reqCode);
}
