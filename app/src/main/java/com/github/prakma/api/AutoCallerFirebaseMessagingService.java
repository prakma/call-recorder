package com.github.prakma.api;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.github.prakma.state.AutoCallerDB;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;

import static com.github.prakma.api.AutoCallerConstants.CALL_REFERENCE_ID;


public class AutoCallerFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "AutoCallerFirebaseMessagingService";

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
        String call_reference_id = remoteMessage.getData().get("call_reference_id");


        Log.i(TAG, "phone: " + phone);
        Log.i(TAG, "Call_Reference_Id: " + call_reference_id);


        Log.i(TAG, "SentTime: " + new Date(remoteMessage.getSentTime()));

        //Log.i(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());


        //initiateNewPhoneCall(remoteMessage.getNotification().getBody());
        initiateNewPhoneCall(phone, call_reference_id);


    }

    private void initiateNewPhoneCall(String phoneNumber, String referenceId){
        AutoCallerDB callerDB = AutoCallerDB.getInstance(this.getBaseContext());
        callerDB.setCallReferenceId(referenceId);
        callerDB.setTargetPhoneNumber(phoneNumber);
        callerDB.save();

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+phoneNumber));
        callIntent.putExtra(CALL_REFERENCE_ID, referenceId );
        startActivity(callIntent);
    }



}
