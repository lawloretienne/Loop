package com.etiennelawlor.loop.network.models.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Interaction implements Parcelable {

    // region Fields
    @SerializedName("added")
    private boolean added;
    @SerializedName("added_time")
    private String addedTime;
    @SerializedName("uri")
    private String uri;
    // endregion

    // region Constructors
    public Interaction() {
    }

    protected Interaction(Parcel in) {
        this.added = in.readByte() != 0;
        this.addedTime = in.readString();
        this.uri = in.readString();
    }
    // endregion

    // region Getters

    public boolean isAdded() {
        return added;
    }

    public String getAddedTime() {
        return addedTime;
    }

    public String getUri() {
        return uri;
    }

    // endregion

    // region Setters

    public void setAdded(boolean added) {
        this.added = added;
    }

    public void setAddedTime(String addedTime) {
        this.addedTime = addedTime;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    // endregion

    // region Parcelable Methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.added ? (byte) 1 : (byte) 0);
        dest.writeString(this.addedTime);
        dest.writeString(this.uri);
    }
    // endregion

    public static final Parcelable.Creator<Interaction> CREATOR = new Parcelable.Creator<Interaction>() {
        @Override
        public Interaction createFromParcel(Parcel source) {
            return new Interaction(source);
        }

        @Override
        public Interaction[] newArray(int size) {
            return new Interaction[size];
        }
    };
}