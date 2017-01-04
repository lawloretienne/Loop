package com.etiennelawlor.loop.network.models.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Pictures implements Parcelable {

    // region Fields
    @SerializedName("uri")
    private String uri;
    @SerializedName("active")
    private boolean active;
    @SerializedName("sizes")
    private List<Size> sizes;
    // endregion

    // region Constructors
    public Pictures() {
    }

    protected Pictures(Parcel in) {
        this.uri = in.readString();
        this.active = in.readByte() != 0;
        this.sizes = in.createTypedArrayList(Size.CREATOR);
    }
    // endregion

    // region Getters

    public String getUri() {
        return uri;
    }

    public boolean isActive() {
        return active;
    }

    public List<Size> getSizes() {
        return sizes;
    }

    // endregion

    // region Setters

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setSizes(List<Size> sizes) {
        this.sizes = sizes;
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
        dest.writeByte(this.active ? (byte) 1 : (byte) 0);
        dest.writeTypedList(this.sizes);
    }
    // endregion

    public static final Parcelable.Creator<Pictures> CREATOR = new Parcelable.Creator<Pictures>() {
        @Override
        public Pictures createFromParcel(Parcel source) {
            return new Pictures(source);
        }

        @Override
        public Pictures[] newArray(int size) {
            return new Pictures[size];
        }
    };
}
