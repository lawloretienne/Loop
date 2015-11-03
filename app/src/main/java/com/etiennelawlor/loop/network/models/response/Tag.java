package com.etiennelawlor.loop.network.models.response;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Tag implements Parcelable {

    // region Fields
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
        return TextUtils.isEmpty(uri) ? "" : uri;
    }

    public String getName() {
        return TextUtils.isEmpty(name) ? "" : name;
    }

    public String getTag() {
        return TextUtils.isEmpty(tag) ? "" : tag;
    }

    public String getCanonical() {
        return TextUtils.isEmpty(canonical) ? "" : canonical;
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
