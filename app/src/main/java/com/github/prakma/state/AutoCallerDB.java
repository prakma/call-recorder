package com.github.prakma.state;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoCallerDB {

    private static String TAG = "AutoCallerDB";
    private static String DB_FILE_KEY = "com.github.artemiscaller.settings";
    private static AutoCallerDB _instance = null;


    public static final String DEVICE_FIREBASE_TOKEN_ID = "com.github.artemiscaller.device_firebase_token_id";
    public static final String DEVICE_PHONE_NO = "com.github.artemiscaller.device_phone_no";
    public static final String DEVICE_REGISTRATION_STATUS_FLAG = "com.github.artemiscaller.device_registration_status_flag";
    public static final String LEAP_USER_EMAIL = "com.github.artemiscaller.leap_user_email";
    public static final String LEAP_TENANT_NAME = "com.github.artemiscaller.leap_tenant_name";



    public static final String LEAP_URI_BASE = "com.github.artemiscaller.leap_uri_base";
    public static final String IGNORE_SSL_FLAG = "com.github.artemiscaller.ignore_ssl_flag";

    public static final String CURRENT_CALL_REF_ID = "com.github.artemiscaller.current_call_ref_id";
    public static final String CURRENT_TARGET_PHONE_NO = "com.github.artemiscaller.current_target_phone_no";
    public static final String CURRENT_CALL_REC_SYNCED_FLAG = "com.github.artemiscaller.current_call_rec_synced_flag";

//    public static final String CALL_REF_ID_BASE = "com.github.artemiscaller.callrefid_";
//    public static final String RECORDING_FILE_BASE = "com.github.artemiscaller.recordingfile_";
//    public static final String CALL_REF_UPLOADQUEUEINDEX = "com.github.artemiscaller.uploadqueueindex";
//    private static final int UPLOADQUEUESIZE = 128; // excluding 0, queue index starts from 1, holds upto 128


    private Context callerAppContext;

    // initial-setup related

    private String deviceFirebaseTokenId;


    private boolean deviceRegistrationCompleted;


    private String deviceRegisteredPhoneNo;

    private String leapUserEmail;

    private String leapTenantName;

    // configuration related

    private String leapServerURIBase;


    private boolean ignoreSSL;


    // current-call related
    private String callReferenceId;


    private String targetPhoneNumber;


    private boolean callRecordsSynced; // did all records related to this callReference have been sync'ed with server?


    //



    public String getCallReferenceId() {
        return callReferenceId;
    }

    public void setCallReferenceId(String callReferenceId) {
        this.callReferenceId = callReferenceId;
    }

    public void resetCallReferenceId(){
        this.callReferenceId = "";
    }





    private AutoCallerDB(Context ctx){
        this.callerAppContext = ctx;
    }

    public String getDeviceFirebaseTokenId() {
        return deviceFirebaseTokenId;
    }

    public void setDeviceFirebaseTokenId(String deviceFirebaseTokenId) {
        this.deviceFirebaseTokenId = deviceFirebaseTokenId;
    }

    public boolean isDeviceRegistrationCompleted() {
        return deviceRegistrationCompleted;
    }

    public void setDeviceRegistrationCompleted(boolean deviceRegistrationCompleted) {
        this.deviceRegistrationCompleted = deviceRegistrationCompleted;
    }

    public String getDeviceRegisteredPhoneNo() {
        return deviceRegisteredPhoneNo;
    }

    public void setDeviceRegisteredPhoneNo(String deviceRegisteredPhoneNo) {
        this.deviceRegisteredPhoneNo = deviceRegisteredPhoneNo;
    }

    public String getLeapUserEmail() {
        return leapUserEmail;
    }

    public void setLeapUserEmail(String leapUserEmail) {
        this.leapUserEmail = leapUserEmail;
    }

    public String getLeapTenantName() {
        return leapTenantName;
    }

    public void setLeapTenantName(String leapTenantName) {
        this.leapTenantName = leapTenantName;
    }

    public String getLeapServerURIBase() {
        return leapServerURIBase;
    }

    public void setLeapServerURIBase(String leapServerURIBase) {
        this.leapServerURIBase = leapServerURIBase;
    }

    public boolean isIgnoreSSL() {
        return ignoreSSL;
    }

    public void setIgnoreSSL(boolean ignoreSSL) {
        this.ignoreSSL = ignoreSSL;
    }

    public String getTargetPhoneNumber() {
        return targetPhoneNumber;
    }

    public void setTargetPhoneNumber(String targetPhoneNumber) {
        this.targetPhoneNumber = targetPhoneNumber;
    }

    public boolean isCallRecordsSynced() {
        return callRecordsSynced;
    }

    public void setCallRecordsSynced(boolean callRecordsSynced) {
        this.callRecordsSynced = callRecordsSynced;
    }

    public void load(){
        SharedPreferences sharedPrefs = callerAppContext.getSharedPreferences(DB_FILE_KEY, Context.MODE_PRIVATE);

        deviceFirebaseTokenId = sharedPrefs.getString(DEVICE_FIREBASE_TOKEN_ID, "");
        deviceRegistrationCompleted = sharedPrefs.getBoolean(DEVICE_REGISTRATION_STATUS_FLAG, false);
        deviceRegisteredPhoneNo = sharedPrefs.getString(DEVICE_PHONE_NO, "");
        leapUserEmail = sharedPrefs.getString(LEAP_USER_EMAIL, "");
        leapTenantName = sharedPrefs.getString(LEAP_TENANT_NAME, "");

        // configuration related
        leapServerURIBase = sharedPrefs.getString(LEAP_URI_BASE, "https://leapapp.leadics.com/TENANTX/");
        ignoreSSL = sharedPrefs.getBoolean(IGNORE_SSL_FLAG, true);


        // current-call related
        callReferenceId = sharedPrefs.getString(CURRENT_CALL_REF_ID, "");
        targetPhoneNumber = sharedPrefs.getString(CURRENT_TARGET_PHONE_NO, "");
        // did all records related to this callReference have been sync'ed with server?
        callRecordsSynced = sharedPrefs.getBoolean(CURRENT_CALL_REC_SYNCED_FLAG, false);

    }

    public void loadCallRefIds(){
        SharedPreferences sharedPrefs = callerAppContext.getSharedPreferences(DB_FILE_KEY, Context.MODE_PRIVATE);
        //List<String> callRefIds =

    }


    public void save(){

        SharedPreferences sharedPrefs = callerAppContext.getSharedPreferences(DB_FILE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        editor.putString(DEVICE_FIREBASE_TOKEN_ID, deviceFirebaseTokenId);
        editor.putBoolean(DEVICE_REGISTRATION_STATUS_FLAG, deviceRegistrationCompleted);
        editor.putString(DEVICE_PHONE_NO, deviceRegisteredPhoneNo);
        editor.putString(LEAP_USER_EMAIL, leapUserEmail);
        editor.putString(LEAP_TENANT_NAME, leapTenantName);

        editor.putString(LEAP_URI_BASE, leapServerURIBase);
        editor.putBoolean(IGNORE_SSL_FLAG, ignoreSSL);

        editor.putString(CURRENT_CALL_REF_ID, callReferenceId);
        editor.putString(CURRENT_TARGET_PHONE_NO, targetPhoneNumber);
        editor.putBoolean(CURRENT_CALL_REC_SYNCED_FLAG, callRecordsSynced);


        editor.commit();

    }


/*
    public void addCallRefAndPersist(String callRefId, String fileName){
        // find the current callrefid's key
        // increment the key and create new entry

        SharedPreferences sharedPrefs = callerAppContext.getSharedPreferences(DB_FILE_KEY, Context.MODE_PRIVATE);

        int uploadQueueCurrentIndex = sharedPrefs.getInt(CALL_REF_UPLOADQUEUEINDEX, 0);
        int uploadQueueNextIndex = getNextIndex(uploadQueueCurrentIndex);
        String call_ref_id_key = CALL_REF_ID_BASE+uploadQueueNextIndex;
        String recording_file_name_key = RECORDING_FILE_BASE+uploadQueueNextIndex;

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(call_ref_id_key, callRefId);
        editor.putString(recording_file_name_key, fileName);
        editor.putInt(CALL_REF_UPLOADQUEUEINDEX, uploadQueueNextIndex);

        editor.commit();

    }

    public void removeCallRefAndPersist(String callRefId){

        SharedPreferences sharedPrefs = callerAppContext.getSharedPreferences(DB_FILE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        for(int i=1; i<=UPLOADQUEUESIZE; i++){
            String call_ref_id_key = CALL_REF_ID_BASE+i;
            String retrieved_call_ref_id = sharedPrefs.getString(call_ref_id_key, "");

            if(retrieved_call_ref_id.equals(callRefId)){
                String recording_file_name_key = RECORDING_FILE_BASE+i;
                editor.remove(call_ref_id_key);
                editor.remove(recording_file_name_key);
                editor.commit();
                break;
            }

        }
        editor.commit();
    }

    public Map<String, String> getNextRecordingFileAndCallRefId(){

        SharedPreferences sharedPrefs = callerAppContext.getSharedPreferences(DB_FILE_KEY, Context.MODE_PRIVATE);


        for(int i=1; i<=UPLOADQUEUESIZE; i++){
            String call_ref_id_key = CALL_REF_ID_BASE+i;
            String recording_file_name_key = RECORDING_FILE_BASE+i;
            String retrieved_call_ref_id = sharedPrefs.getString(call_ref_id_key, "");
            String retrieved_file_name = sharedPrefs.getString(recording_file_name_key, "");

            if(retrieved_call_ref_id.length()>0){

                Map<String,String> resultMap = new HashMap<>();
                resultMap.put("CallRefId", retrieved_call_ref_id);
                resultMap.put("FileName", retrieved_file_name);
                return resultMap;
            }
        }

        return null; // no recording+callrefId exist to upload

    }



    private int getNextIndex(int currentIndex){
        if(currentIndex < 128)
            return currentIndex + 1;
        else
            return 1 ; // reset the counter/index, start from beginning
    }

*/


    public static AutoCallerDB getInstance(Context ctx){
        if(_instance != null)
            return _instance;
        else {
            _instance = new AutoCallerDB(ctx);
            _instance.load();
            return _instance;
        }
    }


}
