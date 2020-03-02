package com.github.prakma.api;

import android.net.Uri;
import android.util.Log;



import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public abstract class ServerApiBase {

    private static final String TAG = "ServerApiBase";
    //private static ServerApiBase _instance = new ServerApiBase();


    public static final String SEPARATOR = ":::";
    public static final String HMAC_HEADER_FORMAT = "%s" + SEPARATOR + "%s";
    public static final String HMAC_HEADER = "X-LEAP-HMAC";
    public static final String HMAC_VALUE = "X-LEAP-VALUE";
    public static final String TENANT_HEADER = "X-LEAP-TENANT";
    public static final String CONTENT_TYPE = "application/json";
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    public static final String SECRET_KEY = "LmVJcmzfXP1LTLCnIga3ePtF5hH21ryH";

    private static final TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }
    };
    private static final SSLContext trustAllSslContext;
    static {
        try {
            trustAllSslContext = SSLContext.getInstance("SSL");
            trustAllSslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }
    private static final SSLSocketFactory trustAllSslSocketFactory = trustAllSslContext.getSocketFactory();

    public static OkHttpClient trustAllSslClient(OkHttpClient client) {
        Log.w(TAG, "Using the trustAllSslClient is highly discouraged and should not be used in Production!");
        OkHttpClient.Builder builder = client.newBuilder();
        builder.sslSocketFactory(trustAllSslSocketFactory, (X509TrustManager)trustAllCerts[0]);
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
        return builder.build();
    }

    public OkHttpClient getNewOkHttpClient(boolean ignoreSSL){

        OkHttpClient client = new OkHttpClient();
        if(ignoreSSL)
            client = trustAllSslClient(client);
        return client;
    }

    protected String calculateMD5(String contentToEncode) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("digestion problem");
        }
        digest.update(contentToEncode.getBytes());

        String result = new String(org.apache.commons.codec.binary.Base64.encodeBase64(digest.digest()));
        //String result = new String(Base64.encode(digest.digest(),Base64.DEFAULT));
        return result;
    }

    protected String calculateHMAC(String secret, String data) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA1_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            //String result = new String(Base64.encode(rawHmac, Base64.DEFAULT));
            String result = new String(org.apache.commons.codec.binary.Base64.encodeBase64(rawHmac));
            return result;
        } catch (Exception e) {
            Log.w(TAG, "Unexpected error while creating hash: " + e.getMessage(), e);
            throw new RuntimeException("Problem calculating HMAC");
        }
    }





}
