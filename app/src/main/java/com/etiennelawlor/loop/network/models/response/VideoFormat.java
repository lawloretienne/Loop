package com.etiennelawlor.loop.network.models.response;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class VideoFormat {

    // region Fields
    @SerializedName("url")
    private String url;
    @SerializedName("width")
    private int width;
    @SerializedName("height")
    private int height;
    // endregion

    // region Getters

    public String getUrl() {
        return url;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // endregion

    // region Setters

    public void setUrl(String url) {
        this.url = url;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    // endregion
}
