package com.etiennelawlor.loop.network.models.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class VP6 {

    // region Member Variables
    @SerializedName("sd")
    private VideoFormat sd;
    @SerializedName("hd")
    private VideoFormat hd;
    @SerializedName("mobile")
    private VideoFormat mobile;
    // endregion

    // region Getters
    public VideoFormat getSd() {
        return sd;
    }

    public VideoFormat getHd() {
        return hd;
    }

    public VideoFormat getMobile() {
        return mobile;
    }
    // endregion

    // region Setters
    public void setSd(VideoFormat sd) {
        this.sd = sd;
    }

    public void setHd(VideoFormat hd) {
        this.hd = hd;
    }

    public void setMobile(VideoFormat mobile) {
        this.mobile = mobile;
    }
    // endregion
}

