package com.github.prakma.ui.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bugfender.sdk.Bugfender;
import com.github.axet.callrecorder.R;
import com.github.prakma.api.ServerApi;
import com.github.prakma.state.AutoCallerDB;
import com.github.prakma.state.LogBuffer;
import com.github.prakma.tasks.RegistrationTask;

import org.json.JSONObject;

public class SetupActivity extends AppCompatActivity {
    private static final String TAG = "prakma.SetupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setup);

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.password);
        textView.setText("Password");

        AutoCallerDB db = AutoCallerDB.getInstance(this.getApplicationContext());

        TextView phoneView = findViewById(R.id.userphone);
        phoneView.setText(db.getDeviceRegisteredPhoneNo());


        TextView emailView = findViewById(R.id.useremail);
        emailView.setText(db.getLeapUserEmail());

        TextView tokenView = findViewById(R.id.device_token);
        tokenView.setText(db.getDeviceFirebaseTokenId());

        TextView tenantView = findViewById(R.id.usertenant);
        tenantView.setText(db.getLeapTenantName());

        TextView dlrCodeView = findViewById(R.id.dlrcode);
        dlrCodeView.setText(db.getDlrCode());

        TextView leapUriView = findViewById(R.id.leapuribase);
        leapUriView.setText(db.getLeapServerURIBase());

        TextView recordLocationView = findViewById(R.id.recordLocationHint);
        recordLocationView.setText(db.getRecordLocationHint());

