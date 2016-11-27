package com.etiennelawlor.loop.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.etiennelawlor.loop.models.AccessToken;
import com.etiennelawlor.loop.network.models.response.AuthorizedUser;
import com.google.gson.Gson;

/**
 * Created by etiennelawlor on 11/26/16.
 */

public class LoopPrefs {

    // region Constants
    private static final String LOOP_PREF = "LOOP_PREF";
    private static final String KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN";
    private static final String KEY_AUTHORIZED_USER = "KEY_AUTHORIZED_USER";
    // endregion

    // region Constructors
    private LoopPrefs() {
        //no instance
    }

    // region Getters
    public static AccessToken getAccessToken(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        Gson gson = new Gson();
        String json = preferences.getString(KEY_ACCESS_TOKEN, "");
        AccessToken accessToken = gson.fromJson(json, AccessToken.class);
        return accessToken;
    }

    public static AuthorizedUser getAuthorizedUser(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        Gson gson = new Gson();
        String json = preferences.getString(KEY_AUTHORIZED_USER, "");
        AuthorizedUser authorizedUser = gson.fromJson(json, AuthorizedUser.class);
        return authorizedUser;
    }
    // endregion

    // region Setters
    public static void saveAccessToken(Context context, AccessToken accessToken) {
        SharedPreferences.Editor editor = getEditor(context);
        Gson gson = new Gson();
        String json = gson.toJson(accessToken);
        editor.putString(KEY_ACCESS_TOKEN, json)
            .apply();
    }

    public static void saveAuthorizedUser(Context context, AuthorizedUser authorizedUser) {
        SharedPreferences.Editor editor = getEditor(context);
        Gson gson = new Gson();
        String json = gson.toJson(authorizedUser);
        editor.putString(KEY_AUTHORIZED_USER, json)
            .apply();
    }
    // endregion

    public static void signOut(Context context) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.remove(KEY_ACCESS_TOKEN)
            .remove(KEY_AUTHORIZED_USER)
            .apply();
    }

    // region Helper Methods
    private static SharedPreferences.Editor getEditor(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.edit();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(LOOP_PREF, Context.MODE_PRIVATE);
    }
    // endregion
}
