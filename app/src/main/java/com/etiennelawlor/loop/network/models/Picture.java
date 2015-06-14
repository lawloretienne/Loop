package com.etiennelawlor.loop.network.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Picture implements Parcelable {

    // region Member Variables
    @SerializedName("type")
    private String type;
    @SerializedName("width")
    private Integer width;
    @SerializedName("uri")
    private Integer height;
    @SerializedName("height")
    private String link;
    // endregion

    // region Getters
    public String getType() {
        if (TextUtils.isEmpty(type))
            return "";
        else
            return type;
    }

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


    public void setType(String type) {
        this.type = type;
    }

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
        dest.writeString(getType());
        dest.writeInt(getWidth());
        dest.writeInt(getHeight());
        dest.writeString(getLink());
    }
    // endregion

    public static final Creator<Picture> CREATOR = new Creator<Picture>() {

        @Override
        public Picture createFromParcel(Parcel source) {
            Picture picture = new Picture();

            picture.setType(source.readString());
            picture.setWidth(source.readInt());
            picture.setHeight(source.readInt());
            picture.setLink(source.readString());

            return picture;
        }

        @Override
        public Picture[] newArray(int size) {
            return new Picture[size];
        }
    };
}
