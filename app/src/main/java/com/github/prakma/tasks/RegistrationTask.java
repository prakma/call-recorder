package com.github.prakma.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.work.WorkManager;

import com.bugfender.sdk.Bugfender;
import com.github.axet.callrecorder.activities.MainActivity;
import com.github.prakma.api.APIResponse;
import com.github.prakma.api.AutoCallerConstants;
import com.github.prakma.api.ServerApi;
import com.github.prakma.ui.activities.SetupActivity;

public class RegistrationTask extends AsyncTask<String, Integer, String> {
    private static final String TAG = "prakma.RegistrationTask";

    private SetupActivity activity;
    private String leapURIBase;
    private String tenant;
    private String dlrCode;
    private String userEmail;
    private String secret;
    private String deviceToken;

    public RegistrationTask(SetupActivity activity,
                            String leapURIBase,
                            String tenant,
                            String dlrCode,
                            String userEmail,
                            String secret,
                            String deviceToken){
        this.activity = activity;
        this.leapURIBase = leapURIBase;
        this.tenant = tenant;
        this.dlrCode = dlrCode;
        this.userEmail = userEmail;
        this.secret = secret;
        this.deviceToken = deviceToken;

    }

    @Override
    public String doInBackground(String... data){

        // first delete all background upload tasks, if any, accumulated since last registered

        WorkManager
                .getInstance(activity.getApplicationContext())
                .cancelAllWorkByTag(AutoCallerConstants.UPLOAD_WORKER_TAG);



        APIResponse response = ServerApi.getInstance().registerDeviceWithLeapServer(userEmail, secret, deviceToken,
                tenant, dlrCode, leapURIBase, true);
        if(response.isSuccessful()) {
            //Log.i(TAG, "response status code "+response.getStatusCode()+ "response message "+response.getMessage()+", body is "+response.getBody());
            Log.i(TAG, "response status code "+response.getStatusCode()+ "response message "+
                    ", body is "+response.getBody());
            Bugfender.i( TAG,"Registration was successful. Device - "+deviceToken+", userEmail - "+userEmail);
            return response.getBody();
        }
        else {
            if(response.hasClientError())
            {
                Log.e( TAG,"Exception while registering "+ response.getClientException(),
                        response.getClientException() );
                Bugfender.e( TAG,"Exception while registering "+ response.getClientException());
            } else{
                Log.i(TAG, "Server API had error. response status code "+response.getStatusCode()+
                        " response body "+response.getBody());
                Bugfender.e( TAG,"Server API had error. response status code "+response.getStatusCode()+
                        " response body "+response.getBody());
            }
            return "Failure";
        }

    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }

    @Override
    protected void onPostExecute(String responseBodyStr) {

        if(responseBodyStr == null || responseBodyStr.equals("Failure")){
            Log.i(TAG, "Device Registration Failed for this user, "+ userEmail+" . Try again later" );
            activity.postRegister(false, null);
        } else{
            Log.i(TAG, "Device Registration Completed for this user, "+ userEmail );
            activity.postRegister(true, responseBodyStr);
        }

        //Storage.delete(appContext,lastFile );
        //showDialog("Downloaded " + result + " bytes");
        //Storage.delete(RecentCallActivity.this, uri);
        //MainActivity.last(appContext);
    }



}
