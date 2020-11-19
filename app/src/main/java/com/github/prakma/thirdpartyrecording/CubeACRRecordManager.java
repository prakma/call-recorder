package com.github.prakma.thirdpartyrecording;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class CubeACRRecordManager {
    private static final String TAG = "prakma.CubeRecordManager";

    private static String CUBE_ACR_RECORD_PATH = "CubeCallRecorder/All";

    private static CubeACRRecordManager _instance = new CubeACRRecordManager();

    public static CubeACRRecordManager getInstance(){
        return _instance;
    }

    private CubeACRRecordManager(){

    }

    public void deleteAllCubeACRFiles(){

        File rootFilesDir = Environment.getExternalStorageDirectory();
        Log.i(TAG, "Root Folder's is at - "+ rootFilesDir.getAbsolutePath());



        String cubeRecordingFolder =
                rootFilesDir.getAbsolutePath() + "/" +
                        CUBE_ACR_RECORD_PATH;
        File callFolder = new File(cubeRecordingFolder);

        File [] callFolderFiles = callFolder.listFiles();

        if(callFolderFiles != null){
            //Arrays.sort(callFolderFiles, compareFilesByRecency);
            for(int i=0; i<callFolderFiles.length;i++){
                Log.i(TAG, "CubeACR Folder's file - "+ callFolderFiles[i].getAbsolutePath());
                callFolderFiles[i].delete();
                Log.i(TAG, "CubeACR Folder's file deleted. ");

            }
        } else{
            try {
                Log.i(TAG, "No child files/dirs exist at this root - " + callFolder.getCanonicalPath());
            }catch (Exception ex){
                Log.e(TAG,"Exception while seeing CUBE Recorder's child dirs", ex);
            }
        }
    }


}
