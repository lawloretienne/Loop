package com.etiennelawlor.loop.network.models.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class VideoConfig {

    // region Fields
    @SerializedName("request")
    private Request request;
    // endregion

    // region Getters
    public Request getRequest() {
        return request;
    }
    // endregion

    // region Setters
    public void setRequest(Request request) {
        this.request = request;
    }
    // endregion
}
