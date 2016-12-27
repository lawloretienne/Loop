package com.etiennelawlor.loop.network.models.response;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Picture implements Parcelable {

    // region Fields
    @SerializedName("type")
    private String type;
    @SerializedName("width")
    private int width;
    @SerializedName("height")
    private int height;
    @SerializedName("link")
    private String link;
    // endregion

    // region Constructors
    public Picture() {
    }

    protected Picture(Parcel in) {
        this.type = in.readString();
        this.width = in.readInt();
        this.height = in.readInt();
        this.link = in.readString();
    }
    // endregion

    // region Getters

    public String getType() {
        return type;
    }

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

    public void setType(String type) {
        this.type = type;
    }

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
        dest.writeString(this.type);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeString(this.link);
    }
    // endregion

    public static final Parcelable.Creator<Picture> CREATOR = new Parcelable.Creator<Picture>() {
        @Override
        public Picture createFromParcel(Parcel source) {
            return new Picture(source);
        }

        @Override
        public Picture[] newArray(int size) {
            return new Picture[size];
        }
    };
}
