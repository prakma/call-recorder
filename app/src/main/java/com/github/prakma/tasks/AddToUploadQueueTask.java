package com.github.prakma.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Data;
import androidx.work.WorkManager;

import com.bugfender.sdk.Bugfender;
import com.github.axet.callrecorder.activities.MainActivity;
import com.github.axet.callrecorder.app.Storage;
import com.github.prakma.api.AutoCallerConstants;
import com.github.prakma.api.ServerApi;
import com.github.prakma.state.AutoCallerDB;
import com.github.prakma.state.LogBuffer;

import java.text.SimpleDateFormat;
//import java.time.Instant;
//import java.time.ZoneId;
//import java.time.ZonedDateTime;
//import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AddToUploadQueueTask extends AsyncTask<Uri, Integer, String> {
    private static final String TAG = "prakma.AddToUploadQueueTask";

    private static SimpleDateFormat SIMPLE_DATE_WITH_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private Context appContext;
    private Uri lastFile;
    private String call;
    private String phone;

    public AddToUploadQueueTask(Context context, String call, String phone){
        this.appContext = context;
        this.call = call;
        this.phone = phone;
    }

    @Override
    public String doInBackground(Uri... uris){

        // find the first uri
        lastFile = uris[0];
        AutoCallerDB callerDB = AutoCallerDB.getInstance(appContext);
        String callRefId = ""; // initialized with empty string
        String vin_no = ""; // initialized with empty string
        String leapURIBase = callerDB.getLeapServerURIBase();
        String tenant = callerDB.getLeapTenantName();
        boolean ignoreSSL = callerDB.isIgnoreSSL();
//        String s3Id = callerDB.getS3Id();
//        String s3BaseUrl = callerDB.getS3BaseURL();
//        String s3Region = callerDB.getS3Region();
//        String s3Bucket = callerDB.getS3Bucket();
//        String s3Folder = callerDB.getS3Folder();

        String fileNameWithPath = lastFile.getEncodedPath();// Path();

        /*
            if the device is not registered, do not upload any recording
         */
        if(callerDB.isDeviceRegistrationCompleted() == false || callerDB.isS3InfoAvailable() == false) {
            LogBuffer.getInstance().createLogEntry("Not registered. Recording not stored.");
            return "NotRegisteredDevice";
        }


        //callerDB.addCallRefAndPersist(callRefId, fileNameWithPath);

        // check if this phoneno is not empty and it is in the relevant no list
        // if it is not relevant, we will not upload the recording

        if( phone == null ||
                phone.trim().length() == 0 ||
                ! callerDB.isRelevantPhoneNo(phone) ) {
            Log.i(TAG, "This call to phone number is likely irrelevant. It will not be stored "+ phone);
            LogBuffer.getInstance().createLogEntry("This call to phone number is likely irrelevant. It will not be stored. "+ phone);
            Log.i(TAG, "This phone number is not in the irrelevant phone list. "+ phone);
            return "Success";
        } else {
            ///////////

            if(callerDB.isLatestPhoneNo(phone)){
                callRefId = callerDB.getCallReferenceId();
                vin_no = callerDB.getTargetVinNumber();
            }

            // Create a Constraints object that defines when the task should run
            Constraints constraints = new Constraints.Builder()
                    .setRequiresDeviceIdle(true)
                    .build();

//            // find the current time in iso-date-time format
//            ZonedDateTime currTime = ZonedDateTime.now();
//            String isoCurrDateTimeStr = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(currTime);

            // find the current time in yyyy-MM-dd HH:mm:ss (for ex, 2020-11-12 19:14:53 )
            Date currTime = new Date();
            String isoCurrDateTimeStr = SIMPLE_DATE_WITH_TIME_FORMAT.format(currTime);



            // Set Input Data for the background task
            Data callRecordingData = new Data.Builder()
                    .putString("CallReferenceId", callRefId)
                    .putString("Vin_no", vin_no)
                    .putString("Call", call)
                    .putString("Phone", phone)
                    .putString("Call_Start_TS", isoCurrDateTimeStr)
                    .putString("RecordingFilePath", fileNameWithPath)
                    .build();



            // ...then create a OneTimeWorkRequest that uses those constraints
            OneTimeWorkRequest uploadWork =
                    new OneTimeWorkRequest.Builder(UploadWorker.class)
                            .setInitialDelay(1, TimeUnit.SECONDS)
                            //.setConstraints(constraints)
                            .setInputData(callRecordingData)
                            .addTag(AutoCallerConstants.UPLOAD_WORKER_TAG)
                            .build();

            WorkManager workManager = WorkManager.getInstance(appContext);

            workManager.enqueue(uploadWork);

            //uploadWork.getId()


            ///////////
            Log.i(TAG, "Calling Time is "+isoCurrDateTimeStr);
            Log.i( TAG," Recording Ref Id "+ callRefId+", with recording "+fileNameWithPath+" was enqueued for async upload." );
            Bugfender.d(TAG, " Recording Ref Id "+ callRefId+", with recording "+fileNameWithPath+" was enqueued for async upload." );

            return "Success";
        }






    }



    @Override
    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        // delete this file

        // delete everything except last recording
        // but make sure to delete only if those recordings have been synced up
        // Log.i(TAG, "Recording deleted after last upload, "+ lastFile.toString() );
        //Storage.delete(appContext,lastFile );
        //showDialog("Downloaded " + result + " bytes");
        //Storage.delete(RecentCallActivity.this, uri);
        //MainActivity.last(appContext);
    }



}
