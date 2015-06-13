package com.etiennelawlor.loop.network.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Size implements Parcelable {

    // region Member Variables
    @SerializedName("width")
    private Integer width;
    @SerializedName("height")
    private Integer height;
    @SerializedName("link")
    private String link;
    // endregion

    // region Getters
    public Integer getWidth() {
        if(width == null)
            return -1;
        else
            return width;
    }

    public Integer getHeight() {
        if(height == null)
            return -1;
        else
            return height;
    }

    public String getLink() {
        if (TextUtils.isEmpty(link))
            return "";
        else
            return link;
    }
    // endregion

    // region Setters

    public void setWidth(Integer width) {
        this.width = width;
    }

    public void setHeight(Integer height) {
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
        dest.writeInt(getWidth());
        dest.writeInt(getHeight());
        dest.writeString(getLink());
    }
    // endregion

    public static final Parcelable.Creator<Size> CREATOR = new Parcelable.Creator<Size>() {

        @Override
        public Size createFromParcel(Parcel source) {
            Size size = new Size();

            size.setWidth(source.readInt());
            size.setHeight(source.readInt());
            size.setLink(source.readString());

            return size;
        }

        @Override
        public Size[] newArray(int size) {
            return new Size[size];
        }
    };
}