package com.etiennelawlor.loop.network;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public enum EndpointUrl {

    VIMEO_API ("https://api.vimeo.com"),
    VIMEO_PLAYER ("http://player.vimeo.com");


    // region Member Variables
    private final String mValue;
    // endregion

    EndpointUrl(String value) {
        this.mValue = value;
    }

    @Override
    public String toString() {
        return mValue;
    }
}