package com.etiennelawlor.loop.network.models.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class ProgressiveData {


//    {
//        profile: 119,
//                width: 1920,
//            mime: "video/mp4",
//            fps: 25,
//            url: "https://14-lvl3-pdl.vimeocdn.com/01/1782/0/8912794/145347913.mp4?expires=1447668771&token=04cdd161893e233cd483e",
//            cdn: "level3",
//            quality: "1080p",
//            id: 145347913,
//            origin: "level3",
//            height: 1080
//    },

    // region Fields
    @SerializedName("profile")
    private int profile;
    @SerializedName("width")
    private int width;
    @SerializedName("mime")
    private String mime;
    @SerializedName("fps")
    private int fps;
    @SerializedName("url")
    private String url;
    @SerializedName("cdn")
    private String cdn;
    @SerializedName("quality")
    private String quality;
    @SerializedName("id")
    private long id;
    @SerializedName("origin")
    private String origin;
    @SerializedName("height")
    private int height;
    // endregion

    // region Getters

    public int getProfile() {
        return profile;
    }

    public int getWidth() {
        return width;
    }

    public String getMime() {
        return mime;
    }

    public int getFps() {
        return fps;
    }

    public String getUrl() {
        return url;
    }

    public String getCdn() {
        return cdn;
    }

    public String getQuality() {
        return quality;
    }

    public long getId() {
        return id;
    }

    public String getOrigin() {
        return origin;
    }

    public int getHeight() {
        return height;
    }

    // endregion

    // region Setters

    public void setProfile(int profile) {
        this.profile = profile;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCdn(String cdn) {
        this.cdn = cdn;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    // endregion
}
