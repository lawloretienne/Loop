package com.etiennelawlor.loop.network.models.response;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Interaction implements Parcelable {

    // region Fields
    @SerializedName("added")
    private Boolean added;
    @SerializedName("added_time")
    private String addedTime;
    @SerializedName("uri")
    private String uri;
    // endregion

    // region Getters
    public Boolean getAdded() {
        return added == null ? false : added;
    }

    public String getAddedTime() {
        return TextUtils.isEmpty(addedTime) ? "" : addedTime;
    }

    public String getUri() {
        return TextUtils.isEmpty(uri) ? "" : uri;
    }
    // endregion

    // region Setters
    public void setAdded(Boolean added) {
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
        dest.writeByte((byte) (getAdded() ? 1 : 0));
        dest.writeString(getAddedTime());
        dest.writeString(getUri());
    }
    // endregion

    public static final Creator<Interaction> CREATOR = new Creator<Interaction>() {

        @Override
        public Interaction createFromParcel(Parcel source) {
            Interaction interaction = new Interaction();

            interaction.setAdded((source.readByte() == 1));
            interaction.setAddedTime(source.readString());
            interaction.setUri(source.readString());

            return interaction;
        }

        @Override
        public Interaction[] newArray(int size) {
            return new Interaction[size];
        }
    };
}