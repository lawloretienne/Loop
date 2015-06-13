package com.etiennelawlor.loop.network.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class H264 {

    // region Member Variables
    @SerializedName("sd")
    private VideoFormat sd;
    @SerializedName("hd")
    private VideoFormat hd;
    @SerializedName("moble")
    private VideoFormat moble;
    // endregion

    // region Getters
    public VideoFormat getSd() {
        return sd;
    }

    public VideoFormat getHd() {
        return hd;
    }

    public VideoFormat getMoble() {
        return moble;
    }
    // endregion

    // region Setters
    public void setSd(VideoFormat sd) {
        this.sd = sd;
    }

    public void setHd(VideoFormat hd) {
        this.hd = hd;
    }

    public void setMoble(VideoFormat moble) {
        this.moble = moble;
    }
    // endregion
}

