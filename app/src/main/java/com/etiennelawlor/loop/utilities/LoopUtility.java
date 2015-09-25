package com.etiennelawlor.loop.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.etiennelawlor.loop.R;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class LoopUtility {

    // region Utility Methods
    public static int dp2px(Context context, int dp) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        DisplayMetrics displaymetrics = new DisplayMetrics();
        display.getMetrics(displaymetrics);

        return (int) (dp * displaymetrics.density + 0.5f);
    }

    public static int px2dp(Context context, int px) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        DisplayMetrics displaymetrics = new DisplayMetrics();
        display.getMetrics(displaymetrics);

        return (int) (px / displaymetrics.density + 0.5f);
    }

    public static String getRelativeDate(Calendar future) {

        String relativeDate = "";

        long days = getDateDiff(future.getTime(), Calendar.getInstance().getTime(), TimeUnit.DAYS);

        if (days < 7) {
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(future.getTimeInMillis(), System.currentTimeMillis(),
                    DateUtils.SECOND_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_ALL);

//      Timber.d("relativeTime - " + relativeTime);

            if (relativeTime.toString().equals("0 minutes ago")
                    || relativeTime.toString().equals("in 0 minutes")) {
                relativeDate = "Just now";
            } else if(relativeTime.toString().contains("hr. ")){
                if(relativeTime.toString().equals("1 hr. ago")){
                    relativeDate = "1 hour ago";
                } else {
                    relativeDate = relativeTime.toString().replace("hr. ", "hours ");
                }
            } else {
                relativeDate = relativeTime.toString();
            }
        } else if (days >= 7 && days < 14) {
            relativeDate = "A week ago";
        } else if (days >= 14 && days < 21) {
            relativeDate = "2 weeks ago";
        } else if (days >= 21 && days < 28) {
            relativeDate = "3 weeks ago";
        } else if ((days / 30) == 1) {
            relativeDate = "1 month ago";
        } else if ((days / 30) >= 2 && (days / 30) < 12) {
            relativeDate = String.format("%d months ago", (days / 30));
        } else if ((days / 365) > 1) {
            relativeDate = String.format("%d years ago", (days / 365));
        }

//        Timber.d("getRelativeDate() : days - " + days);
//        Timber.d("getRelativeDate() : relativeDate - " + relativeDate);

        return relativeDate;
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

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

    public static int getScreenWidth(Context context){
        Point size = new Point();
        ((Activity)context).getWindowManager().getDefaultDisplay().getSize(size);
        return size.x;
    }

    public static boolean isInLandscapeMode(Context context){
        boolean isLandscape = false;
        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            isLandscape =  true;
        }
        return isLandscape;
    }
    // endregion
}
