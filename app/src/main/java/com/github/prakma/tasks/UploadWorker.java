package com.github.prakma.tasks;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.github.axet.callrecorder.app.Storage;
import com.github.prakma.api.ServerApi;
import com.github.prakma.state.AutoCallerDB;

public class UploadWorker extends Worker {
    private static final String TAG = "UploadWorker";
    private Context appContext;

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.appContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        Log.i( TAG," Upload Worker executing now " );
        Data taskData = getInputData();
        AutoCallerDB callerDB = AutoCallerDB.getInstance(appContext);


        String callRefId = taskData.getString("CallReferenceId");
        String recordingFilePath = taskData.getString("RecordingFilePath");

        Log.i( TAG," Upload Worker executing now for Recording Ref Id "+ callRefId+", fileName"+recordingFilePath );

        Uri lastFile = Uri.parse(recordingFilePath);

//        String callRefId = callerDB.getCallReferenceId();
        String leapURIBase = callerDB.getLeapServerURIBase();
        String tenant = callerDB.getLeapTenantName();
        boolean ignoreSSL = callerDB.isIgnoreSSL();

        //String fileNameWithPath = lastFile.getPath();

        //callerDB.addCallRefAndPersist(callRefId, fileNameWithPath);

        boolean result = false;
        try{
            result = ServerApi.getInstance().sendFile2Server(lastFile,callRefId,tenant,
                    leapURIBase, ignoreSSL);
            Log.i( TAG," Recording Ref Id "+ callRefId+", file "+recordingFilePath+" was successfully uploaded." );

            if(result == true){
                // delete the file after upload
                deleteRecordingFile(lastFile);
                return Result.success();
            } else{
                return Result.retry();
            }

        }catch(Exception ex){
            Log.e( TAG,"Exception while Uploading Recording "+callRefId+" . Message "+ ex, ex );
            return Result.retry();
        }



    }

    private void deleteRecordingFile(Uri lastFile){
        try {
            Storage.delete(appContext, lastFile);
        }catch(Exception ex){
            Log.w(TAG, "Ignore the warning, but could not delete file "+lastFile, ex);
        }
    }
}
