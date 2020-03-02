package com.github.prakma.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Data;
import androidx.work.WorkManager;

import com.github.axet.callrecorder.activities.MainActivity;
import com.github.axet.callrecorder.app.Storage;
import com.github.prakma.api.ServerApi;
import com.github.prakma.state.AutoCallerDB;

import java.util.concurrent.TimeUnit;

public class AddToUploadQueueTask extends AsyncTask<Uri, Integer, String> {
    private static final String TAG = "AddToUploadQueueTask";

    private Context appContext;
    private Uri lastFile;

    public AddToUploadQueueTask(Context context){
        this.appContext = context;
    }

    @Override
    public String doInBackground(Uri... uris){

        // find the first uri
        lastFile = uris[0];
        AutoCallerDB callerDB = AutoCallerDB.getInstance(appContext);
        String callRefId = callerDB.getCallReferenceId();
        String leapURIBase = callerDB.getLeapServerURIBase();
        String tenant = callerDB.getLeapTenantName();
        boolean ignoreSSL = callerDB.isIgnoreSSL();

        String fileNameWithPath = lastFile.getEncodedPath();// Path();

        //callerDB.addCallRefAndPersist(callRefId, fileNameWithPath);


        ///////////

        // Create a Constraints object that defines when the task should run
        Constraints constraints = new Constraints.Builder()
                .setRequiresDeviceIdle(true)
                .build();

        // Set Input Data for the background task
        Data callRecordingData = new Data.Builder()
                .putString("CallReferenceId", callRefId)
                .putString("RecordingFilePath", fileNameWithPath)
                .build();


        // ...then create a OneTimeWorkRequest that uses those constraints
        OneTimeWorkRequest uploadWork =
                new OneTimeWorkRequest.Builder(UploadWorker.class)
                        .setInitialDelay(1, TimeUnit.SECONDS)
                        //.setConstraints(constraints)
                        .setInputData(callRecordingData)
                        .addTag("ArtemisUploadWork")
                        .build();

        WorkManager workManager = WorkManager.getInstance(appContext);
        workManager.enqueue(uploadWork);

        //uploadWork.getId()


        ///////////

        Log.i( TAG," Recording Ref Id "+ callRefId+", with recording "+fileNameWithPath+" was enqueued for async upload." );

        return "Success";

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
