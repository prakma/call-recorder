package com.github.prakma.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.github.axet.callrecorder.activities.MainActivity;
import com.github.prakma.api.APIResponse;
import com.github.prakma.api.ServerApi;
import com.github.prakma.ui.activities.SetupActivity;

public class RegistrationTask extends AsyncTask<String, Integer, String> {
    private static final String TAG = "RegistrationTask";

    private SetupActivity activity;
    private String leapURIBase;
    private String tenant;
    private String userEmail;
    private String deviceToken;

    public RegistrationTask(SetupActivity activity,
                            String leapURIBase,
                            String tenant,
                            String userEmail,
                            String deviceToken){
        this.activity = activity;
        this.leapURIBase = leapURIBase;
        this.tenant = tenant;
        this.userEmail = userEmail;
        this.deviceToken = deviceToken;

    }

    @Override
    public String doInBackground(String... data){

        APIResponse response = ServerApi.getInstance().registerDeviceWithLeapServer(userEmail, deviceToken,
                tenant, leapURIBase, true);
        if(response.isSuccessful()) {
            Log.i(TAG, "response status code "+response.getStatusCode()+ "response message "+response.getMessage());
            return "Success";
        }
        else {
            if(response.hasClientError())
            {
                Log.e( TAG,"Exception while registering "+ response.getClientException(),
                        response.getClientException() );
            } else{
                Log.i(TAG, "Server API had error. response status code "+response.getStatusCode()+
                        "response message "+response.getMessage()+" response body "+response.getBody());
            }

            return "Failure";

        }

    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {

        if(result.equals("Success")){
            Log.i(TAG, "Device Registration Completed for this user, "+ userEmail );
            activity.postRegister(true);
        } else{
            Log.i(TAG, "Device Registration Failed for this user, "+ userEmail+" . Try again later" );
            activity.postRegister(false);
        }

        //Storage.delete(appContext,lastFile );
        //showDialog("Downloaded " + result + " bytes");
        //Storage.delete(RecentCallActivity.this, uri);
        //MainActivity.last(appContext);
    }



}
