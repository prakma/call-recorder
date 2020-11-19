package com.github.prakma.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.github.axet.callrecorder.activities.MainActivity;
import com.github.axet.callrecorder.activities.RecentCallActivity;
import com.github.axet.callrecorder.app.Storage;
import com.github.prakma.api.ServerApi;
import com.github.prakma.state.AutoCallerDB;

import java.io.File;

@Deprecated
public class UploaderTask extends AsyncTask<Uri, Integer, String> {
    private static final String TAG = "prakma.UploaderTask";

    private Context appContext;
    private Uri lastFile;

    public UploaderTask(Context context){
        this.appContext = context;
    }

    @Override
    public String doInBackground(Uri... uris){
        return "Success";
    }

    /*
    @Override
    public String doInBackground(Uri... uris){

        // find the first uri
        lastFile = uris[0];
        AutoCallerDB callerDB = AutoCallerDB.getInstance(appContext);
        String callRefId = callerDB.getCallReferenceId();
        String leapURIBase = callerDB.getLeapServerURIBase();
        String tenant = callerDB.getLeapTenantName();
        boolean ignoreSSL = callerDB.isIgnoreSSL();

        String fileNameWithPath = lastFile.getPath();

        callerDB.addCallRefAndPersist(callRefId, fileNameWithPath);

        boolean result = false;
        try{
            result = ServerApi.getInstance().sendFile2Server(lastFile,callRefId,tenant,
                    leapURIBase, ignoreSSL);
            Log.i( TAG," Recording Ref Id "+ callRefId+" was successfully uploaded." );
        }catch(Exception ex){
            Log.e( TAG,"Exception while Uploading Recording "+callRefId+" . Message "+ ex, ex );
        }

        return ""+result;

    }

     */

    @Override
    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        // delete this file

        // delete everything except last recording
        // but make sure to delete only if those recordings have been synced up
        Log.i(TAG, "Recording deleted after last upload, "+ lastFile.toString() );
        //Storage.delete(appContext,lastFile );
        //showDialog("Downloaded " + result + " bytes");
        //Storage.delete(RecentCallActivity.this, uri);
        MainActivity.last(appContext);
    }



}
