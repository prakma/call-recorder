package com.github.prakma.api;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.time.format.DateTimeFormatter;

public class AutoCallerConstants {

    private static final String TAG = "AutoCallerConstants";

    // used for all custom intents
    public static String INTENT_PREFIX = "com.github.prakma.init.AutoCallerInit.";
    public static String CALL_REFERENCE_ID = INTENT_PREFIX + "CALL_REFERENCE_ID";

    // tag for uploadworker
    public static final String UPLOAD_WORKER_TAG = "ArtemisUploadWork";

    // string constants for recording-upload-notification form names
    public static final String FORM_CALLREFID = "taskId";
    //public static final String FORM_VIN_NO = "vinNo";
    public static final String FORM_PHONE = "To";
    public static final String FORM_CALL_DURATION = "ConversationDuration";
    public static final String FORM_CALL_START_TS = "DateCreated"; //"call_start_ts";

    public static final String FORM_RECORDING_FILE = "file";
    public static final String FORM_RECORDING_URL = "RecordingUrl";
    //public static final String FORM_DATE_CREATED = "DateCreated";
    public static final String FORM_CUSTOM_FIELD = "CustomField";
    public static final String FORM_DLR_CODE = "DealerCode";
    public static final String FORM_CALL_INBOUND_DIRECTION = "Inbound";

    // device token to be used for acknowledgement api call
    public static final String FORM_DEVICE_TOKEN = "token";



    // DateTime formatter
    //public static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    // string constants to read from properties file
    public static final String S3ID = "S3ID";
    public static final String S3BUCKET = "S3BUCKET";
    public static final String S3UPLOADFOLDER = "S3UPLOADFOLDER";
    public static final String S3REGION = "S3REGION";
    public static final String S3URLBASE = "S3URLBASE";

    // string constant related to Firebase notification Message for version upgrade title
    public static final String VERSION_UPGRADE_NOTIFICATION_TITLE="VERSION_UPGRADE_NOTIFICATION_TITLE";



    public static void dumpIntents(Intent intent){
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                Log.e(TAG, key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
            }
        }
    }
}
