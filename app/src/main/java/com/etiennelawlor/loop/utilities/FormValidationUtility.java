package com.etiennelawlor.loop.utilities;

import android.text.TextUtils;

/**
 * Created by etiennelawlor on 6/7/16.
 */

public class FormValidationUtility {

    public static boolean validateComment(String comment) {
        return !TextUtils.isEmpty(comment);
    }
}
