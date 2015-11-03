package com.etiennelawlor.loop.models;

/**
 * Created by etiennelawlor on 6/14/15.
 */
public class AccessToken {

    // region Fields
    private String tokenType;
    private String accessToken;
    // endregion

    // region Constructors
    public AccessToken(String tokenType, String accessToken) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
    }
    // endregion

    // region Getters
    public String getTokenType() {
        // OAuth requires uppercase Authorization HTTP header value for token type
        if ( ! Character.isUpperCase(tokenType.charAt(0))) {
            tokenType = Character.toString(tokenType.charAt(0)).toUpperCase() + tokenType.substring(1);
        }

        return tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }
    // endregion
}



