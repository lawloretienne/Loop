package com.etiennelawlor.loop.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.etiennelawlor.loop.R;

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

    public static void hideKeyboard(Context context, View view) {
        if (context != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                if (view != null) {
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
    }

    public static void showKeyboard(Context context, View view) {
        view.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }
    // endregion
}
