package com.github.prakma.api;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.github.axet.callrecorder.BuildConfig;
import com.github.axet.callrecorder.activities.MainActivity;
import com.github.prakma.state.AutoCallerDB;
import com.github.prakma.state.LogBuffer;
import com.github.prakma.tasks.RegistrationTask;
import com.github.prakma.ui.activities.SetupActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;

import static com.github.prakma.api.AutoCallerConstants.CALL_REFERENCE_ID;


public class AutoCallerFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "prakma.FirebaseMsgService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "AutoCallerFirebaseMessagingService created" );
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d(TAG, s);

        //sendNewRegistrationToServer();
    }



    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO: Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        Log.i(TAG, "From: " + remoteMessage.getFrom());
        //Log.i(TAG, "Title: " + remoteMessage.getNotification().getTitle());
        Log.i(TAG, "Title: " + remoteMessage.getData().get("title"));
        Log.i(TAG, "Body: " + remoteMessage.getData().get("body"));
        Log.i(TAG, "Key2: " + remoteMessage.getData().get("key2"));


        String phone = remoteMessage.getData().get("body");
        String call_reference_id_temp = remoteMessage.getData().get("call_reference_id"); // task_id
        String call_id = remoteMessage.getData().get("call_id"); // call_id
        String vin_no = remoteMessage.getData().get("vin_no");

        // combined call_reference_id that will be stored is of the format
        // format - task_id::call_id::vin_no
        // note that the task_id is what we get as "call_reference_id" from server
        // also note that what will be passed back to server on acknowledgement call is
        // of the format - task_id::call_id::vin_no::cre_email
        String call_reference_id = call_reference_id_temp +
                "::" + call_id +
                "::" + vin_no; // combined call_reference_id that will be stored

        String notificationTitle = remoteMessage.getNotification() != null?
                remoteMessage.getNotification().getTitle(): "";
        String notificationBody = remoteMessage.getNotification() != null?
                remoteMessage.getNotification().getBody(): "";

        String predefinedVersionUpgradeUrlKey = BuildConfig.PropertyPairs.get(AutoCallerConstants.VERSION_UPGRADE_NOTIFICATION_TITLE);

        Log.i(TAG, "phone: " + phone);
        Log.i(TAG, "Call_Reference_Id: " + call_reference_id);


//        Log.i(TAG, "SentTime: " + new Date(remoteMessage.getSentTime()));
//
//        Log.i(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
//        Log.i(TAG, "Notification ChannelId: " + remoteMessage.getNotification().getChannelId());
//        Log.i(TAG, "Notification Title: " + remoteMessage.getNotification().getTitle());
//
//        Log.i(TAG, "Notification Tag: " + remoteMessage.getNotification().getTag());
//        Log.i(TAG, "Notification Data: " + remoteMessage.getData());


        if("New version update".equals(notificationTitle)){
            // tell user to upgrade his ArtemisCallerApp
            String urlToDownload = remoteMessage.getData().get(predefinedVersionUpgradeUrlKey);
            initiateNewVersionDownload2(predefinedVersionUpgradeUrlKey, urlToDownload);
            LogBuffer.getInstance().createLogEntry("Received notification from Artemis to download new version. Url - "+urlToDownload);
            //initiateNewVersionDownload(urlToDownload);


        } else if(phone != null && phone.trim().length() > 0){
            // initiate the outbound phone call
            initiateNewPhoneCall(phone, call_reference_id, vin_no);
        } else {
            // we do not know why this notification came here. Just log and leave.
            String logMsg = "Unknown notification received. Body: "+
                    remoteMessage.getNotification().getBody() +
                    " Data: "+remoteMessage.getData();

            LogBuffer.getInstance().createLogEntry(logMsg);
            Log.i(TAG, logMsg);
        }





        //initiateNewPhoneCall(remoteMessage.getNotification().getBody());
        //initiateNewPhoneCall(phone, call_reference_id, vin_no);


    }

    private void initiateNewPhoneCall(String phoneNumber, String referenceId, String vin_no){
        AutoCallerDB callerDB = AutoCallerDB.getInstance(this.getBaseContext());
        callerDB.setCallReferenceId(referenceId);
        callerDB.setTargetPhoneNumber(phoneNumber);
        callerDB.setTargetVinNumber(vin_no);
        callerDB.save();

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+phoneNumber));
        callIntent.putExtra(CALL_REFERENCE_ID, referenceId );
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(callIntent);
    }

//    private void initiateNewVersionDownload(final String urlToDownload){
//        //MainActivity.startActivity();
//        AlertDialog.Builder alert = new AlertDialog.Builder(this);
//        alert.setTitle("Your must install new version of Artemis Caller app. Press Ok to go to APK download site.");
//        // alert.setMessage("Message");
//
//        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                //Your action here
//                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlToDownload));
//                startActivity(browserIntent);
//
//            }
//        });
//
//        alert.setNegativeButton("Cancel",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//
//                    }
//                });
//
//        alert.show();
//    }

    private void initiateNewVersionDownload2(final String predefinedVersionUpgradeUrlKey,
                                        final String urlToDownload){
        Intent downloadIntent = new Intent(this.getApplication().getBaseContext(),MainActivity.class);
        //new Intent(this.getApplication().getBaseContext(),MainActivity.class);
        downloadIntent.putExtra(predefinedVersionUpgradeUrlKey, urlToDownload );
        downloadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //downloadIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //Intent.Flag_
        startActivity(downloadIntent);
    }



}
