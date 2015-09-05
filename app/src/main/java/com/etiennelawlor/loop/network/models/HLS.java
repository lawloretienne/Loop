package com.etiennelawlor.loop.network.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class HLS {

    // region Member Variables
    @SerializedName("origin")
    private String origin;
    @SerializedName("cdn")
    private String cdn;
    @SerializedName("all")
    private String all;
    // endregion

    // region Getters
    public String getOrigin() {
        return origin;
    }

    public String getCdn() {
        return cdn;
    }

    public String getAll() {
        return all;
    }
    // endregion

    // region Setters
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setCdn(String cdn) {
        this.cdn = cdn;
    }

    public void setAll(String all) {
        this.all = all;
    }
    // endregion
}

