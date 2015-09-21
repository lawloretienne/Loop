package com.etiennelawlor.loop.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.etiennelawlor.loop.models.AccessToken;
import com.etiennelawlor.loop.network.models.response.AuthorizedUser;
import com.google.gson.Gson;

/**
 * Created by etiennelawlor on 6/20/15.
 */
public class PreferencesHelper {

    // region Constants
    private static final String USER_PREFERENCES = "userPreferences";
    private static final String PREFERENCE_ACCESS_TOKEN = USER_PREFERENCES + ".accessToken";
    private static final String PREFERENCE_AUTHORIZED_USER = USER_PREFERENCES + ".authorizedUser";
    // endregion

    // region Constructors
    private PreferencesHelper() {
        //no instance
    }
    // endregion

    public static AccessToken getAccessToken(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        Gson gson = new Gson();
        String json = preferences.getString(PREFERENCE_ACCESS_TOKEN, "");
        AccessToken accessToken = gson.fromJson(json, AccessToken.class);
        return accessToken;
    }

    public static AuthorizedUser getAuthorizedUser(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        Gson gson = new Gson();
        String json = preferences.getString(PREFERENCE_AUTHORIZED_USER, "");
        AuthorizedUser authorizedUser = gson.fromJson(json, AuthorizedUser.class);
        return authorizedUser;
    }

    public static void saveAccessToken(Context context, AccessToken accessToken) {
        SharedPreferences.Editor editor = getEditor(context);
        Gson gson = new Gson();
        String json = gson.toJson(accessToken);
        editor.putString(PREFERENCE_ACCESS_TOKEN, json);
        editor.apply();
    }

    public static void saveAuthorizedUser(Context context, AuthorizedUser authorizedUser) {
        SharedPreferences.Editor editor = getEditor(context);
        Gson gson = new Gson();
        String json = gson.toJson(authorizedUser);
        editor.putString(PREFERENCE_AUTHORIZED_USER, json);
        editor.apply();
    }

    public static void signOut(Context context) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.remove(PREFERENCE_ACCESS_TOKEN);
        editor.remove(PREFERENCE_AUTHORIZED_USER);
        editor.apply();
    }

    // region Helper Methods
    private static SharedPreferences.Editor getEditor(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.edit();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(USER_PREFERENCES, Context.MODE_PRIVATE);
    }
    // endregion
}