//        CheckBox ignoreSSLView = findViewById(R.id.checkbox_ignoressl);
//        ignoreSSLView.setChecked(db.isIgnoreSSL());

        TextView t2 = (TextView) findViewById(R.id.checkbox_eula);

        t2.setMovementMethod(LinkMovementMethod.getInstance());



    }

    public void onEULACheckBoxClick(View view){

        boolean checked = ((CheckBox) view).isChecked();
        LogBuffer.getInstance().createLogEntry("EULA Checkbox clicked - "+checked);
        Bugfender.i(TAG, "EULA Checkbox clicked = "+checked);
        Button registerBtn = (Button) findViewById(R.id.registerBtn);
        if(checked){
            registerBtn.setEnabled(true);
        } else {
            registerBtn.setEnabled(false);
        }
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

        TextView dlrCodeView = findViewById(R.id.dlrcode);
        String dlrCode = dlrCodeView.getText().toString();

        TextView leapUriView = findViewById(R.id.leapuribase);
        String leapUriBase = leapUriView.getText().toString();

        TextView recordLocationView = findViewById(R.id.recordLocationHint);
        String recordLocationHint = recordLocationView.getText().toString();

        //CheckBox ignoreSSLView = findViewById(R.id.checkbox_ignoressl);
        boolean ignoreSSLSetting = true; //ignoreSSLView.isChecked();


        AutoCallerDB autoCallerDb = AutoCallerDB.getInstance(this);
        save(autoCallerDb, userEmail, phoneNo, tenant, dlrCode, leapUriBase, recordLocationHint, ignoreSSLSetting);

        register(autoCallerDb, leapUriBase, tenant, dlrCode, userEmail, password);



        //registerBtn.setEnabled(false);

    }

    private void save(AutoCallerDB autoCallerDb, String userEmail, String phoneNumber,
                      String tenant, String dlrCode,
                      String leapUriBase, String recordLocationHint, boolean ignoreSSL){

        autoCallerDb.setLeapTenantName(tenant);
        autoCallerDb.setDeviceRegisteredPhoneNo(phoneNumber);
        autoCallerDb.setLeapUserEmail(userEmail);
        Log.i(TAG, "Ignore SSL Setting is "+ignoreSSL);
        autoCallerDb.setIgnoreSSL(ignoreSSL);
        autoCallerDb.setLeapServerURIBase(leapUriBase);
        autoCallerDb.setRecordLocationHint(recordLocationHint);
        autoCallerDb.setDlrCode(dlrCode);
        autoCallerDb.save();

    }

    private void register(final AutoCallerDB autoCallerDb, final String leapURIBase, final String tenant,
                          final String dlrCode, final String userEmail, final String secret){

        final boolean regCompleted = autoCallerDb.isDeviceRegistrationCompleted();
        if(regCompleted){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Your app is already registered. Press Ok to re-register.");
            // alert.setMessage("Message");

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //Your action here

                        new RegistrationTask(SetupActivity.this,
                                leapURIBase,
                                tenant,
                                dlrCode,
                                userEmail,
                                secret,
                                autoCallerDb.getDeviceFirebaseTokenId()
                        ).execute(userEmail);

                }
            });

            alert.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    });

            alert.show();
        } else{
            // fresh registration
            new RegistrationTask(SetupActivity.this,
                    leapURIBase,
                    tenant,
                    dlrCode,
                    userEmail,
                    secret,
                    autoCallerDb.getDeviceFirebaseTokenId()
            ).execute(userEmail);
        }


    }

    public void postRegister(boolean registrationSuccess, String responseBody){

        AutoCallerDB autoCallerDb = AutoCallerDB.getInstance(this);
        Log.i(TAG, "Response body is "+ responseBody);

        if (registrationSuccess) {
            Log.i(TAG, "Device Registration Completed");
            try {
                JSONObject responseJson = new JSONObject(responseBody);
//                "s3UPLOADFOLDER": "LEAP/16-artemis/recordings/S8202/cres8202test1@leadics.com/2020-09-18/",
//                        "s3REGION": "us-west-2",
//                        "s3URLBASE": "https://s3-us-west-2.amazonaws.com/",
//                        "s3BUCKET": "hmil-leap-uat-v1-cl4y5ps",
//                        "s3ID": "ap-south-1:139ff2ae-a546-48a6-9b72-71944a7919e0",
//                        "dlrCode": "S8202"
                String s3UploadFolder = responseJson.getString("s3UPLOADFOLDER");

                String s3Region = responseJson.getString("s3REGION");
                String s3UrlBase = responseJson.getString("s3URLBASE");
                String s3Bucket = responseJson.getString("s3BUCKET");
                String s3Id = responseJson.getString("s3ID");
                autoCallerDb.setS3Info(s3Region, s3Bucket, s3UrlBase, s3UploadFolder, s3Id);

                Log.i(TAG, "S3 info from payload -  region:"+s3Region+",bucket:"+s3Bucket+
                        ", folder:"+s3UploadFolder+", s3url: "+s3UrlBase+", s3Id:"+s3Id);

                String dlrCode = responseJson.getString("dlrCode");
                String phoneNumber = responseJson.getString("phoneNumber");
                //String phoneNumber = "";
                autoCallerDb.setDlrCode(dlrCode);
                autoCallerDb.setDeviceRegisteredPhoneNo(phoneNumber);
                autoCallerDb.setDeviceRegistrationCompleted(true);
                autoCallerDb.save();

                updatePhoneAndDealerCodeInUI(phoneNumber, dlrCode);

                Toast toast = Toast.makeText(getApplicationContext(),
                        "This user is now registered.",
                        Toast.LENGTH_LONG);
                LogBuffer.getInstance().createLogEntry("This user is now registered.");

                toast.show();
            }catch (Exception ex){
                Log.e(TAG, "Could not parse JSON response - "+responseBody );

                LogBuffer.getInstance().createLogEntry("Registration did not succeed. Please try again");
                Toast toast = Toast.makeText(getApplicationContext(),
                        "The device registration did not succeed perhaps due to temporary issues. Please try again.",
                        Toast.LENGTH_LONG);

                toast.show();
            }



        } else{
            Toast toast = Toast.makeText(getApplicationContext(),
                    "The user registration failed. Please try again",
                    Toast.LENGTH_LONG);
            LogBuffer.getInstance().createLogEntry("Registration failed. Please review your inputs.");

            toast.show();
        }


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
