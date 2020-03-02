package com.github.prakma.init;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.prakma.state.AutoCallerDB;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class AutoCallerInit {
    private static final String TAG = "AutoCallerInit";
    private static AutoCallerInit _instance = new AutoCallerInit();



    public static AutoCallerInit getInstance(){
        return _instance;
    }



    public void init(Context ctx){
        final AutoCallerDB autoCallerDb = AutoCallerDB.getInstance(ctx);
        //autoCallerDb.load();

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        autoCallerDb.setDeviceFirebaseTokenId(token);
                        autoCallerDb.save();

                        // Log and toast
                        //String msg = getString(R.string.msg_token_fmt, token);
                        Log.i(TAG, token);
                        //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }



                });
    }
}
