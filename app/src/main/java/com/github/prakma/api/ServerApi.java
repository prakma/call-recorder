package com.github.prakma.api;

import android.net.Uri;
import android.util.Base64;
import android.util.Log;

//import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServerApi extends ServerApiBase {
    private static String TAG = "ServerApi";


    private static String REGISTRATION_URI_CONTEXT = "device-token/register";
    private static String UPLOAD_URI_CONTEXT = "call/upload-recording";

    private static ServerApi _instance = new ServerApi();


    private ServerApi(){}

    public static ServerApi getInstance(){
        return _instance;
    }


    public boolean sendNoRecording2Server() {

        Log.i(TAG, "nothing to send to server");
        return true;
    }


    public boolean sendFile2Server(Uri uri, String callRefId, String tenant,
                                   String leapUri, boolean ignoreSSL) {
        return sendFile2Server(new File(uri.getPath()), callRefId, tenant,
                leapUri, ignoreSSL);
    }

    public boolean sendFile2Server(File anyFile, String callRefId, String tenant,
                                   String leapUriBase, boolean ignoreSSLFlag) {

        Log.i(TAG, "post recording to server");

        String leapUri = leapUriBase + UPLOAD_URI_CONTEXT;
        String hmacContent = tenant;
        String contentMd5 = calculateMD5(hmacContent);
        String hmac = calculateHMAC(SECRET_KEY, contentMd5);
        String accessKey = "dfkjnfr39dfdfgsd"; // doesn't matter. backend doesn't do anything with it
        String hmacHeader = String.format(HMAC_HEADER_FORMAT, accessKey, hmac);

        MediaType MEDIA_TYPE_AUDIO_OGG = MediaType.parse("audio/ogg; codecs=vorbis");
        OkHttpClient client = getNewOkHttpClient(ignoreSSLFlag);
        Log.i(TAG, "For CallRefId "+ callRefId+" file name to be uploaded as "+anyFile.getName());

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("taskId", callRefId)
                .addFormDataPart("file", anyFile.getName(),
                        RequestBody.create(MEDIA_TYPE_AUDIO_OGG, anyFile))
                .build();
        Request request = new Request.Builder()
                .url(leapUri)
                .addHeader(HMAC_HEADER, hmacHeader)
                .addHeader(TENANT_HEADER, tenant)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String result = response.body().string();

            System.out.println(result);
            Log.i(TAG, "server response is "+result);
            response.close();

            return true;
        }catch (Exception ex){
            Log.e(TAG,"Call to Server failed - " + ex.getMessage(), ex);
            return false;
        }

    }

    public APIResponse registerDeviceWithLeapServer(String userEmail, String deviceFirebaseToken,
                                                String tenant, String leapUriBase, boolean ignoreSSLFlag){


        OkHttpClient client = getNewOkHttpClient(ignoreSSLFlag);
        String leapUri = leapUriBase + REGISTRATION_URI_CONTEXT;
        String hmacContent = tenant;
        String contentMd5 = calculateMD5(hmacContent);
        String hmac = calculateHMAC(SECRET_KEY, contentMd5);
        String accessKey = "dfkjnfr39dfdfgsd"; // doesn't matter. backend doesn't do anything with it
        String hmacHeader = String.format(HMAC_HEADER_FORMAT, accessKey, hmac);

        RequestBody formBody = new FormBody.Builder()
                .add("email", userEmail)
                .add("token", deviceFirebaseToken)
                .build();
        Request request = new Request.Builder()
                .url(leapUri)
                .addHeader(HMAC_HEADER, hmacHeader)
                .addHeader(TENANT_HEADER, tenant)
                .post(formBody)
                .build();

        Log.i(TAG, " invoking server API "+leapUri+" for device registration "+ deviceFirebaseToken );
        try (Response response = client.newCall(request).execute()) {

            return new APIResponse(response);
        }catch(Exception ex){
            //throw new RuntimeException("Problem invoking the registration API", ex);
            Log.e(TAG, "Server Registration API "+leapUri+" invocation failed", ex);
            return new APIResponse(ex);
        }

    }
}
