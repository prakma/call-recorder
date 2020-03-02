package com.github.prakma.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.axet.callrecorder.R;
import com.github.prakma.api.ServerApi;
import com.github.prakma.state.AutoCallerDB;
import com.github.prakma.tasks.RegistrationTask;

public class SetupActivity extends AppCompatActivity {
    private static final String TAG = "SetupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setup);

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.password);
        textView.setText("This is not a password");

        AutoCallerDB db = AutoCallerDB.getInstance(this.getApplicationContext());

        TextView phoneView = findViewById(R.id.userphone);
        phoneView.setText(db.getDeviceRegisteredPhoneNo());


        TextView emailView = findViewById(R.id.useremail);
        emailView.setText(db.getLeapUserEmail());

        TextView tokenView = findViewById(R.id.device_token);
        tokenView.setText(db.getDeviceFirebaseTokenId());

        TextView tenantView = findViewById(R.id.usertenant);
        tenantView.setText(db.getLeapTenantName());

        TextView leapUriView = findViewById(R.id.leapuribase);
        leapUriView.setText(db.getLeapServerURIBase());


    }

    public void saveAndRegister(View view){
        TextView phoneView = findViewById(R.id.userphone);
        String phoneNo = phoneView.getText().toString();

        TextView emailView = findViewById(R.id.useremail);
        String userEmail = emailView.getText().toString();

        TextView passwordView = findViewById(R.id.password);
        String password = passwordView.getText().toString();

        TextView tokenView = findViewById(R.id.device_token);
        String token = tokenView.getText().toString();

        TextView tenantView = findViewById(R.id.usertenant);
        String tenant = tenantView.getText().toString();

        TextView leapUriView = findViewById(R.id.leapuribase);
        String leapUriBase = leapUriView.getText().toString();

        CheckBox ignoreSSLView = findViewById(R.id.checkbox_ignoressl);
        boolean ignoreSSLSetting = ignoreSSLView.isChecked();


        AutoCallerDB autoCallerDb = AutoCallerDB.getInstance(this);
        save(autoCallerDb, userEmail, phoneNo, tenant, leapUriBase, ignoreSSLSetting);

        register(autoCallerDb, leapUriBase, tenant, userEmail, password);



        //registerBtn.setEnabled(false);

    }

    private void save(AutoCallerDB autoCallerDb, String userEmail, String phoneNumber,
                      String tenant, String leapUriBase, boolean ignoreSSL){

        autoCallerDb.setLeapTenantName(tenant);
        autoCallerDb.setDeviceRegisteredPhoneNo(phoneNumber);
        autoCallerDb.setLeapUserEmail(userEmail);
        autoCallerDb.setIgnoreSSL(ignoreSSL);
        autoCallerDb.setLeapServerURIBase(leapUriBase);
        autoCallerDb.save();

    }

    private void register(AutoCallerDB autoCallerDb, String leapURIBase, String tenant,
                          String userEmail, String forceRegister){

        boolean regCompleted = autoCallerDb.isDeviceRegistrationCompleted();

        if(!regCompleted || "Unlock".equals(forceRegister)){
            new RegistrationTask(SetupActivity.this,
                    leapURIBase,
                    tenant,
                    userEmail,
                    autoCallerDb.getDeviceFirebaseTokenId()
                    ).execute(userEmail);



        } else{
            Log.i(TAG, "Device Registration Previously Completed");
        }
    }

    public void postRegister(boolean registrationSuccess){

        AutoCallerDB autoCallerDb = AutoCallerDB.getInstance(this);

        if (registrationSuccess) {
            Log.i(TAG, "Device Registration Completed");
            autoCallerDb.setDeviceRegistrationCompleted(true);
            autoCallerDb.save();

            Toast toast = Toast.makeText(getApplicationContext(),
                    "The device is registered",
                    Toast.LENGTH_SHORT);

            toast.show();

        } else{
            Toast toast = Toast.makeText(getApplicationContext(),
                    "The device registration failed. Try again",
                    Toast.LENGTH_SHORT);

            toast.show();
        }




        //Button registerBtn = findViewById(R.id.registerBtn);

    }





}
