package com.etiennelawlor.loop.network.models.response;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class VideoFormat {

    // region Member Variables
    @SerializedName("url")
    private String url;
    @SerializedName("width")
    private Integer width;
    @SerializedName("height")
    private Integer height;
    // endregion

    // region Getters
    public String getUrl() {
        if (TextUtils.isEmpty(url))
            return "";
        else
            return url;
    }

    public Integer getWidth() {
        if(width == null)
            return -1;
        else
            return width;
    }

    public Integer getHeight() {
        if(height == null)
            return -1;
        else
            return height;
    }
    // endregion

    // region Setters
    public void setUrl(String url) {
        this.url = url;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }
    // endregion
}
