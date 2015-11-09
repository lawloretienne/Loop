package com.etiennelawlor.loop.network.models.response;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Category implements Parcelable {

    // region Fields
    @SerializedName("uri")
    private String uri;
    @SerializedName("name")
    private String name;
    @SerializedName("link")
    private String link;
    @SerializedName("top_level")
    private Boolean topLevel;
    @SerializedName("pictures")
    private Pictures pictures;
    @SerializedName("metadata")
    private Metadata metadata;
    // endregion

    // region Getters
    public String getUri() {
        return TextUtils.isEmpty(uri) ? "" : uri;
    }

    public String getName() {
        return TextUtils.isEmpty(name) ? "" : name;
    }

    public String getLink() {
        return TextUtils.isEmpty(link) ? "" : link;
    }

    public Boolean getTopLevel() {
        return topLevel == null ? false : topLevel;
    }

    public Pictures getPictures() {
        return pictures;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    // endregion

    // region Setters

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setTopLevel(Boolean topLevel) {
        this.topLevel = topLevel;
    }

    public void setPictures(Pictures pictures) {
        this.pictures = pictures;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    // endregion

    // region Parcelable Methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getUri());
        dest.writeString(getName());
        dest.writeString(getLink());
        dest.writeByte((byte) (getTopLevel() ? 1 : 0));
        dest.writeParcelable(getPictures(), flags);
        dest.writeParcelable(getMetadata(), flags);
    }
    // endregion

    public static final Creator<Category> CREATOR = new Creator<Category>() {

        @Override
        public Category createFromParcel(Parcel source) {
            Category video = new Category();

            video.setUri(source.readString());
            video.setName(source.readString());
            video.setLink(source.readString());
            video.setTopLevel((source.readByte() == 1));
            video.setPictures((Pictures) source.readParcelable(Pictures.class.getClassLoader()));
            video.setMetadata((Metadata) source.readParcelable(Metadata.class.getClassLoader()));

            return video;
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
}