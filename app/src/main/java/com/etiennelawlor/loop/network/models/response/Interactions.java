package com.etiennelawlor.loop.network.models.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Interactions implements Parcelable {

    // region Member Variables
    @SerializedName("watchlater")
    private Interaction watchlater;
    @SerializedName("like")
    private Interaction like;
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
        dest.writeParcelable(getWatchlater(), flags);
        dest.writeParcelable(getLike(), flags);
    }
    // endregion

    public static final Creator<Interactions> CREATOR = new Creator<Interactions>() {

        @Override
        public Interactions createFromParcel(Parcel source) {
            Interactions interactions = new Interactions();

            interactions.setWatchlater((Interaction) source.readParcelable(Interaction.class.getClassLoader()));
            interactions.setLike((Interaction) source.readParcelable(Interaction.class.getClassLoader()));

            return interactions;
        }

        @Override
        public Interactions[] newArray(int size) {
            return new Interactions[size];
        }
    };
}