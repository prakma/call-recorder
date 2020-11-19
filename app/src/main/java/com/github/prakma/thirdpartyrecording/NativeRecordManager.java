package com.github.prakma.thirdpartyrecording;

import android.os.Environment;
import android.util.Log;

import com.bugfender.sdk.Bugfender;

import java.io.File;
// import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class NativeRecordManager {

    private static final String TAG = "prakma.NativeRecordManager";

    private HashMap<String,String> fileExtension_mimeType_map = new HashMap();
    private List<String> recordingLocationsList = new ArrayList<>();
    Comparator<File> compareFilesByRecency = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            return (int)(o2.lastModified() - o1.lastModified());
        }
    };

    private static NativeRecordManager _instance = new NativeRecordManager();

    private void setupExtensionToMimeTypeMap(){
        fileExtension_mimeType_map.put("ogg", "audio/ogg; codecs=vorbis");
        fileExtension_mimeType_map.put("m4a", "audio/mp4");
        fileExtension_mimeType_map.put("amr", "audio/amr");
    }

    private void setupRecordingLocationsList(){

        String [] prefinedFolderLocations = {
                "Call",
                "Record/PhoneRecord"};

        for (int i=0, j=prefinedFolderLocations.length; i<j; i++){
            recordingLocationsList.add(prefinedFolderLocations[i]);
        }

    }
    private void addToRecordingLocationsList(String nativeRecordLocationHint){
        if(nativeRecordLocationHint == null || nativeRecordLocationHint.trim().equals(""))
            return;
        if(! recordingLocationsList.contains(nativeRecordLocationHint)){
            recordingLocationsList.add(0, nativeRecordLocationHint);
        }
    }

    private NativeRecordManager(){
        setupExtensionToMimeTypeMap();
        setupRecordingLocationsList();
    }

    public static NativeRecordManager getInstance(String nativeRecordLocationHint){
        _instance.addToRecordingLocationsList(nativeRecordLocationHint);
        return _instance;
    }

    public File getMostRecentRecordingOfGivenPhoneCall(String phone, String contactName, Date call_start_time){
//        File rootFilesDir = Environment.getExternalStorageDirectory();
//        Log.i(TAG, "Root Folder's is at - "+ rootFilesDir.getAbsolutePath());
//
//        String audioSavePathInDevice =
//                rootFilesDir.getAbsolutePath() + "/" +
//                        "Call";
//        File callFolder = new File(audioSavePathInDevice);
//        //String [] childFiles = rootFilesDir.list();
//        File [] callFolderFiles = callFolder.listFiles();

        File [] callFolderFiles = huntPossibleRecordingLocationsAndGetRecordings();

        File candidateFile = null;

        if(callFolderFiles != null){
            Arrays.sort(callFolderFiles, compareFilesByRecency);
            for(int i=0; i<callFolderFiles.length;i++){
                Log.i(TAG, "Call Folder's file - "+ callFolderFiles[i].getAbsolutePath());

                Bugfender.i(TAG, "Call Folder's file  - "+ callFolderFiles[i].getAbsolutePath() +" check for a match");

                // check if this file's lastModifiedTime is within reasonable limits of few seconds
                // check if the file name has the phone number as substring
                if( candidateFile == null &&
                        matchFileByPhoneNumberOrContactName(callFolderFiles[i], phone, contactName) &&
                        secondsElapsed(callFolderFiles[i], call_start_time ) < 20){

                    Bugfender.i(TAG, "Match succeeded for Call Folder's file  - "+ callFolderFiles[i].getAbsolutePath());

                    // this is our candidate file
                    candidateFile = callFolderFiles[i];

                } else {

                    Bugfender.i(TAG, "Match was either unnecessary or failed. Will delete if too old. file  - "+ callFolderFiles[i].getAbsolutePath());
                    // delete the older files
                    deleteIfOlderThanADay(callFolderFiles[i]);

                    //Log.i(TAG, "Call Folder's file deleted. timestamp - "+ callFolderFiles[i].lastModified());
                }

            }
        } else{

            Log.i(TAG, "No recordings found in all possible locations");
            Bugfender.i(TAG, "No recordings found in all possible locations");

        }

        return candidateFile;
    }

    private File[] huntPossibleRecordingLocationsAndGetRecordings(){
        File rootFilesDir = Environment.getExternalStorageDirectory();
        Log.i(TAG, "Root Folder's is at - "+ rootFilesDir.getAbsolutePath());
        Bugfender.i(TAG, "Root Folder's is at - "+ rootFilesDir.getAbsolutePath());

        File [] recordingFiles = null;

        for (int i=0,j=recordingLocationsList.size(); i<j;i++){
            String folderLocationPotential = recordingLocationsList.get(i);
            recordingFiles = checkIfRecordingsExistInThisLocation(rootFilesDir, folderLocationPotential);
            if(recordingFiles != null){
                Log.i(TAG, "Found recording at "+ folderLocationPotential);
                Bugfender.i(TAG, "Found recording at  "+ folderLocationPotential);
                break;
            }

        }

        if(recordingFiles == null){
            Log.i(TAG, "No recordings files found under -  "+ rootFilesDir.getAbsolutePath());
            Bugfender.i(TAG, "No recordings files found under -  "+ rootFilesDir.getAbsolutePath());
        }

        return recordingFiles;



//        String audioSavePathInDevice =
//                rootFilesDir.getAbsolutePath() + "/" +
//                        "Call";
//        File callFolder = new File(audioSavePathInDevice);
//        //String [] childFiles = rootFilesDir.list();
//        File [] callFolderFiles = callFolder.listFiles();
    }

    private File [] checkIfRecordingsExistInThisLocation(File baseDir, String folderLocation){
        String recordingPath =
                baseDir.getAbsolutePath() + "/" + folderLocation;

        File callRecordingFolder = new File(recordingPath);
        if(callRecordingFolder.exists()){

            File [] callFolderFiles = callRecordingFolder.listFiles();
            if(callFolderFiles != null && callFolderFiles.length > 0){
                Log.i(TAG, "Found Recordings exist at this location - " + callRecordingFolder.getAbsolutePath());
                Bugfender.i(TAG, "Found Recordings exist at this location - " + callRecordingFolder.getAbsolutePath());
                return callFolderFiles;
            } else {

                Log.i(TAG, "No child files/dirs exist at this root - " + recordingPath);
                Bugfender.i(TAG, "No child files/dirs exist at this root - " + recordingPath);

                return null;
            }
        } else {
//            try {
//                Log.i(TAG, "No child files/dirs exist at this root - " + callRecordingFolder.getAbsolutePath());
//            }catch (Exception ex){
//                Log.e(TAG,"Exception while seeing child dirs", ex);
//            }
            Log.i(TAG, "No child files/dirs exist at this root - " + recordingPath);
            Bugfender.i(TAG, "No child files/dirs exist here - " + recordingPath);
            return null;
        }

    }

    private void deleteIfOlderThanADay(File candidateFile){
        if(candidateFile == null) return;

        long whenSaved = candidateFile.lastModified();
        long currTimeNow = System.currentTimeMillis();
        long millisElapsed = currTimeNow - whenSaved;

        //this case should never occur where an existing file's lastmodified time is in future
        if(millisElapsed < 0) return;

        long hrsElapsed = millisElapsed/(1000 * 3600) ;
        Log.i(TAG, "Call Folder's file-"+candidateFile.getName()+" is older than "+ hrsElapsed+" hrs");

        if( hrsElapsed > 24 ){
            // delete the file
            candidateFile.delete();
            Log.i(TAG, "Call Folder's file-"+candidateFile.getName()+"- deleted, because it is older than "+ hrsElapsed);
        }
    }

    private long secondsElapsed(File candidateFile, Date fromThisTime){
        if(candidateFile == null) throw new IllegalArgumentException("candidateFile cannot be null");

        long whenSaved = candidateFile.lastModified();
        //long fromRefTime = fromThisTime.toInstant().toEpochMilli();
        long fromRefTime = fromThisTime.getTime();
        long millisElapsed = fromRefTime - whenSaved;

        long secondsElapsed = millisElapsed/(1000);
        Log.i(TAG,"seconds elapsed from "+fromThisTime+" and the file last modified time "+candidateFile.getName()+" is "+secondsElapsed  );
        Bugfender.i(TAG,"seconds elapsed from "+fromThisTime+" and the file last modified time "+candidateFile.getName()+" is "+secondsElapsed  );
        return secondsElapsed;
    }

    private boolean matchFileByPhoneNumberOrContactName( File candidateFile, String phoneNumberStr, String contactName ){
        if(phoneNumberStr == null || phoneNumberStr.trim().equals("")) return false;
        if(candidateFile == null) return false;

        String sanitizedFileName = sanitizeString(candidateFile.getName());
        String sanitizedPhoneStr = sanitizeString(phoneNumberStr);
        String sanitizedContactName = sanitizeString(contactName);

        Log.i(TAG, "filename "+sanitizedFileName+",contactName-"+ contactName+ ",phone-"+phoneNumberStr);
        Bugfender.i(TAG, "going to match - filename "+sanitizedFileName+",contactName-"+ sanitizedContactName+ ",phone-"+sanitizedPhoneStr);

        // extract_prefix_suffix etc from the name
        if(sanitizedFileName.indexOf(sanitizedPhoneStr) > -1)
            return true;
        else if(contactName != null && ! contactName.equals("") && sanitizedFileName.indexOf(sanitizedContactName) > -1 )
            return true;
        else return false;

    }

    private String sanitizeString(String input){
        if(input == null || input.equals("")) return input;
        String candidate = input.trim().toLowerCase();
        // match all non-alphanumeric chars and replace it with empty space
        return candidate.replaceAll("[^A-Za-z0-9]", "");
    }
}
