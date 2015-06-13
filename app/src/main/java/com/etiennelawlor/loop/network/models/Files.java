package com.etiennelawlor.loop.network.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Files {

    // region Member Variables
    @SerializedName("h264")
    private H264 h264;
    // endregion

    // region Getters
    public H264 getH264() {
        return h264;
    }
    // endregion

    // region Setters
    public void setH264(H264 h264) {
        this.h264 = h264;
    }
    // endregion
}
