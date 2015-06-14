package com.etiennelawlor.loop.network.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class AuthorizedUser implements Parcelable {

    // region Member Variables
    @SerializedName("uri")
    private String uri;
    @SerializedName("name")
    private String name;
    @SerializedName("link")
    private String link;
    @SerializedName("location")
    private String location;
    @SerializedName("bio")
    private String bio;
    @SerializedName("created_time")
    private String createdTime;
    @SerializedName("account")
    private String account;
    @SerializedName("pictures")
    private List<Picture> pictures;
    // endregion

    // region Getters
    public String getUri() {
        if (TextUtils.isEmpty(uri))
            return "";
        else
            return uri;
    }

    public String getName() {
        if (TextUtils.isEmpty(name))
            return "";
        else
            return name;
    }

    public String getLink() {
        if (TextUtils.isEmpty(link))
            return "";
        else
            return link;
    }

    public String getLocation() {
        if (TextUtils.isEmpty(location))
            return "";
        else
            return location;
    }

    public String getBio() {
        if (TextUtils.isEmpty(bio))
            return "";
        else
            return bio;
    }

    public String getCreatedTime() {
        if (TextUtils.isEmpty(createdTime))
            return "";
        else
            return createdTime;
    }

    public String getAccount() {
        if (TextUtils.isEmpty(account))
            return "";
        else
            return account;
    }

    public List<Picture> getPictures() {
        return pictures;
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

    public void setLocation(String location) {
        this.location = location;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setPictures(List<Picture> pictures) {
        this.pictures = pictures;
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
        dest.writeString(getLocation());
        dest.writeString(getBio());
        dest.writeString(getCreatedTime());
        dest.writeString(getAccount());
        dest.writeTypedList(getPictures());
    }
    // endregion

    public static final Creator<AuthorizedUser> CREATOR = new Creator<AuthorizedUser>() {

        @Override
        public AuthorizedUser createFromParcel(Parcel source) {
            AuthorizedUser user = new AuthorizedUser();

            user.setUri(source.readString());
            user.setName(source.readString());
            user.setLink(source.readString());
            user.setLocation(source.readString());
            user.setBio(source.readString());
            user.setCreatedTime(source.readString());
            user.setAccount(source.readString());
            user.setPictures(source.createTypedArrayList(Picture.CREATOR));

            return user;
        }

        @Override
        public AuthorizedUser[] newArray(int size) {
            return new AuthorizedUser[size];
        }
    };
}