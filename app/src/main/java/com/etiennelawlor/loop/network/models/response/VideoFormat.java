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
    private Integer width;
    @SerializedName("height")
    private Integer height;
    // endregion

    // region Getters
    public String getUrl() {
        return TextUtils.isEmpty(url) ? "" : url;
    }

    public Integer getWidth() {
        return width == null ? -1 : width;
    }

    public Integer getHeight() {
        return height == null ? -1 : height;
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
