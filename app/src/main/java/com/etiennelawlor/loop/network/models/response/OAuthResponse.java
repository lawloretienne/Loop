package com.etiennelawlor.loop.network.models.response;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */

public final class OAuthResponse {

    // region Fields
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("token_type")
    private String tokenType;
    @SerializedName("scope")
    private String scope;
    @SerializedName("user")
    private AuthorizedUser user;
    // endregion

    // region Getters
    public String getAccessToken() {
        return TextUtils.isEmpty(accessToken) ? "" : accessToken;
    }

    public String getTokenType() {
        return TextUtils.isEmpty(tokenType) ? "" : tokenType;
    }

    public String getScope() {
        return TextUtils.isEmpty(scope) ? "" : scope;
    }

    public AuthorizedUser getUser() {
        return user;
    }

    // endregion

    // region Setters
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setUser(AuthorizedUser user) {
        this.user = user;
    }
    // endregion

}

