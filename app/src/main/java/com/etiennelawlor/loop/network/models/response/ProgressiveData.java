package com.etiennelawlor.loop.network.models.response;

import android.text.TextUtils;

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
    private Integer profile;
    @SerializedName("width")
    private Integer width;
    @SerializedName("mime")
    private String mime;
    @SerializedName("fps")
    private Integer fps;
    @SerializedName("url")
    private String url;
    @SerializedName("cdn")
    private String cdn;
    @SerializedName("quality")
    private String quality;
    @SerializedName("id")
    private Long id;
    @SerializedName("origin")
    private String origin;
    @SerializedName("height")
    private Integer height;
    // endregion

    // region Getters

    public Integer getProfile() {
        return profile == null ? -1 : profile;
    }

    public Integer getWidth() {
        return width == null ? -1 : width;
    }

    public String getMime() {
        return TextUtils.isEmpty(mime) ? "" : mime;
    }

    public Integer getFps() {
        return fps == null ? -1 : fps;
    }

    public String getUrl() {
        return TextUtils.isEmpty(url) ? "" : url;
    }

    public String getCdn() {
        return TextUtils.isEmpty(cdn) ? "" : cdn;
    }

    public String getQuality() {
        return TextUtils.isEmpty(quality) ? "" : quality;
    }

    public Long getId() {
        return id == null ? -1L : id;
    }

    public String getOrigin() {
        return TextUtils.isEmpty(origin) ? "" : origin;
    }

    public Integer getHeight() {
        return height == null ? -1 : height;
    }

    // endregion

    // region Setters
    public void setProfile(Integer profile) {
        this.profile = profile;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public void setFps(Integer fps) {
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

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }
    // endregion
}
