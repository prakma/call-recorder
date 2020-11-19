package com.github.prakma.api;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.github.axet.callrecorder.BuildConfig;
//import com.github.axet.callrecorder.BuildConfig;

import java.io.File;
import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.time.ZonedDateTime;
//import java.time.format.DateTimeFormatter;

public class S3UploadClient {
    private static String TAG = "prakma.S3UploadClient";

    //private static DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static SimpleDateFormat SIMPLE_DATE_WITH_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat SIMPLE_DATE_WITHOUT_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static S3UploadClient _instance = new S3UploadClient();

//    private String s3Id;
//    private String s3Region;
//    private String s3Bucket;
//    private String s3Folder;
//    private String s3BaseURL;

    private S3UploadClient(){}

    public static S3UploadClient getInstance(){
        return _instance;
    }

//    public void setS3Info(String s3Id, String s3Region, String s3Bucket, String s3Folder,
//                          String s3BaseURL){
//
//    }


    public String uploadtos3 (final Context context, final File file,
                            String callRefId, String vin_no,
                            String call, String phone, String call_duration, String call_start_ts,
                            String tenant, String dlrCode, String creCode,
                            String s3Id, String s3Region, String s3Bucket,
                              String s3Folder, String s3BaseURL,
                            final S3ResponseCallback callback) {

//        String identityPoolId = BuildConfig.PropertyPairs.get(AutoCallerConstants.S3ID);
//        String s3Region = BuildConfig.PropertyPairs.get(AutoCallerConstants.S3REGION);
//        String bucketName = BuildConfig.PropertyPairs.get(AutoCallerConstants.S3BUCKET);

        String identityPoolId = s3Id;
        //String s3Region = s3Region;
        String bucketName = s3Bucket;

        Regions s3RegionObject = Regions.fromName(s3Region);

        //Log.i(TAG, "identitypoolid read from properties file is"+identityPoolId);
        Log.i(TAG, "S3 info for upload -  region:"+s3Region+",bucket:"+s3Bucket+
                ", folder:"+s3Folder+", s3url: "+s3BaseURL+", s3Id:"+s3Id);
//        Log.i(TAG, "identitypoolid read from properties file is"+identityPoolId+", bucket "+bucketName+", region "+s3Region);
//        Log.i(TAG, "Resolved region object for "+s3Region+" is "+testRegion);
//        Log.i(TAG, "Resolved region object for us-east-1  is "+Regions.fromName("us-east-1"));



        if(file !=null){
            CognitoCachingCredentialsProvider credentialsProvider;
            credentialsProvider = new CognitoCachingCredentialsProvider(
                    context,
                    identityPoolId, // Identity pool ID
                    Regions.fromName(s3Region) // Region
            );

            AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
            s3.setRegion(Region.getRegion(s3RegionObject));


            //Log.i(TAG, "file key for S3 upload = "+ get_folder_name_to_upload(tenant, dlrCode, call_start_ts) + "/" + file.getName());
            TransferUtility transferUtility = new TransferUtility(s3, context);
            ObjectMetadata objMetadata = new ObjectMetadata();

            objMetadata.addUserMetadata(AutoCallerConstants.FORM_CALLREFID, callRefId);
            //objMetadata.addUserMetadata(AutoCallerConstants.FORM_VIN_NO, vin_no);
            objMetadata.addUserMetadata(AutoCallerConstants.FORM_PHONE, phone);
            objMetadata.addUserMetadata(AutoCallerConstants.FORM_CALL_DURATION, call_duration);
            objMetadata.addUserMetadata(AutoCallerConstants.FORM_CALL_START_TS, call_start_ts);
            objMetadata.addUserMetadata(AutoCallerConstants.FORM_CUSTOM_FIELD, callRefId+"::"+creCode);
            //objMetadata.addUserMetadata(AutoCallerConstants.FORM_RECORDING_FILE, file.getName());

            String s3filePath = get_folder_name_to_upload(tenant, dlrCode, creCode, s3Folder, call_start_ts) + "/" + file.getName();
            Log.i(TAG, "file key for S3 upload = "+ s3filePath);
            final String s3URL = getS3BucketUrl(s3BaseURL, s3Region, bucketName, s3filePath);

            final TransferObserver observer = transferUtility.upload(
                    bucketName,  //this is the bucket name on S3
                    s3filePath,
                    file,
                    objMetadata
            );
            observer.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (state.equals(TransferState.COMPLETED)) {
                        Log.i(TAG, "Upload to S3 succeeded ! ");
                        callback.onSuccess(s3URL);
                    } else if (state.equals(TransferState.FAILED)) {
                        //Toast.makeText(context,"Failed to upload",Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Failed to upload to S3");
                        callback.onFailure();
                    }

                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    Log.e(TAG, "S3 upload on progress. bytesTotal "+bytesTotal);

                }

                @Override
                public void onError(int id, Exception ex) {
                    Log.e(TAG, "Failed to upload to S3. Exception is - "+ex, ex);
                    callback.onError(ex);
                }
            });

            return s3URL;
        } else {
            throw new RuntimeException("file was null. could not upload to s3. Investigate ! ");
        }
    }

    private String generate_date_based_subfolder_name(String call_start_ts){

//        java.time.LocalDate localDate = LocalDate.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(call_start_ts));
//        String dateSubfolder = DateTimeFormatter.BASIC_ISO_DATE.format(localDate);


        //java.time.LocalDate localDate = LocalDate.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(call_start_ts));
        //String dateSubfolder = DTF.format(localDate);
        try {
            java.util.Date localDate = SIMPLE_DATE_WITH_TIME_FORMAT.parse(call_start_ts);
            String dateSubfolder = SIMPLE_DATE_WITHOUT_TIME_FORMAT.format(localDate);
            return dateSubfolder;
        }catch(Exception ex){
            throw new RuntimeException("Could not parse datetimestring text "+call_start_ts, ex );
        }
    }

    private String get_folder_name_to_upload(String tenant, String dlrCode, String creCode, String s3FolderBase, String call_start_ts){
//        String uploadBaseFolder = BuildConfig.PropertyPairs.get(AutoCallerConstants.S3UPLOADFOLDER);
//        return uploadBaseFolder + "/" + sanitize(tenant) + "/" + sanitize(dlrCode)+ "/" + sanitize(creCode) + "/" +
//                generate_date_based_subfolder_name(call_start_ts);
        return s3FolderBase + "/" + generate_date_based_subfolder_name(call_start_ts);
    }

    private String sanitize(String creCode){
        if(creCode == null || creCode.trim().length()==0)
            return "CRE_UNKNOWN";
        return creCode.trim().replaceAll("[^a-zA-Z0-9_-]","_");
    }

    private String getS3BucketUrl(String s3BaseURL, String region, String bucket, String filePath){
//        return BuildConfig.PropertyPairs.get(AutoCallerConstants.S3URLBASE) +
//                "/" + bucket +
//                "/" + filePath;

        //return s3BaseURL + "/" + bucket + "/" + filePath;
        return s3BaseURL + "/" + filePath;
    }
}
