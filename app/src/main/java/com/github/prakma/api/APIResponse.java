package com.github.prakma.api;

import android.util.Log;

import okhttp3.Response;

public class APIResponse {
    private static String TAG = "APIResponse";
    private int statusCode;
    private boolean isSuccessful;
    private String body;
    private String message;

    private Exception clientEx;

    //private Response response;


    public APIResponse(Response response){
        statusCode = response.code();
        isSuccessful = response.isSuccessful();
        message = response.message();
        try {
            body = response.body().string();
        }catch (Exception ex){
            Log.e(TAG, "No Body Returned", ex);
            body = "No Body Returned: "+ex.getMessage();
        }
    }

    public APIResponse(Exception ex){
        this.clientEx = ex;

    }

    public boolean hasClientError(){
        return clientEx != null;
    }

    public boolean hasServerError(){
        return !isSuccessful;
    }

    public boolean isSuccessful(){
        return isSuccessful;
    }

    public int getStatusCode(){
        return statusCode;

    }

    public String getBody(){
        return body;

    }

    public String getMessage(){
        return message;
    }

    public Exception getClientException(){
        return clientEx;
    }
}
