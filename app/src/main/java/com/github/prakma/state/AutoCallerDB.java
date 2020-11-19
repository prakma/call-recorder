package com.github.prakma.state;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bugfender.sdk.Bugfender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoCallerDB {

    private static String TAG = "prakma.AutoCallerDB";
    private static String DB_FILE_KEY = "com.github.artemiscaller.settings";
    private static AutoCallerDB _instance = null;

    private static int PHONE_LIST_SIZE = 16;


    public static final String DEVICE_FIREBASE_TOKEN_ID = "com.github.artemiscaller.device_firebase_token_id";
    public static final String DEVICE_PHONE_NO = "com.github.artemiscaller.device_phone_no";
    public static final String DEVICE_REGISTRATION_STATUS_FLAG = "com.github.artemiscaller.device_registration_status_flag";
    public static final String LEAP_USER_EMAIL = "com.github.artemiscaller.leap_user_email";
    public static final String LEAP_TENANT_NAME = "com.github.artemiscaller.leap_tenant_name";
    public static final String LEAP_DLR_CODE = "com.github.artemiscaller.leap_dlr_code";



    public static final String LEAP_URI_BASE = "com.github.artemiscaller.leap_uri_base";
    public static final String LEAP_S3_ID = "com.github.artemiscaller.leap_s3_id";
    public static final String LEAP_S3_BUCKET = "com.github.artemiscaller.leap_s3_bucket";
    public static final String LEAP_S3_FOLDER = "com.github.artemiscaller.leap_s3_folder";
    public static final String LEAP_S3_REGION = "com.github.artemiscaller.leap_s3_region";
    public static final String LEAP_S3_URL = "com.github.artemiscaller.leap_s3_url";

    public static final String IGNORE_SSL_FLAG = "com.github.artemiscaller.ignore_ssl_flag";

    public static final String CURRENT_CALL_REF_ID = "com.github.artemiscaller.current_call_ref_id";
    public static final String CURRENT_TARGET_PHONE_NO = "com.github.artemiscaller.current_target_phone_no";
    public static final String CURRENT_TARGET_VIN_NO = "com.github.artemiscaller.current_target_vin_no";
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

    private String dlrCode;

    // configuration related

    private String leapServerURIBase;
    private String recordLocationHint;

    private String s3Region;
    private String s3Bucket;
    private String s3BaseUrl;
    private String s3Folder;
    private String s3Id;



    private boolean ignoreSSL;


    // current-call related
    private String callReferenceId;


    private String targetPhoneNumber;
    private String targetVinNumber;


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

    public String getDlrCode() {
        return dlrCode;
    }

    public void setDlrCode(String dlrCode) {
        this.dlrCode = dlrCode;
    }

    public String getLeapServerURIBase() {
        return leapServerURIBase;
    }

    public void setLeapServerURIBase(String leapServerURIBase) {
        this.leapServerURIBase = leapServerURIBase;
    }

    public String getRecordLocationHint(){
        return recordLocationHint;
    }

    public void setRecordLocationHint(String locationHint){
         this.recordLocationHint = locationHint;
    }

    public void setS3Info(String s3Region, String s3Bucket,
                          String s3BaseUrl, String s3Folder,
                          String s3Id ){

        this.s3Region = s3Region;
        this.s3Bucket = s3Bucket;
        this.s3BaseUrl = s3BaseUrl;
        this.s3Id = s3Id;
        this.s3Folder = s3Folder;

    }

    public boolean isS3InfoAvailable(){
        String [] s3Info = {s3Region, s3Bucket, s3BaseUrl, s3Id, s3Folder};
        if( notnull_notempty_check(s3Info) ) {
            return true;
        } else {
            return false;
        }
    }



    public String getS3Region(){
        return s3Region;
    }
    public String getS3Bucket(){
        return s3Bucket;
    }
    public String getS3BaseURL(){
        return s3BaseUrl;
    }
    public String getS3Folder() {
        return s3Folder;
    }
    public String getS3Id(){
        return s3Id;
    }

    public boolean isIgnoreSSL() {
        return ignoreSSL;
    }

    public void setIgnoreSSL(boolean ignoreSSL) {
        this.ignoreSSL = ignoreSSL;
    }

    public void resetDeviceRegistration(){
        this.s3Region = "";
        this.s3Bucket = "";
        this.s3BaseUrl = "";
        this.s3Id = "";
        this.s3Folder = "";
        setS3Info("", "", "", "", "");
        setDeviceRegistrationCompleted(false);
    }

//    public String getTargetPhoneNumber() {
//        return targetPhoneNumber;
//    }

    public void setTargetPhoneNumber(String targetPhoneNumber) {
        // make sure the phone number does not have comma in it.
        if(targetPhoneNumber == null || targetPhoneNumber.indexOf(",") > -1){
            throw new RuntimeException("calling phone no should not be blank or contain comma ! "+targetPhoneNumber);
        }

        // commented because this would not work for Android 6/7
        //List<String> phone_as_list = new ArrayList<String>(Arrays.asList(this.targetPhoneNumber.split(",")));

        String[] phones_as_array = this.targetPhoneNumber.split(",");
        List<String> phone_as_list = new ArrayList<String>();
        if(phones_as_array != null){
            for(int i=0, j=phones_as_array.length; i<j;i++){
                phone_as_list.add(phones_as_array[i]);
            }
        }


        // check if we are not beyond our PHONE_LIST_SIZE limit
        if(phone_as_list.size() < PHONE_LIST_SIZE){
            phone_as_list.add(targetPhoneNumber);
        } else {
            // remove the first phone ( ie, the oldest phone )
            phone_as_list.remove(0);
            // then add the new phone at last
            phone_as_list.add(targetPhoneNumber);
        }

        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0, j=phone_as_list.size(); i<j; i++ ){
            stringBuilder.append(phone_as_list.get(i));
            stringBuilder.append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);

        // this code would not work for android 6/7
        //this.targetPhoneNumber = String.join(",", phone_as_list);

        this.targetPhoneNumber = stringBuilder.toString();

    }

    public boolean isRelevantPhoneNo(String one_phone_no){
        List<String> phone_as_list = Arrays.asList(this.targetPhoneNumber.split(","));
        if(phone_as_list == null) return false;

        for(int i=0,j=phone_as_list.size(); i<j; i++){
            String phone2 = phone_as_list.get(i);
            if(PhoneUtil.phone_matches(one_phone_no, phone2)){
                return true;
            }
        }
        return false;

        //todo - remove always true return, and uncomment the actual code which is currently commented out for my testing
        //return true;
    }

    public boolean isLatestPhoneNo(String one_phone_no){
        List<String> phone_as_list = Arrays.asList(this.targetPhoneNumber.split(","));
        if(phone_as_list == null || phone_as_list.size() == 0)
            return false;

        String latestNumber = phone_as_list.get(phone_as_list.size() - 1);

        //return latestNumber.equals(one_phone_no);
        return PhoneUtil.phone_matches(latestNumber, one_phone_no);
    }

    public String getTargetVinNumber() {
        return targetVinNumber;
    }

    public void setTargetVinNumber(String targetVinNumber) {
        this.targetVinNumber = targetVinNumber;
    }

    public boolean isCallRecordsSynced() {
        return callRecordsSynced;
    }

    public void setCallRecordsSynced(boolean callRecordsSynced) {
        this.callRecordsSynced = callRecordsSynced;
    }


    /**
     * returns true only if each and every item in the input list is non-null and nonempty
     * @param stringBulk
     * @return
     */
    private static boolean notnull_notempty_check (String[] stringBulk){
        for(int i = 0, j = stringBulk.length; i < j; i++){
            String oneItem = stringBulk[i];
            if(oneItem == null || oneItem.trim().equals(""))
                return false;
        }
        return true;
    }

    public void load(){
        SharedPreferences sharedPrefs = callerAppContext.getSharedPreferences(DB_FILE_KEY, Context.MODE_PRIVATE);

        deviceFirebaseTokenId = sharedPrefs.getString(DEVICE_FIREBASE_TOKEN_ID, "");
        deviceRegistrationCompleted = sharedPrefs.getBoolean(DEVICE_REGISTRATION_STATUS_FLAG, false);
        deviceRegisteredPhoneNo = sharedPrefs.getString(DEVICE_PHONE_NO, "");
        leapUserEmail = sharedPrefs.getString(LEAP_USER_EMAIL, "");
        leapTenantName = sharedPrefs.getString(LEAP_TENANT_NAME, "");
        dlrCode = sharedPrefs.getString(LEAP_DLR_CODE, "");

        // configuration related
        leapServerURIBase = sharedPrefs.getString(LEAP_URI_BASE, "https://leapappapi.leadics.com/leap-clv/");
        s3BaseUrl = sharedPrefs.getString(LEAP_S3_URL, "");
        s3Bucket = sharedPrefs.getString(LEAP_S3_BUCKET, "");
        s3Folder = sharedPrefs.getString(LEAP_S3_FOLDER, "");
        s3Region = sharedPrefs.getString(LEAP_S3_REGION, "");
        s3Id = sharedPrefs.getString(LEAP_S3_ID, "");

        ignoreSSL = sharedPrefs.getBoolean(IGNORE_SSL_FLAG, true);


        // current-call related
        callReferenceId = sharedPrefs.getString(CURRENT_CALL_REF_ID, "");
        targetPhoneNumber = sharedPrefs.getString(CURRENT_TARGET_PHONE_NO, "");
        targetVinNumber = sharedPrefs.getString(CURRENT_TARGET_VIN_NO, "");
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
        editor.putString(LEAP_DLR_CODE, dlrCode);

        editor.putString(LEAP_URI_BASE, leapServerURIBase);
        editor.putString(LEAP_S3_URL, s3BaseUrl);
        editor.putString(LEAP_S3_BUCKET, s3Bucket);
        editor.putString(LEAP_S3_FOLDER, s3Folder);
        editor.putString(LEAP_S3_REGION, s3Region);
        editor.putString(LEAP_S3_ID, s3Id);

        editor.putBoolean(IGNORE_SSL_FLAG, ignoreSSL);

        editor.putString(CURRENT_CALL_REF_ID, callReferenceId);
        editor.putString(CURRENT_TARGET_PHONE_NO, targetPhoneNumber);
        editor.putString(CURRENT_TARGET_VIN_NO, targetVinNumber);
        editor.putBoolean(CURRENT_CALL_REC_SYNCED_FLAG, callRecordsSynced);

        // Associate a string value to the device
        Bugfender.setDeviceString("user.email", leapUserEmail);




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

    private static class PhoneUtil {
        /**
         * same phone no could be in several different formats -
         * starting with country code. for rx, +918834345677 or
         * or just the phone number - 8834345677, or
         * or beginning with 0, ie, 08834345677
         * this matching algorithm should be able to match these
         * to be same phone number
         *
         * simple algo for now -
         * reverse both the given phone numbers
         * if the first nine digits on the reversed numbers match, then it is most likely
         * same phone
         *
         * @param phone1
         * @param phone2
         * @return
         */
        public static boolean phone_matches(String phone1, String phone2){

            // two empty or null phone numbers does not mean that they match !
            if(phone1 == null || phone1.trim().length() == 0)
                return false;

            if(phone2 == null || phone2.trim().length() == 0)
                return false;

            // strip non alphanumeric like dot or dashes from the phones
            // so that 425-440-1133 or 425.440.1153 is converted to 4254401153


            String phone1Cleaned = phone1.replaceAll("[^a-zA-Z0-9]", "");
            String phone1Reversed = new StringBuilder(phone1Cleaned).reverse().toString();
            int maxLengthForChecking = phone1Reversed.length() > 10 ? 9: phone1Reversed.length()-2;
            //String phone1Chars9 = phone1Reversed.substring(0,9);
            String phone1Chars9 = phone1Reversed.substring(0, maxLengthForChecking);

            String phone2Cleaned = phone2.replaceAll("[^a-zA-Z0-9]", "");
            String phone2Reversed = new StringBuilder(phone2Cleaned).reverse().toString();
            int maxLength2ForChecking = phone2Reversed.length() > 10 ? 9: phone2Reversed.length()-2;
            //String phone2Chars9 = phone2Reversed.substring(0,9);
            String phone2Chars9 = phone2Reversed.substring(0, maxLength2ForChecking);

            return phone1Chars9.equalsIgnoreCase(phone2Chars9);

        }
    }


}
