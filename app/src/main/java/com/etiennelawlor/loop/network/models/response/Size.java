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
    private Integer width;
    @SerializedName("height")
    private Integer height;
    @SerializedName("link")
    private String link;
    // endregion

    // region Getters
    public Integer getWidth() {
        return width == null ? -1 : width;
    }

    public Integer getHeight() {
        return height == null ? -1 : height;
    }

    public String getLink() {
        return TextUtils.isEmpty(link) ? "" : link;
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