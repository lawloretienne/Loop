package com.etiennelawlor.loop.network.models.response;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Size implements Parcelable {

    // region Fields
    @SerializedName("width")
    private int width;
    @SerializedName("height")
    private int height;
    @SerializedName("link")
    private String link;
    // endregion

    // region Constructors
    public Size() {
    }

    protected Size(Parcel in) {
        this.width = in.readInt();
        this.height = in.readInt();
        this.link = in.readString();
    }
    // endregion

    // region Getters

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getLink() {
        return link;
    }

    // endregion

    // region Setters

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setLink(String link) {
        this.link = link;
    }

    // endregion

    // region Parcelable Methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeString(this.link);
    }
    // endregion

    public static final Parcelable.Creator<Size> CREATOR = new Parcelable.Creator<Size>() {
        @Override
        public Size createFromParcel(Parcel source) {
            return new Size(source);
        }

        @Override
        public Size[] newArray(int size) {
            return new Size[size];
        }
    };
}