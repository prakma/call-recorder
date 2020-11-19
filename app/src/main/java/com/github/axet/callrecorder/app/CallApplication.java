package com.github.axet.callrecorder.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import androidx.preference.PreferenceManager;
import android.util.Log;

import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;
import com.github.axet.androidlibrary.app.NotificationManagerCompat;
import com.github.axet.androidlibrary.widgets.NotificationChannelCompat;
import com.github.axet.androidlibrary.widgets.OptimizationPreferenceCompat;
import com.github.axet.callrecorder.R;
import com.github.prakma.api.AutoCallerConstants;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
//import java.time.Instant;
//import java.time.ZoneId;
//import java.time.ZonedDateTime;
//import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class CallApplication extends com.github.axet.audiolibrary.app.MainApplication {
    public static final String PREFERENCE_DELETE = "delete";
    public static final String PREFERENCE_FORMAT = "format";
    public static final String PREFERENCE_CALL = "call";
    public static final String PREFERENCE_OPTIMIZATION = "optimization";
    public static final String PREFERENCE_NEXT = "next";
    public static final String PREFERENCE_DETAILS_CONTACT = "_contact";
    public static final String PREFERENCE_DETAILS_CALL = "_call";
    public static final String PREFERENCE_DETAILS_CALL_REF_ID = "_callRefId";
    public static final String PREFERENCE_SOURCE = "source";
    public static final String PREFERENCE_FILTER_IN = "filter_in";
    public static final String PREFERENCE_FILTER_OUT = "filter_out";
    public static final String PREFERENCE_DONE_NOTIFICATION = "done_notification";
    public static final String PREFERENCE_MIXERPATHS = "mixer_paths";
    public static final String PREFERENCE_VOICE = "voice";
    public static final String PREFERENCE_VOLUME = "volume";
    public static final String PREFERENCE_VERSION = "version";
    public static final String PREFERENCE_BOOT = "boot";
    public static final String PREFERENCE_INSTALL = "install";

    public static final String CALL_OUT = "out";
    public static final String CALL_IN = "in";

    public NotificationChannelCompat channelPersistent;
    public NotificationChannelCompat channelStatus;

    @SuppressWarnings("unchecked")
    @SuppressLint("PrivateApi")
    public static String getprop(String key) {
        try {
            Class klass = Class.forName("android.os.SystemProperties");
            Method method = klass.getMethod("get", String.class);
            return (String) method.invoke(null, key);
        } catch (Exception e) {
            Log.d(TAG, "no system prop", e);
            return null;
        }
    }

    public static CallApplication from(Context context) {
        return (CallApplication) com.github.axet.audiolibrary.app.MainApplication.from(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Bugfender.init(this, "UjYrttwkuDjL2ZEVRsgd9ckE6uk5DniR", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();
        Bugfender.enableUIEventLogging(this);
        //Bugfender.enableLogcatLogging();

        create_diagnostic_log();


        channelPersistent = new NotificationChannelCompat(this, "icon", "Persistent Icon", NotificationManagerCompat.IMPORTANCE_LOW);
        channelStatus = new NotificationChannelCompat(this, "status", "Status", NotificationManagerCompat.IMPORTANCE_LOW);

        OptimizationPreferenceCompat.setPersistentServiceIcon(this, true);

        switch (getVersion(PREFERENCE_VERSION, R.xml.pref_general)) {
            case -1:
                SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor e = shared.edit();
                MixerPaths m = new MixerPaths();
                if (!m.isSupported() || !m.isEnabled()) {
                    e.putString(CallApplication.PREFERENCE_ENCODING, Storage.EXT_3GP);
                }
                SharedPreferences.Editor edit = shared.edit();
                edit.putInt(PREFERENCE_VERSION, 2);
                edit.commit();
                break;
            case 0:
                version_0_to_1();
                break;
            case 1:
                version_1_to_2();
                break;
        }
    }

    void version_0_to_1() {
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = shared.edit();
        edit.putFloat(PREFERENCE_VOLUME, shared.getFloat(PREFERENCE_VOLUME, 0) + 1); // update volume from 0..1 to 0..1..4
        edit.putInt(PREFERENCE_VERSION, 1);
        edit.commit();
    }

    void version_1_to_2() {
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = shared.edit();
        edit.remove(PREFERENCE_SORT);
        edit.putInt(PREFERENCE_VERSION, 2);
        edit.commit();
    }

    public static String getContact(Context context, Uri f) {
        final SharedPreferences shared = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        String p = getFilePref(f) + PREFERENCE_DETAILS_CONTACT;
        return shared.getString(p, null);
    }

    public static void setContact(Context context, Uri f, String id) {
        final SharedPreferences shared = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        String p = getFilePref(f) + PREFERENCE_DETAILS_CONTACT;
        SharedPreferences.Editor editor = shared.edit();
        editor.putString(p, id);
        editor.commit();
    }

    public static String getCall(Context context, Uri f) {
        final SharedPreferences shared = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        String p = getFilePref(f) + PREFERENCE_DETAILS_CALL;
        return shared.getString(p, null);
    }

    public static void setCall(Context context, Uri f, String id) {
        final SharedPreferences shared = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        String p = getFilePref(f) + PREFERENCE_DETAILS_CALL;
        SharedPreferences.Editor editor = shared.edit();
        editor.putString(p, id);
        editor.commit();
    }

    public static String getCallRefId(Context context, Uri f){
        final SharedPreferences shared = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        String p = getFilePref(f) + PREFERENCE_DETAILS_CALL_REF_ID;
        return shared.getString(p, null);

    }

    public static void setCallRefIf(Context context, Uri f, String callRefId){
        final SharedPreferences shared = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        String p = getFilePref(f) + PREFERENCE_DETAILS_CALL_REF_ID;
        SharedPreferences.Editor editor = shared.edit();
        editor.putString(p, callRefId);
        editor.commit();
    }

    public static String getString(Context context, Locale locale, int id, Object... formatArgs) {
        return getStringNewRes(context, locale, id, formatArgs);
    }

    public static String getStringNewRes(Context context, Locale locale, int id, Object... formatArgs) {
        Resources res;
        Configuration conf = new Configuration(context.getResources().getConfiguration());
        if (Build.VERSION.SDK_INT >= 17)
            conf.setLocale(locale);
        else
            conf.locale = locale;
        res = new Resources(context.getAssets(), context.getResources().getDisplayMetrics(), conf);
        String str;
        if (formatArgs.length == 0)
            str = res.getString(id);
        else
            str = res.getString(id, formatArgs);
        new Resources(context.getAssets(), context.getResources().getDisplayMetrics(), context.getResources().getConfiguration()); // restore side effect
        return str;
    }

    public static String[] getStrings(Context context, Locale locale, int id) {
        Resources res;
        Configuration conf = new Configuration(context.getResources().getConfiguration());
        if (Build.VERSION.SDK_INT >= 17)
            conf.setLocale(locale);
        else
            conf.locale = locale;
        res = new Resources(context.getAssets(), context.getResources().getDisplayMetrics(), conf);
        String[] str;
        str = res.getStringArray(id);
        new Resources(context.getAssets(), context.getResources().getDisplayMetrics(), context.getResources().getConfiguration()); // restore side effect
        return str;
    }

    private static void test_code(){
        //        String currentDateTimeInISOFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
//                .format(Instant.now().atZone(ZoneId.systemDefault()));
//        Log.i("prkma - TAG", "currentDateTimeInISOFormat: "+currentDateTimeInISOFormat);

        //ZonedDateTime zdt = ZonedDateTime.now();
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        //SimpleDateFormat sdf = SimpleDateFormat.getInstance().format()
//        String dateSubfolder1 = DateTimeFormatter.ISO_DATE.format(zdt);
//        String dateSubfolder2 = DateTimeFormatter.ISO_LOCAL_DATE.format(zdt);
//        String dateSubfolder3 = DateTimeFormatter.ISO_ZONED_DATE_TIME.format(zdt);
//        String dateSubfolder4 = DateTimeFormatter.BASIC_ISO_DATE.format(zdt);

        Date now = new Date();
        String dateSubfolder1 = simpleDateFormat.format(now);

        Log.i(TAG, "isoDate subfolder for current time is "+dateSubfolder1);
//        Log.i(TAG, "isolocalDate subfolder for current time is "+dateSubfolder2);
//        Log.i(TAG, "isozonedDate subfolder for current time is "+dateSubfolder3);
//        Log.i(TAG, "basicisoDate subfolder for current time is "+dateSubfolder4);

        String dateWithTimePattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat simpleDateWithTimeFormat = new SimpleDateFormat(dateWithTimePattern);

        String dateSubfolder2 = simpleDateWithTimeFormat.format(now);

        Log.i(TAG, "datewithtime subfolder for current time is "+dateSubfolder2);

        try {
            Date reproducedDate = simpleDateWithTimeFormat.parse(dateSubfolder2);
            String dateSubfolder3 = simpleDateWithTimeFormat.format(reproducedDate);

            Log.i(TAG, "reproduced datewithtime subfolder for current time is " + dateSubfolder3);
        }catch(Exception ex){
            Log.e(TAG, ex.getMessage(), ex);
        }
    }

    private void create_diagnostic_log(){
        String mfr = Build.MANUFACTURER;
        String model = Build.MODEL;
        String product = Build.PRODUCT;
        String androidVersion = Build.VERSION.RELEASE;
        int androidSDKVersion = Build.VERSION.SDK_INT;

        Log.i(TAG, "Model- "+model+", Product-"+product+", androidVersion - "+androidVersion+", sdkVersion- "+androidSDKVersion);
    }
}
