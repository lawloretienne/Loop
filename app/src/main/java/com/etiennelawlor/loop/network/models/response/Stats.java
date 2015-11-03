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
    private Integer plays;
    // endregion

    // region Getters
    public Integer getPlays() {
        return plays == null ? -1 : plays;
    }
    // endregion

    // region Setters
    public void setPlays(Integer plays) {
        this.plays = plays;
    }
    // endregion

    // region Parcelable Methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getPlays());
    }
    // endregion

    public static final Creator<Stats> CREATOR = new Creator<Stats>() {

        @Override
        public Stats createFromParcel(Parcel source) {
            Stats stats = new Stats();

            stats.setPlays(source.readInt());

            return stats;
        }

        @Override
        public Stats[] newArray(int size) {
            return new Stats[size];
        }
    };
}