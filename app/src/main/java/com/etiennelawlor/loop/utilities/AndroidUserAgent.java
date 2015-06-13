package com.etiennelawlor.loop.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.etiennelawlor.loop.R;

import java.util.UUID;

import timber.log.Timber;

public class AndroidUserAgent {

    public final String appLabel;
    public final String packageVersion;
    public final String packageName;
    public final String platform;

    public final String device = Build.DEVICE;
    public final String osVersion = Build.VERSION.SDK;

    public final String uniqueId;

    private static AndroidUserAgent singleton;

    private AndroidUserAgent(String appLabel, String packageVersion, String packageName, String uniqueId, String platform) {
        this.appLabel = appLabel;
        this.uniqueId = uniqueId;
        this.packageName = packageName;
        this.packageVersion = packageVersion;
        this.platform = platform;
    }

    //Make sure that we can only grab one android user agent per context to save memory and avoid bugs.
    synchronized public static AndroidUserAgent getUserAgent(Context context) {
        if (singleton == null) {
            String uid = getUniqueId(context);
            String platform = context.getString(R.string.platform);
            String packageName = context.getPackageName();
            PackageManager pm = context.getPackageManager();
            try {
                PackageInfo pi = pm.getPackageInfo(packageName, 0);
                String appName = context.getResources().getString(pi.applicationInfo.labelRes);
                String version = pi.versionName;
                singleton = new AndroidUserAgent(appName, version, packageName, uid, platform);
            } catch (Exception e) {
                Timber.e(e, e.getMessage());
            }
        }
        return singleton;
    }

    synchronized private static String getUniqueId(Context ctx) {
        SharedPreferences preferences = ctx.getSharedPreferences(SharedPreferenceConstants.KEYSPACE, Context.MODE_PRIVATE);
        String uniqueId = preferences.getString(SharedPreferenceConstants.USERID, null);

        if (uniqueId == null) {
            Editor editor = preferences.edit();
            uniqueId = UUID.randomUUID().toString();
            editor.putString(SharedPreferenceConstants.USERID, uniqueId);
            editor.apply(); //.commit();
        }
        return uniqueId;
    }

    @Override
    public String toString() {
        return String.format("%s-v%s %s %s AndroidSDKv%s FormFactor-%s",
                cleanString(appLabel),
                cleanString(packageVersion),
                cleanString(packageName),
                cleanString(device),
                cleanString(osVersion),
                cleanString(platform));
    }

    //remove spaces
    private String cleanString(String cleanme) {
        if (cleanme == null) {
            return null;
        } else {
            return cleanme.replace(" ", "");
        }
    }
}
