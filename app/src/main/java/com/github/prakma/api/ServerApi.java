package com.github.prakma.api;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

//import org.apache.commons.codec.binary.Base64;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bugfender.sdk.Bugfender;
import com.github.axet.callrecorder.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServerApi extends ServerApiBase {
    private static String TAG = "prakma.ServerApi";


    //private static String REGISTRATION_URI_CONTEXT = "device-token/register";
    //private static String UPLOAD_URI_CONTEXT = "call/upload-recording";

    public static final String REGISTRATION_URI_CONTEXT = "REGISTRATION_URI_CONTEXT";
    public static final String UPLOAD_URI_CONTEXT = "UPLOAD_URI_CONTEXT";
    public static final String UPLOAD_NOTIFICATION_URI_CONTEXT = "UPLOAD_NOTIFICATION_URI_CONTEXT";

    private static ServerApi _instance = new ServerApi();


    private ServerApi(){}

    public static ServerApi getInstance(){
        return _instance;
    }


    public boolean sendNoRecording2Server() {

        Log.i(TAG, "nothing to send to server");
        return true;
    }





    public APIResponse registerDeviceWithLeapServer(String userEmail, String secret, String deviceFirebaseToken,
                                                String tenant, String dlrCode,
                                                    String leapUriBase, boolean ignoreSSLFlag){


        OkHttpClient client = getNewOkHttpClient(ignoreSSLFlag);
        String registration_uri_context = BuildConfig.PropertyPairs.get(REGISTRATION_URI_CONTEXT);
        String leapUri = leapUriBase + registration_uri_context+"?tenant="+tenant;

        //String leapUri = "https://mptest1.free.beeceptor.com/" + "REGISTRATION_URI_CONTEXT";
        //String leapUri = "https://c7a17154.ngrok.io/" + "REGISTRATION_URI_CONTEXT";
        String hmacContent = tenant;
        String contentMd5 = calculateMD5(hmacContent);
        String secretKey = BuildConfig.PropertyPairs.get(LEAP_API_SECRET);
        String hmac = calculateHMAC(secretKey, contentMd5);
        String accessKey = "dfkjnfr39dfdfgsd"; // doesn't matter. backend doesn't do anything with it
        String hmacHeader = String.format(HMAC_HEADER_FORMAT, accessKey, hmac);

        // get some diagnostic information as well
        String mfr = Build.MANUFACTURER;
        String model = Build.MODEL;
        String product = Build.PRODUCT;
        String androidVersion = Build.VERSION.RELEASE;
        String androidVersionCode = Build.VERSION.SDK_INT+"";
        String artemisCallerVersionCd = BuildConfig.VERSION_CODE+"";
        String artemisCallerVersion = BuildConfig.VERSION_NAME;

        String phoneMakeModel = mfr+"-"+model+"-"+product;
        // PhoneMakeModel, AndroidVersion , AndroidVersionCode, ArtemisCallerAppVersion

        RequestBody formBody = new FormBody.Builder()
                .add("email", userEmail)
                .add("token", deviceFirebaseToken)
                .add("loginEmailOrId", userEmail)
                .add("password", secret)
                .add("PhoneMakeModel",phoneMakeModel)
                .add("AndroidVersion",androidVersion)
                .add("AndroidVersionCode",androidVersionCode)
                .add("ArtemisCallerAppVersion",artemisCallerVersion)
                .build();
        Request request = new Request.Builder()
                .url(leapUri)
                .addHeader(HMAC_HEADER, hmacHeader)
                .addHeader(TENANT_HEADER, tenant)
                .post(formBody)
                .build();

        Log.i(TAG, " invoking server API "+leapUri+" for device registration "+ deviceFirebaseToken );
        Bugfender.d(TAG, "Leap Registration URI "+ leapUri + "TENANT-"+ tenant  + " DlrCode-"+dlrCode+
                "device id "+deviceFirebaseToken);
        //Bugfender.d(TAG, "TENANT-"+ tenant  + " DlrCode-"+dlrCode );
        try (Response response = client.newCall(request).execute()) {

            APIResponse apiResponse = new APIResponse(response);
            response.close();
            return apiResponse;
        }catch(Exception ex){
            //throw new RuntimeException("Problem invoking the registration API", ex);
            Log.e(TAG, "Server Registration API "+leapUri+" invocation failed", ex);
            Bugfender.e(TAG, "Server Registration API "+leapUri+" invocation failed");
            return new APIResponse(ex);
        }

    }

    public APIResponse sendFile2Server(File anyFile, String callRefId, String vin_no,
                                   String call, String phone, String call_duration, String call_start_ts,
                                   String tenant, String dlrCode, String creCode,
                                   Context ctx, String leapUriBase, boolean ignoreSSLFlag,
                                   String s3Id, String s3Region, String s3Bucket, String s3Folder,
                                   String s3BaseURL, String deviceToken) {

        // todo - use S3 upload our our own backend , by reading some configuration
        //boolean use_s3_flag = ignoreSSLFlag == false;
        boolean use_s3_flag = true;


        APIResponse apiResponse = null;


        if(use_s3_flag) {
            // try s3
            apiResponse = sendToS3(ctx, anyFile, callRefId, vin_no,
                    call, phone, call_duration, call_start_ts, tenant, dlrCode, creCode,
                    leapUriBase, ignoreSSLFlag,
                    s3Id, s3Region, s3Bucket, s3Folder,
                    s3BaseURL, deviceToken);
        }else {
            boolean result = send2CustomBackend_v2( anyFile, callRefId, vin_no,
                    call, phone, call_duration, call_start_ts,
                    tenant, dlrCode, creCode, leapUriBase, ignoreSSLFlag);
            if (result) {
                apiResponse = APIResponse.createSimpleSuccessResponse("Success");
            } else {
                apiResponse = new APIResponse(new Exception("Uploading file to custom backend was not successful"));
            }

        }

        return apiResponse;

    }

    private APIResponse sendToS3(Context ctx, File anyFile,
                             String callRefId, String vin_no,
                             String call, String phone, String call_duration, String call_start_ts,
                             String tenant, String dlrCode, String creCode,
                             String leapUriBase, boolean ignoreSSLFlag,
                             String s3Id, String s3Region, String s3Bucket, String s3Folder,
                             String s3BaseURL, String deviceToken){

        Log.i(TAG, "Using S3 backend to post recording file");
        S3UploadClient s3UploadClient = S3UploadClient.getInstance();


        APIResponse apiResponse = null;

        Object lock = new Object(); // used to block this method until s3 upload returns

        S3ResponseCallback callback = createS3Callback(callRefId, vin_no, call,
                phone, call_duration, call_start_ts, tenant, dlrCode, creCode,
                leapUriBase, ignoreSSLFlag, lock);

        String s3URL = s3UploadClient.uploadtos3(ctx, anyFile, callRefId, vin_no, call,
                phone, call_duration, call_start_ts, tenant, dlrCode, creCode,
                s3Id, s3Region, s3Bucket, s3Folder, s3BaseURL, callback);
        try{
            // todo - block the call, watch for the s3 response before returning the result
            Log.i(TAG, "Halting for S3 Upload");
            synchronized (lock){
                lock.wait(300000); // 5 minute max wait
            }
            Log.i(TAG, "Woken up by S3 Upload Callback");

        }catch(InterruptedException ex){
            // interrupted. proceed as upload to S3 failed
            Log.i(TAG, "Upload to S3 wait interrupted.", ex);
            return new APIResponse(new Exception("Failed during upload to S3 - wait was interrupted"));
        }

        // notify the server
        if(! callback.isError()){
            apiResponse = sendUploadNotification2Backend(s3URL,
                    callRefId, vin_no, call, phone, call_duration, call_start_ts, tenant,
                    dlrCode, creCode, leapUriBase, ignoreSSLFlag, deviceToken);
        }

        return apiResponse;

    }

//    private boolean send2CustomBackend(File anyFile, String callRefId, String vin_no,
//                                    String call, String phone, String call_duration,
//                                       String call_start_ts, String tenant,
//                                       String dlrCode, String creCode,
//                                       String leapUriBase, boolean ignoreSSLFlag){
//
//        Log.i(TAG, "post recording to backend server");
//
//        String upload_uri_context = BuildConfig.PropertyPairs.get(UPLOAD_URI_CONTEXT);
//        String leapUri = leapUriBase + upload_uri_context;
//        //String leapUri = "https://mptest1.free.beeceptor.com/" + "UPLOAD_URI_CONTEXT";
//        //String leapUri = "https://c7a17154.ngrok.io/" + "UPLOAD_URI_CONTEXT";
//        String hmacContent = tenant;
//        String contentMd5 = calculateMD5(hmacContent);
//        String secretKey = BuildConfig.PropertyPairs.get(LEAP_API_SECRET);
//        String hmac = calculateHMAC(secretKey, contentMd5);
//        String accessKey = "dfkjnfr39dfdfgsd"; // doesn't matter. backend doesn't do anything with it
//        String hmacHeader = String.format(HMAC_HEADER_FORMAT, accessKey, hmac);
//
//        MediaType MEDIA_TYPE_AUDIO_OGG = MediaType.parse("audio/ogg; codecs=vorbis");
//        OkHttpClient client = getNewOkHttpClient(ignoreSSLFlag);
//        Log.i(TAG, "For CallRefId "+ callRefId+" file name to be uploaded as "+anyFile.getName());
//        Bugfender.d(TAG, "Leap upload URI "+ leapUri );
//        Bugfender.d(TAG, "TENANT-"+ tenant );
//        Bugfender.d(TAG, "For CallRefId "+ callRefId+" file name to be uploaded as "+anyFile.getName());
//
//        RequestBody requestBody = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("taskId", callRefId)
//                .addFormDataPart("vinNo", vin_no)
//                .addFormDataPart(AutoCallerConstants.FORM_CUSTOM_FIELD, callRefId+"::"+creCode)
//                .addFormDataPart(AutoCallerConstants.FORM_PHONE, phone)
//                .addFormDataPart(AutoCallerConstants.FORM_CALL_DURATION, call_duration)
//                .addFormDataPart(AutoCallerConstants.FORM_CALL_START_TS, call_start_ts)
//                .addFormDataPart(AutoCallerConstants.FORM_RECORDING_FILE, anyFile.getName(),
//                        RequestBody.create(MEDIA_TYPE_AUDIO_OGG, anyFile))
//                .build();
//
//        Request request = new Request.Builder()
//                .url(leapUri)
//                .addHeader(HMAC_HEADER, hmacHeader)
//                .addHeader(TENANT_HEADER, tenant)
//                .post(requestBody)
//                .build();
//
//        try (Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) throw new IOException("Unexpected response " + response);
//
//            String result = response.body().string();
//            //System.out.println(result);
//            Log.i(TAG, "server response is "+result);
//            response.close();
//
//            return true;
//        }catch (Exception ex){
//            Log.e(TAG,"Call to Backend Server failed - " + ex.getMessage(), ex);
//            Bugfender.e(TAG,"Upload Call to Backend Server failed - " + ex.getMessage());
//            return false;
//        }
//
//    }

    private boolean send2CustomBackend_v2(File anyFile, String callRefId, String vin_no,
                                       String call, String phone, String call_duration,
                                       String call_start_ts, String tenant,
                                       String dlrCode, String creCode,
                                       String leapUriBase, boolean ignoreSSLFlag){
        JSONObject jsonData = new JSONObject();
        try {
            //jsonData.put(AutoCallerConstants.FORM_RECORDING_URL, s3RecordingUri);
            jsonData.put(AutoCallerConstants.FORM_CALL_START_TS, call_start_ts);
            jsonData.put(AutoCallerConstants.FORM_CALL_DURATION, Integer.parseInt(call_duration));
            jsonData.put(AutoCallerConstants.FORM_CUSTOM_FIELD, callRefId+"::"+creCode);
            jsonData.put(AutoCallerConstants.FORM_PHONE, phone);
            jsonData.put(AutoCallerConstants.FORM_DLR_CODE, dlrCode);
            jsonData.put(AutoCallerConstants.FORM_CALL_INBOUND_DIRECTION,false);

        } catch(JSONException e){
            Log.e(TAG, "JSON Exception when creating payload "+e, e);
            return false;
        }

        Log.i(TAG, "post recording to backend server v2 api");

        String upload_uri_context = BuildConfig.PropertyPairs.get(UPLOAD_URI_CONTEXT);
        String leapUri = leapUriBase + upload_uri_context;
        //String leapUri = "https://mptest1.free.beeceptor.com/" + "UPLOAD_URI_CONTEXT";
        //String leapUri = "https://c7a17154.ngrok.io/" + "UPLOAD_URI_CONTEXT";
        String hmacContent = tenant;
        String contentMd5 = calculateMD5(hmacContent);
        String secretKey = BuildConfig.PropertyPairs.get(LEAP_API_SECRET);
        String hmac = calculateHMAC(secretKey, contentMd5);
        String accessKey = "dfkjnfr39dfdfgsd"; // doesn't matter. backend doesn't do anything with it
        String hmacHeader = String.format(HMAC_HEADER_FORMAT, accessKey, hmac);

        MediaType MEDIA_TYPE_AUDIO_OGG = MediaType.parse("audio/ogg; codecs=vorbis");
        OkHttpClient client = getNewOkHttpClient(ignoreSSLFlag);
        Log.i(TAG, "For CallRefId "+ callRefId+" file name to be uploaded as "+anyFile.getName());
        Bugfender.d(TAG, "Leap upload URI "+ leapUri );
        Bugfender.d(TAG, "TENANT-"+ tenant );
        Bugfender.d(TAG, "For CallRefId "+ callRefId+" file name to be uploaded as "+anyFile.getName());

        String callDataStr = jsonData.toString();
        Log.i(TAG, "callData json is "+ callDataStr);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("callData", callDataStr)
                .addFormDataPart(AutoCallerConstants.FORM_RECORDING_FILE, anyFile.getName(),
                        RequestBody.create(MEDIA_TYPE_AUDIO_OGG, anyFile))
                .build();

        Request request = new Request.Builder()
                .url(leapUri)
                .addHeader(HMAC_HEADER, hmacHeader)
                .addHeader(TENANT_HEADER, tenant)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected response " + response);

            String result = response.body().string();
            //System.out.println(result);
            Log.i(TAG, "server response is "+result);
            response.close();

            return true;
        }catch (Exception ex){
            Log.e(TAG,"Call to Backend Server V2 failed - " + ex.getMessage(), ex);
            Bugfender.e(TAG,"Upload Call to Backend Server V2 failed - " + ex.getMessage());
            return false;
        }
    }

    private S3ResponseCallback createS3Callback(final String callRefId, final String vin_no,
                                                final String call, final String phone,
                                                final String call_duration, final String call_start_ts,
                                                final String tenant, final String dlrCode,
                                                final String creCode,
                                                final String leapUriBase, final boolean ignoreSSLFlag,
                                                final Object lock){
        final S3ResponseCallback callback = new S3ResponseCallback(){
            @Override
            public void onSuccess(String s3URL) {
                synchronized (lock){
                    lock.notifyAll();
                }

//                sendUploadNotification2Backend(s3URL,
//                        callRefId, vin_no, call, phone, call_duration, call_start_ts, tenant,
//                        dlrCode, creCode, leapUriBase, ignoreSSLFlag);


            }

            @Override
            public void onFailure() {
                synchronized (lock){
                    lock.notifyAll();
                }
                setError();
                Log.e(TAG, "S3 Upload failed for callRefid "+callRefId+", phone "+phone);
            }

            @Override
            public void onError(Exception ex) {
                synchronized (lock){
                    lock.notifyAll();
                }
                setError();
                Log.e(TAG, "S3 Upload error'ed for callRefid "+callRefId+", phone "+phone, ex);

            }
        };

        return callback;
    }

    public APIResponse sendUploadNotification2Backend(String s3RecordingUri, String callRefId, String vin_no,
                                       String call, String phone, String call_duration,
                                       String call_start_ts, String tenant,
                                       String dlrCode, String creCode,
                                       String leapUriBase, boolean ignoreSSLFlag, String deviceToken){

        Log.i(TAG, "post recording-uploaded-notification to backend server");

        String upload_notif_uri_context = BuildConfig.PropertyPairs.get(UPLOAD_NOTIFICATION_URI_CONTEXT);
        String leapUri = leapUriBase + upload_notif_uri_context+"?tenant="+tenant;

        //String leapUri = "http://1b533ddc.ngrok.io/" + "UPLOAD_URI_CONTEXT";
        String hmacContent = tenant;
        String contentMd5 = calculateMD5(hmacContent);
        String secretKey = BuildConfig.PropertyPairs.get(LEAP_API_SECRET);
        String hmac = calculateHMAC(secretKey, contentMd5);
        String accessKey = "dfkjnfr39dfdfgsd"; // doesn't matter. backend doesn't do anything with it
        String hmacHeader = String.format(HMAC_HEADER_FORMAT, accessKey, hmac);

        MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = getNewOkHttpClient(ignoreSSLFlag);
        //Log.i(TAG, "For CallRefId "+ callRefId+" file name to be uploaded as "+anyFile.getName());
        Log.i(TAG, "Leap notification URI "+ leapUri +", ignore ssl "+ignoreSSLFlag );
        Bugfender.d(TAG, "Leap notification URI "+ leapUri +", ignore ssl "+ignoreSSLFlag );
        //Bugfender.d(TAG, "TENANT-"+ tenant );
        Bugfender.d(TAG, "For tenant "+tenant+" CallRefId "+ callRefId+" S3 file is "+s3RecordingUri);

        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put(AutoCallerConstants.FORM_RECORDING_URL, s3RecordingUri);
            jsonData.put(AutoCallerConstants.FORM_CALL_START_TS, call_start_ts);
            jsonData.put(AutoCallerConstants.FORM_CALL_DURATION, Integer.parseInt(call_duration));
            jsonData.put(AutoCallerConstants.FORM_CUSTOM_FIELD, callRefId+"::"+creCode);
            jsonData.put(AutoCallerConstants.FORM_PHONE, phone);
            jsonData.put(AutoCallerConstants.FORM_DLR_CODE, dlrCode);
            jsonData.put(AutoCallerConstants.FORM_CALL_INBOUND_DIRECTION,false);
            jsonData.put(AutoCallerConstants.FORM_DEVICE_TOKEN,deviceToken);

        } catch(JSONException e){
            Log.e(TAG, "JSON Exception when creating payload "+e, e);
            return new APIResponse(e);
        }


        RequestBody requestBody = RequestBody.create(jsonData.toString(), MEDIA_TYPE_JSON );
        Request request = new Request.Builder()
                .url(leapUri)
                .addHeader(HMAC_HEADER, hmacHeader)
                .addHeader(TENANT_HEADER, tenant)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected response " + response);
            APIResponse apiResponse = new APIResponse(response);
            //String result = response.body().string();
            Log.i(TAG, "upload notification call returned. server response is "+apiResponse.getBody());
            response.close();
            return apiResponse;
        }catch (Exception ex){
            Log.e(TAG,"Upload Notification Call to Backend Server failed - " + ex.getMessage(), ex);
            Bugfender.e(TAG,"Upload Notification Call to Backend Server failed - " + ex.getMessage());
            return new APIResponse(ex);
        }

    }


}
