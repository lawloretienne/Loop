package com.etiennelawlor.loop.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.etiennelawlor.loop.R;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class LoopUtility {

    // region Utility Methods
    public static Intent getEmailIntent(Context context) {
//        final Intent intent = new Intent(Intent.ACTION_SEND);
        final Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        AndroidUserAgent agent = AndroidUserAgent.getUserAgent(context);

//        intent.setType("text/plain");

        String bodyText = getEmailEnding(agent, context);

        String emailAddy = context.getResources().getString(R.string.support_email);

        String subject = context.getResources().getString(R.string.email_subject);

        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddy});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, bodyText);

        return intent;
    }

    private static String getEmailEnding(AndroidUserAgent agent, Context context) {
        String version = "";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName + " (" + pInfo.versionCode + ")";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "App Name: "
                + agent.appLabel + "\n"
                + "App Version: "
                + version + "\n"
                + "App ID: "
                + agent.packageName + "\n"
                + "Device: "
                + agent.device + "\n"
                + "OS Version: "
                + agent.osVersion + "\n"
                + "GUID: " + agent.uniqueId + "\n"
                + context.getResources().getString(R.string.email_message) + "\n";
    }
    // endregion
}
