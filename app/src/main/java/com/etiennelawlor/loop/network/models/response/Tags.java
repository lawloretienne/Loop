package com.etiennelawlor.loop.network.models.response;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Tags implements Parcelable {

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

    // region Constructors
    public Tags() {
    }

    protected Tags(Parcel in) {
        this.uri = in.readString();
        this.name = in.readString();
        this.tag = in.readString();
        this.canonical = in.readString();
    }
    // endregion

    // region Getters

    public String getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public String getCanonical() {
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
        dest.writeString(this.uri);
        dest.writeString(this.name);
        dest.writeString(this.tag);
        dest.writeString(this.canonical);
    }
    // endregion

    public static final Parcelable.Creator<Tags> CREATOR = new Parcelable.Creator<Tags>() {
        @Override
        public Tags createFromParcel(Parcel source) {
            return new Tags(source);
        }

        @Override
        public Tags[] newArray(int size) {
            return new Tags[size];
        }
    };
}
