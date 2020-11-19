package com.github.prakma.tasks;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


import com.bugfender.sdk.Bugfender;
import com.github.axet.androidlibrary.sound.MediaPlayerCompat;
import com.github.axet.callrecorder.app.Storage;
import com.github.prakma.api.APIResponse;
import com.github.prakma.api.ServerApi;
import com.github.prakma.state.AutoCallerDB;
import com.github.prakma.state.LogBuffer;
import com.github.prakma.thirdpartyrecording.CubeACRRecordManager;
import com.github.prakma.thirdpartyrecording.NativeRecordManager;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.ZonedDateTime;
//import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class UploadWorker extends Worker {
    private static final String TAG = "prakma.UploadWorker";
    private static SimpleDateFormat SIMPLE_DATE_WITH_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Context appContext;

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.appContext = context;
    }

    private String ifnullthenemptystring(String str){
        if(str == null) return "";
        return str;
    }
    private String ifnullthen_wellknowncallrefid_string(String str){
        if(str == null || str.trim().length() == 0) return "TASK_ID_UNKNOWN::CALL_ID_UNKNOWN::VIN_NO_UNKNOWN";
        return str;
    }


    @NonNull
    @Override
    public Result doWork() {

        Log.i( TAG," Upload Worker executing now " );
        Data taskData = getInputData();
        AutoCallerDB callerDB = AutoCallerDB.getInstance(appContext);

        String callRefId = ifnullthen_wellknowncallrefid_string(taskData.getString("CallReferenceId"));
        String vin_no = ifnullthenemptystring(taskData.getString("Vin_no"));
        String call = ifnullthenemptystring(taskData.getString("Call"));
        String phone = ifnullthenemptystring(taskData.getString("Phone"));
        String recordingFilePath = ifnullthenemptystring(taskData.getString("RecordingFilePath"));
        String call_start_ts = ifnullthenemptystring(taskData.getString("Call_Start_TS"));

        Log.i( TAG," Upload Worker executing now for Recording Ref Id "+ callRefId+", called at:"+call_start_ts+" fileName "+recordingFilePath);

        Bugfender.d( TAG," Upload Worker executing now for Recording Ref Id "+ callRefId+", fileName "+recordingFilePath );


        Uri lastFile = Uri.parse(recordingFilePath);
        long duration_in_seconds = (getDuration(appContext, lastFile))/1000;
        //Log.d(TAG, " recording duration (in seconds) is "+ duration_in_seconds);
        String contactName = getContactName(phone, appContext);
        Log.d(TAG, " contact_name for phone "+ phone+" is - "+contactName);

        String nativeRecordLocationHint = callerDB.getRecordLocationHint();



        File recordingFile = null; //new File( lastFile.getPath() );
        recordingFile = getAlternateRecordingIfAvailable(phone, contactName,  call_start_ts, nativeRecordLocationHint);
        if(recordingFile == null) {
            if(isAndroid10()){
                // record in logs. likely configuration is wrong.
                add_diagnostic_log(phone, new File( lastFile.getPath()), duration_in_seconds);
            }
            recordingFile = new File(lastFile.getPath());
        }

        String leapURIBase = callerDB.getLeapServerURIBase();
        String tenant = callerDB.getLeapTenantName();
        String dlrCode = callerDB.getDlrCode();
        String creCode = callerDB.getLeapUserEmail();
        boolean ignoreSSL = callerDB.isIgnoreSSL();
        String deviceToken = callerDB.getDeviceFirebaseTokenId();


        String s3Id = callerDB.getS3Id();
        String s3BaseUrl = callerDB.getS3BaseURL();
        String s3Region = callerDB.getS3Region();
        String s3Bucket = callerDB.getS3Bucket();
        String s3Folder = callerDB.getS3Folder();


        //String fileNameWithPath = lastFile.getPath();

        //callerDB.addCallRefAndPersist(callRefId, fileNameWithPath);

        boolean result = false;
        APIResponse apiResponse = null;
        try{
            Log.i(TAG, "recording of talk with phone "+phone);
            Bugfender.d(TAG, "recording of talk with phone "+phone);
            if(phone == null || phone.trim().length() == 0){
                // do not attempt to upload as we have no reference of even the phone no for which recording is done
                Log.i(TAG, "will not upload because phone is empty "+phone);
                Bugfender.d(TAG, "will not upload because phone is empty "+phone);
                result = false;
            } else{
                apiResponse = ServerApi.getInstance().sendFile2Server(recordingFile, callRefId, vin_no,
                        call, phone, ""+duration_in_seconds, call_start_ts, tenant,
                        dlrCode, creCode, appContext, leapURIBase, ignoreSSL,
                        s3Id, s3Region, s3Bucket, s3Folder, s3BaseUrl, deviceToken);
                if(apiResponse != null && apiResponse.isSuccessful()){
                    result = true;
                }
                //Bugfender.d(TAG, "Upload Invocation returned. Check success ? "+result);

            }


            if(result == true){

                checkAndRemovePairingIfCreIsNoLongerActive(apiResponse);
                deleteForeginAppRecordingFile();
                //deleteRecordingFile(lastFile);
                deleteRecordingFile( recordingFile );
                return Result.success();
            } else{
                deleteForeginAppRecordingFile();
                return Result.success();
            }

        }catch(Exception ex){
            Log.e( TAG,"Exception while Uploading Recording "+callRefId+" . Message "+ ex, ex );
            Bugfender.e( TAG,"Exception while Uploading Recording "+callRefId+" . Message "+ ex );
            return Result.retry();
        }



    }

    private void deleteRecordingFile(File lastFile){
        try {
//            Uri validRecordingUri = Uri.fromFile(
//                    new File(
//                            lastFile.getPath()
//                    )
//            );
            // Storage.delete(appContext, validRecordingUri);

        }catch(Exception ex){
            Log.w(TAG, "Ignore the warning, but could not delete file "+lastFile, ex);
        }
    }

    private void deleteForeginAppRecordingFile(){
        try {
            //deleteAllLatestNativeCallRecording();
            CubeACRRecordManager.getInstance().deleteAllCubeACRFiles();

        }catch(Exception ex){
            Log.w(TAG, "Ignore the warning, but could not delete foreign app recording object ", ex);
        }
    }

    private void checkAndRemovePairingIfCreIsNoLongerActive(APIResponse apiResponse){
        if(apiResponse == null || apiResponse.hasClientError() || apiResponse.hasServerError()){
            return;
        } else{
            try {
                JSONObject jsonResponse = new JSONObject(apiResponse.getBody());
                boolean userIsValid = jsonResponse.getBoolean("isUserValid");
                if(userIsValid) {
                    return;
                }
                else {
                    // disable the app registration/pairing so that future uploads will not be done through this app.
                    AutoCallerDB callerDB = AutoCallerDB.getInstance(appContext);
                    callerDB.resetDeviceRegistration();
                    callerDB.save();
                    Log.i(TAG, "App registration reset. Will require fresh registration");
                }

            }catch(Exception ex){
                Log.e(TAG, "Error while acknowledgement response, "+ex, ex);
            }

        }
    }

    private void add_diagnostic_log(String phone, File recordingFile, long call_duration_in_seconds){
        String mfr = Build.MANUFACTURER;
        String model = Build.MODEL;
        String product = Build.PRODUCT;
        String androidVersion = Build.VERSION.RELEASE;

        String fileName = recordingFile.getName();
        long fileSizeInBits = 0;
        if(recordingFile.exists())
            fileSizeInBits = recordingFile.length();

        boolean bogusRecording = isLikelyBogusRecording(call_duration_in_seconds, fileSizeInBits);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(mfr);
        stringBuilder.append("/");

        stringBuilder.append(model);
        stringBuilder.append("/");

        stringBuilder.append(product);
        stringBuilder.append("/");

        stringBuilder.append("androidVersion ");
        stringBuilder.append(androidVersion);

        stringBuilder.append(" ");

        stringBuilder.append("recording ");
        stringBuilder.append(fileName);

        stringBuilder.append(" ");

        stringBuilder.append("callToNumber ");
        stringBuilder.append(phone);

        stringBuilder.append(" ");

        if(bogusRecording) {
            stringBuilder.append("Call recording may not be audible. Please check " +
                    "that ArtemisCaller recording configurations are correctly followed. " +
                    "Contact your support if required.");
        } else {
            stringBuilder.append("Call recording audio varies by phone model. " +
                    "It may be a good idea to periodically check that your recordings are audible.");
        }
        String diagnosticMsg = stringBuilder.toString();
        LogBuffer.getInstance().createLogEntry(diagnosticMsg);
        Bugfender.e(TAG, diagnosticMsg);
    }

    private boolean isAndroid10(){
        int androidSDKVersion = Build.VERSION.SDK_INT;
        if(androidSDKVersion >= 29)
            return true;
        else
            return false;
    }

    private boolean isLikelyBogusRecording(long call_duration_in_seconds, long file_size_in_bits){
        // use heuristics to guess if the file has bogus/silent recording
        // because bogus/silent recording will have filesize (per second of recording) which are much
        // less than the valid recording's filesize(per second og recording).

        if(file_size_in_bits == 0) return true;

        long bits_per_sec = file_size_in_bits/call_duration_in_seconds;


        // for less than 5 seconds duration, file size less than 1300 bits/sec denotes bogus/silent recording
        // for less than 10 seconds duration, file size less than 300 bits/sec denotes bogus/silent recording
        // for less than 20 seconds duration, file size less than 275 bits/sec denotes bogus/silent recording
        // for less than 30 seconds duration, file size less than 200 bits/sec denotes bogus/silent recording
        // for less than 60 seconds duration, file size less than 125 bits/sec denotes bogus/silent recording
        // for less than 100 seconds duration, file size less than 100 bits/sec denotes bogus/silent recording

        // the above heuristics are taken from my observations on my phone for .ogg file

        if(call_duration_in_seconds < 5){
            return bits_per_sec < 1300; // means bogus recording is true
        } else if(call_duration_in_seconds < 10){
            return bits_per_sec < 300;
        } else if(call_duration_in_seconds < 20){
            return bits_per_sec < 275;
        } else if(call_duration_in_seconds < 30){
            return bits_per_sec < 200;
        } else if(call_duration_in_seconds < 60){
            return bits_per_sec < 125;
        } else if(call_duration_in_seconds < 100){
            return bits_per_sec < 100;
        } else if(call_duration_in_seconds < 400){
            return bits_per_sec < 75;
        } else {
            // means all recordings greater than 7 minutes and bits/sec is less than 72
            // is most likely a silent recording
            return bits_per_sec < 72; // it is bogus recording
        }

    }




    public static long getDuration(final Context context, final Uri u) {
        final Object lock = new Object();
        final AtomicLong duration = new AtomicLong();
        final MediaPlayerCompat mp = MediaPlayerCompat.create(context, u);
        if (mp == null)
            return 0;
        mp.addListener(new MediaPlayerCompat.Listener() {
            @Override
            public void onReady() {
                synchronized (lock) {
                    duration.set(mp.getDuration());
                    lock.notifyAll();
                }
            }

            @Override
            public void onEnd() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }

            @Override
            public void onError(Exception e) {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });
        try {
            synchronized (lock) {
                mp.prepare();
                duration.set(mp.getDuration());
                if (duration.longValue() == 0)
                    lock.wait();
            }
            mp.release();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return duration.longValue();
    }

//    private String generate_date_based_subfolder_name(String call_start_ts){
//        java.time.LocalDate localDate = LocalDate.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(call_start_ts));
//
//    }

    private File getAlternateRecordingIfAvailable(String phoneNumber, String contactName,
                                                  String call_start_ts,
                                                  String nativeRecordLocationHint){

        File cubeACRRecording = checkCubeACRRecordingIfAvailable(phoneNumber, contactName, call_start_ts);
        if(cubeACRRecording != null){
            return cubeACRRecording;
        } else{
            File nativeRecording = checkNativeRecordingIfAvailable(phoneNumber, contactName,
                    call_start_ts, nativeRecordLocationHint);
            if(nativeRecording != null){
                return nativeRecording;
            } else {
                return null;
            }
        }
    }

    private File checkCubeACRRecordingIfAvailable(String phoneNumber, String contactName, String call_start_ts){
        //todo - uncomment, and implement
        //throw new RuntimeException("To be implemented - checkCubeACRRecordingIfAvailable()");
        return null;
    }

    private File checkNativeRecordingIfAvailable(String phoneNumber, String contactName,
                                                 String call_start_ts, String nativeRecordLocationHint) {
        //ZonedDateTime zdt = ZonedDateTime.parse(call_start_ts, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        try {
            Date reproducedDate = SIMPLE_DATE_WITH_TIME_FORMAT.parse(call_start_ts);
            return NativeRecordManager.getInstance(nativeRecordLocationHint).getMostRecentRecordingOfGivenPhoneCall(phoneNumber, contactName, reproducedDate);
        }catch(Exception ex){
            throw new RuntimeException("Cannot parse Date from String - "+call_start_ts, ex);
        }
    }


    private void deleteAllLatestNativeCallRecording() {

        Comparator<File> compareFilesByRecency = new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return (int)(o2.lastModified() - o1.lastModified());
            }
        };

        //File rootFilesDir = context.getExternalFilesDir(null);

        File rootFilesDir = Environment.getExternalStorageDirectory();
        Log.i(TAG, "Root Folder's is at - "+ rootFilesDir.getAbsolutePath());

        String audioSavePathInDevice =
                rootFilesDir.getAbsolutePath() + "/" +
                        "Call";
        File callFolder = new File(audioSavePathInDevice);
        //String [] childFiles = rootFilesDir.list();
        File [] callFolderFiles = callFolder.listFiles();

        if(callFolderFiles != null){
            Arrays.sort(callFolderFiles, compareFilesByRecency);
            for(int i=0; i<callFolderFiles.length;i++){
                Log.i(TAG, "Call Folder's file - "+ callFolderFiles[i].getAbsolutePath());
                callFolderFiles[i].delete();
                Log.i(TAG, "Call Folder's file deleted. ");
                //Log.i(TAG, "Call Folder's file deleted. timestamp - "+ callFolderFiles[i].lastModified());
            }
        } else{
            try {
                Log.i(TAG, "No child files/dirs exist at this root - " + callFolder.getCanonicalPath());
            }catch (Exception ex){
                Log.e(TAG,"Exception while seeing child dirs", ex);
            }
        }



    }

    public String getContactName(final String phoneNumber, Context context)
    {
        Uri uri=Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName="";
        Cursor cursor=context.getContentResolver().query(uri,projection,null,null,null);

        if (cursor != null) {
            if(cursor.moveToFirst()) {
                contactName=cursor.getString(0);
            }
            cursor.close();
        }

        return contactName;
    }





}
