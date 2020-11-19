package com.github.prakma.api;

public abstract class S3ResponseCallback {


    private boolean flag_uploadError = false;

    public abstract void onSuccess(String s3URL);

    public abstract void onFailure();

    public abstract void onError(Exception ex);

//    public void setS3_URL(String s3_url) {
//        this.s3URL = s3_url;
//    }
//
//    public String getS3_URL(){
//        return this.s3URL;
//    }
    public boolean isError() {
        return flag_uploadError;
    }

    public void setError(){
        flag_uploadError = true;

    }
}
