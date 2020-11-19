package com.github.prakma.ui.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.axet.callrecorder.R;
import com.github.prakma.state.AutoCallerDB;
import com.github.prakma.state.LogBuffer;
import com.github.prakma.tasks.RegistrationTask;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewLogActivity extends AppCompatActivity {
    private static final String TAG = "prakma.ViewLogActivity";
    //private static ArrayList<String> logBuffer = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewlog);


        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.recent_log_text);
        textView.setText(getLogEntries());

    }

    private String getLogEntries(){
        //StringBuilder stringBuilder = new StringBuilder();
        AutoCallerDB db = AutoCallerDB.getInstance(this.getApplicationContext());
        boolean registrationCompleted = db.isDeviceRegistrationCompleted();
        String deviceId = db.getDeviceFirebaseTokenId();

//        // find the recording checkbox state
//        CheckBox checkBox= (CheckBox) findViewById(R.id.action_call);
//        boolean recordingFlag = checkBox.isChecked();

        LogBuffer logInstance = LogBuffer.getInstance();
        //logInstance.resetLogs();
        logInstance.createRegistrationLog(deviceId, registrationCompleted);
        return logInstance.getRecentLogStmtsAsString();

    }









    public void updatePhoneAndDealerCodeInUI(final String phone, final String dlrCode) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView phoneView = findViewById(R.id.userphone);
                phoneView.setText(phone);

                TextView dlrCodeView = findViewById(R.id.dlrcode);
                dlrCodeView.setText(dlrCode);
            }
        });
    }

}
