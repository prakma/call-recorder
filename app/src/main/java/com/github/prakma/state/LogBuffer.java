package com.github.prakma.state;

import android.os.Build;

//import com.bugfender.sdk.Bugfender;

//import java.io.File;
import com.github.axet.callrecorder.BuildConfig;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogBuffer {

    private static SimpleDateFormat SIMPLE_DATE_WITH_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static LogBuffer _instance = new LogBuffer();
    public static LogBuffer getInstance(){
        return _instance;
    }
    private List<String> recentLogStmts = new ArrayList<String>();
    private boolean recordingCheckBoxState = false;
    private boolean registrationState = false;
    private String deviceId = "Unknown";
    private LogBuffer() {
        createOrUpdateDiagnosticLog();
    }

    private void add_first_diagnostic_log(StringBuilder stringBuilder){
        String mfr = Build.MANUFACTURER;
        String model = Build.MODEL;
        String product = Build.PRODUCT;
        String androidVersion = Build.VERSION.RELEASE;
        int artemisCallerVersionCd = BuildConfig.VERSION_CODE;
        String artemisCallerVersion = BuildConfig.VERSION_NAME;


        stringBuilder.append(mfr);
        stringBuilder.append("/");

        stringBuilder.append(model);
        stringBuilder.append("/");

        stringBuilder.append(product);
        stringBuilder.append("/");

        stringBuilder.append("androidVersion ");
        stringBuilder.append(androidVersion);
        stringBuilder.append("/");

        stringBuilder.append("ArtemisCaller version ");
        stringBuilder.append(artemisCallerVersion);
        stringBuilder.append("/");

        stringBuilder.append("Device ");
        stringBuilder.append(deviceId);
        stringBuilder.append("/");

        stringBuilder.append("Registration ");
        stringBuilder.append(registrationState);
        stringBuilder.append("/");

        stringBuilder.append("Recording ");
        stringBuilder.append(recordingCheckBoxState);
        stringBuilder.append("/");

        stringBuilder.append(" ");

    }

    public void createRegistrationLog(String deviceToken, /*boolean recordingFlag,*/ boolean registrationCompletedFlag){
        deviceId = deviceToken;
        registrationState = registrationCompletedFlag;
        //recordingCheckBoxState = recordingFlag;
        createOrUpdateDiagnosticLog();
        if(registrationState)
            createLogEntry("Registration is completed");
        else {
            createLogEntry("Registration is incomplete");
        }

    }

    private void createOrUpdateDiagnosticLog(){
        StringBuilder stringBuilder = new StringBuilder();
        createTimeStamp(stringBuilder);
        add_first_diagnostic_log(stringBuilder);
        stringBuilder.append("\n");

        if( recentLogStmts.size() > 0 )
            recentLogStmts.remove(0);
        recentLogStmts.add(0, stringBuilder.toString());
    }

    private void createTimeStamp(StringBuilder stringBuilder){
        stringBuilder.append(SIMPLE_DATE_WITH_TIME_FORMAT.format(new Date()));
        stringBuilder.append(" - ");
    }

    public void createLogEntry(String str){
        StringBuilder stringBuilder = new StringBuilder();
        createTimeStamp(stringBuilder);
        stringBuilder.append(str);
        stringBuilder.append("\n");

        if(recentLogStmts.size()<16){
            recentLogStmts.add(stringBuilder.toString());
        }
        else {
            recentLogStmts.remove(1);
            recentLogStmts.add(stringBuilder.toString());
        }

    }

    public void setRecordingCheckboxState(boolean stateFlag){
        recordingCheckBoxState = stateFlag;
        createOrUpdateDiagnosticLog();
    }

    public List<String> getRecentLogStmts(){
        return recentLogStmts;
    }

    public String getRecentLogStmtsAsString(){
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0,j=recentLogStmts.size();i<j;i++){
            stringBuilder.append(recentLogStmts.get(i));
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

//    public void resetLogs(){
//        recentLogStmts.clear();
//    }

}
