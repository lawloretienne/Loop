package com.etiennelawlor.loop.network.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Tag implements Parcelable {

    // region Member Variables
    @SerializedName("uri")
    private String uri;
    @SerializedName("name")
    private String name;
    @SerializedName("tag")
    private String tag;
    @SerializedName("canonical")
    private String canonical;
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

    public String getTag() {
        if (TextUtils.isEmpty(tag))
            return "";
        else
            return tag;
    }

    public String getCanonical() {
        if (TextUtils.isEmpty(canonical))
            return "";
        else
            return canonical;
    }

    // endregion

    // region Setters
    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setCanonical(String canonical) {
        this.canonical = canonical;
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
        dest.writeString(getTag());
        dest.writeString(getCanonical());
    }
    // endregion

    public static final Creator<Tag> CREATOR = new Creator<Tag>() {

        @Override
        public Tag createFromParcel(Parcel source) {
            Tag tags = new Tag();

            tags.setUri(source.readString());
            tags.setName(source.readString());
            tags.setTag(source.readString());
            tags.setCanonical(source.readString());

            return tags;
        }

        @Override
        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };
}
