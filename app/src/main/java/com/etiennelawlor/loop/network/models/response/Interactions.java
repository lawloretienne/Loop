package com.etiennelawlor.loop.network.models.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Interactions implements Parcelable {

    // region Fields
    @SerializedName("watchlater")
    private Interaction watchlater;
    @SerializedName("like")
    private Interaction like;
    // endregion

    // region Constructors
    public Interactions() {
    }

    protected Interactions(Parcel in) {
        this.watchlater = in.readParcelable(Interaction.class.getClassLoader());
        this.like = in.readParcelable(Interaction.class.getClassLoader());
    }
    // endregion

    // region Getters
    public Interaction getWatchlater() {
        return watchlater;
    }

    public Interaction getLike() {
        return like;
    }
    // endregion

    // region Setters
    public void setWatchlater(Interaction watchlater) {
        this.watchlater = watchlater;
    }

    public void setLike(Interaction like) {
        this.like = like;
    }
    // endregion

    // region Parcelable Methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.watchlater, flags);
        dest.writeParcelable(this.like, flags);
    }
    // endregion

    public static final Parcelable.Creator<Interactions> CREATOR = new Parcelable.Creator<Interactions>() {
        @Override
        public Interactions createFromParcel(Parcel source) {
            return new Interactions(source);
        }

        @Override
        public Interactions[] newArray(int size) {
            return new Interactions[size];
        }
    };
}