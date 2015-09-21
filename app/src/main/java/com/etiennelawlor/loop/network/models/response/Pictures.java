package com.etiennelawlor.loop.network.models.response;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Pictures implements Parcelable {

    // region Member Variables
    @SerializedName("uri")
    private String uri;
    @SerializedName("active")
    private Boolean active;
    @SerializedName("sizes")
    private List<Size> sizes;
    // endregion

    // region Getters
    public String getUri() {
        if (TextUtils.isEmpty(uri))
            return "";
        else
            return uri;
    }

    public Boolean getActive() {
        if(active == null)
            return false;
        else
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

    public void setActive(Boolean active) {
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
        dest.writeString(getUri());
        dest.writeByte((byte) (getActive() ? 1 : 0));
        dest.writeTypedList(getSizes());
    }
    // endregion

    public static final Creator<Pictures> CREATOR = new Creator<Pictures>() {

        @Override
        public Pictures createFromParcel(Parcel source) {
            Pictures pictures = new Pictures();

            pictures.setUri(source.readString());
            pictures.setActive((source.readByte() == 1));
            pictures.setSizes(source.createTypedArrayList(Size.CREATOR));

            return pictures;
        }

        @Override
        public Pictures[] newArray(int size) {
            return new Pictures[size];
        }
    };
}
