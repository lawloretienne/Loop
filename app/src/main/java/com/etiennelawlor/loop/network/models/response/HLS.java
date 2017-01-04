package com.etiennelawlor.loop.network.models.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class HLS {

    // region Fields
    @SerializedName("origin")
    private String origin;
    @SerializedName("cdn")
    private String cdn;
    @SerializedName("url")
    private String url;
    // endregion

    // region Getters

    public String getOrigin() {
        return origin;
    }

    public String getCdn() {
        return cdn;
    }

    public String getUrl() {
        return url;
    }

    // endregion

    // region Setters
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setCdn(String cdn) {
        this.cdn = cdn;
    }

    public void setAll(String url) {
        this.url = url;
    }
    // endregion
}

