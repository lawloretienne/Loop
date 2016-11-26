package com.etiennelawlor.loop.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import com.etiennelawlor.loop.R;

/**
 * Created by etiennelawlor on 6/16/16.
 */

public class EmailUtility {

    public static Intent getEmailIntent(Context context) {
        final Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));

        String bodyText = getEmailBody(context);

        String emailAddy = context.getResources().getString(R.string.support_email);

        String subject = context.getResources().getString(R.string.email_subject);

        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddy});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, bodyText);

        return intent;
    }

    private static String getEmailBody(Context context) {
        String version = "";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName + " (" + pInfo.versionCode + ")";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String body = String.format("App Version : %s\n" +
                        "API : %s\n" +
                        "Model : %s\n" +
                        "Device : %s\n" +
                        "Manufacturer : %s\n" +
                        "-----------------------------------------------------\n\n" +
                        "%s",
                version,
                Build.VERSION.SDK_INT,
                Build.MODEL,
                Build.DEVICE,
                Build.MANUFACTURER,
                context.getResources().getString(R.string.email_message));

        return body;
    }
}
