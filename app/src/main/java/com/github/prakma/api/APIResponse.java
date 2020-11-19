package com.github.prakma.api;

import android.util.Log;

import org.json.JSONObject;

import okhttp3.Response;

public class APIResponse {
    private static String TAG = "prakma.APIResponse";
    private int statusCode;
    private boolean isSuccessful;
    private String body;
    //private String message;

    private Exception clientEx;

    //private Response response;


    public APIResponse(Response response){
        statusCode = response.code();
        isSuccessful = response.isSuccessful();
        //message = response.message();
        try {
            body = response.body().string();
            //JSONObject responseJson = new JSONObject(body);
            //responseJson.
        }catch (Exception ex){
            Log.e(TAG, "No Body Returned", ex);
            body = "No Body Returned: "+ex.getMessage();
        }
    }

    public APIResponse(Exception ex){
        this.clientEx = ex;
        isSuccessful = false;

    }

    private APIResponse(String str){
        statusCode = 200;
        body = str;
        isSuccessful = true;
    }

    public static APIResponse createSimpleSuccessResponse(String str){
        return new APIResponse(str);
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

//    public String getMessage(){
//        return message;
//    }

    public Exception getClientException(){
        return clientEx;
    }
}
