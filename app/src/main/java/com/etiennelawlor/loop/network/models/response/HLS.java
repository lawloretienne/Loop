package com.etiennelawlor.loop.network.models.response;

import android.text.TextUtils;

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
        return TextUtils.isEmpty(origin) ? "" : origin;
    }

    public String getCdn() {
        return TextUtils.isEmpty(cdn) ? "" : cdn;
    }

    public String getUrl() {
        return TextUtils.isEmpty(url) ? "" : url;
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

