package com.etiennelawlor.loop.network.models.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Stats implements Parcelable {

    // region Fields
    @SerializedName("plays")
    private int plays;
    // endregion

    // region Constructors
    public Stats() {
    }

    protected Stats(Parcel in) {
        this.plays = in.readInt();
    }
    // endregion

    // region Getters

    public int getPlays() {
        return plays;
    }

    // endregion

    // region Setters

    public void setPlays(int plays) {
        this.plays = plays;
    }

    // endregion

    // region Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.plays);
    }
    // endregion

    public static final Parcelable.Creator<Stats> CREATOR = new Parcelable.Creator<Stats>() {
        @Override
        public Stats createFromParcel(Parcel source) {
            return new Stats(source);
        }

        @Override
        public Stats[] newArray(int size) {
            return new Stats[size];
        }
    };
}