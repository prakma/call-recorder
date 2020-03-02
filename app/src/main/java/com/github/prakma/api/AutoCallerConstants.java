package com.github.prakma.api;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class AutoCallerConstants {

    private static final String TAG = "AutoCallerConstants";

    // used for all custom intents
    public static String INTENT_PREFIX = "com.github.prakma.init.AutoCallerInit.";
    public static String CALL_REFERENCE_ID = INTENT_PREFIX + "CALL_REFERENCE_ID";

    public static void dumpIntents(Intent intent){
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                Log.e(TAG, key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
            }
        }
    }
}
