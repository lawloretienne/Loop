package com.etiennelawlor.loop.utilities;

import android.text.TextUtils;

import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 11/8/15.
 */
public class LogUtility {

    public static void logFailure(Throwable throwable){
        Throwable cause = throwable.getCause();
        String message = throwable.getMessage();

        if (cause != null) {
            Timber.e("failure() : cause.toString() -" + cause.toString());
        }

        if (!TextUtils.isEmpty(message)) {
            Timber.e("failure() : message - " + message);
        }

        throwable.printStackTrace();
    }

    public static void logFailedResponse(Response rawResponse){
        String message = rawResponse.message();
        int code = rawResponse.code();
        Timber.d("onResponse() : message - " + message);
        Timber.d("onResponse() : code - " + code);
    }
}
