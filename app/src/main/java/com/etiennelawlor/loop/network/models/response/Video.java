package com.etiennelawlor.loop.network.models.response;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Video implements Parcelable {

    // region Fields
    @SerializedName("uri")
    private String uri;
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("link")
    private String link;
    @SerializedName("duration")
    private int duration;
    @SerializedName("width")
    private int width;
    @SerializedName("language")
    private String language;
    @SerializedName("height")
    private int height;
    @SerializedName("embed")
    private Embed embed;
    @SerializedName("created_time")
    private String createdTime;
    @SerializedName("modified_time")
    private String modifiedTime;
    @SerializedName("content_rating")
    private List<String> contentRating;
//    @SerializedName("license")
//    private Object license;
    @SerializedName("pictures")
    private Pictures pictures;
    @SerializedName("tags")
    private List<Tag> tags;
    @SerializedName("stats")
    private Stats stats;
    @SerializedName("metadata")
    private Metadata metadata;
    @SerializedName("user")
    private User user;
//    @SerializedName("app")
//    private Object app;
    @SerializedName("status")
    private String status;
//    @SerializedName("embed_presets")
//    private Object embedPresets;
    // endregion

    // region Constructors
    public Video() {
    }

    protected Video(Parcel in) {
        this.uri = in.readString();
        this.name = in.readString();
        this.description = in.readString();
        this.link = in.readString();
        this.duration = in.readInt();
        this.width = in.readInt();
        this.language = in.readString();
        this.height = in.readInt();
        this.embed = in.readParcelable(Embed.class.getClassLoader());
        this.createdTime = in.readString();
        this.modifiedTime = in.readString();
        this.contentRating = in.createStringArrayList();
        this.pictures = in.readParcelable(Pictures.class.getClassLoader());
        this.tags = in.createTypedArrayList(Tag.CREATOR);
        this.stats = in.readParcelable(Stats.class.getClassLoader());
        this.metadata = in.readParcelable(Metadata.class.getClassLoader());
        this.user = in.readParcelable(User.class.getClassLoader());
        this.status = in.readString();
    }
    // endregion

    // region Getters

    public String getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public int getDuration() {
        return duration;
    }

    public int getWidth() {
        return width;
    }

    public String getLanguage() {
        return language;
    }

    public int getHeight() {
        return height;
    }

    public Embed getEmbed() {
        return embed;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public String getModifiedTime() {
        return modifiedTime;
    }

    public List<String> getContentRating() {
        return contentRating;
    }

    public Pictures getPictures() {
        return pictures;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public Stats getStats() {
        return stats;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public User getUser() {
        return user;
    }

    public String getStatus() {
        return status;
    }

    public long getId() {
        long id = -1L;
        String uri = getUri();
        if (!TextUtils.isEmpty(uri)) {
            String lastPathSegment = Uri.parse(uri).getLastPathSegment();
            id = Long.parseLong(lastPathSegment);
        }
        return id;
    }

    // endregion

    // region Setters

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setEmbed(Embed embed) {
        this.embed = embed;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public void setModifiedTime(String modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public void setContentRating(List<String> contentRating) {
        this.contentRating = contentRating;
    }

    public void setPictures(Pictures pictures) {
        this.pictures = pictures;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // endregion

    // region Parcelable Methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uri);
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeString(this.link);
        dest.writeInt(this.duration);
        dest.writeInt(this.width);
        dest.writeString(this.language);
        dest.writeInt(this.height);
        dest.writeParcelable(this.embed, flags);
        dest.writeString(this.createdTime);
        dest.writeString(this.modifiedTime);
        dest.writeStringList(this.contentRating);
        dest.writeParcelable(this.pictures, flags);
        dest.writeTypedList(this.tags);
        dest.writeParcelable(this.stats, flags);
        dest.writeParcelable(this.metadata, flags);
        dest.writeParcelable(this.user, flags);
        dest.writeString(this.status);
    }
    // endregion

    public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel source) {
            return new Video(source);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };
}