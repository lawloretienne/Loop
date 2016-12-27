package com.etiennelawlor.loop.network.models.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Connections implements Parcelable {

    // region Fields
    @SerializedName("comments")
    private Connection comments;
//    @SerializedName("credits")
//    private Connection credits;
//    @SerializedName("likes")
//    private Connection likes;
    @SerializedName("pictures")
    private Connection pictures;
    @SerializedName("texttracks")
    private Connection texttracks;
    // endregion

    // region Constructors
    public Connections() {
    }

    protected Connections(Parcel in) {
        this.comments = in.readParcelable(Connection.class.getClassLoader());
        this.pictures = in.readParcelable(Connection.class.getClassLoader());
        this.texttracks = in.readParcelable(Connection.class.getClassLoader());
    }
    // endregion

    // region Getters

    public Connection getComments() {
        return comments;
    }

//    public Connection getCredits() {
//        return credits;
//    }

//    public Connection getLikes() {
//        return likes;
//    }

    public Connection getPictures() {
        return pictures;
    }

    public Connection getTexttracks() {
        return texttracks;
    }

    // endregion

    // region Setters
    public void setComments(Connection comments) {
        this.comments = comments;
    }

//    public void setCredits(Connection credits) {
//        this.credits = credits;
//    }

//    public void setLikes(Connection likes) {
//        this.likes = likes;
//    }

    public void setPictures(Connection pictures) {
        this.pictures = pictures;
    }

    public void setTexttracks(Connection texttracks) {
        this.texttracks = texttracks;
    }
    // endregion

    // region Parcelable Methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.comments, flags);
        dest.writeParcelable(this.pictures, flags);
        dest.writeParcelable(this.texttracks, flags);
    }
    // endregion

    public static final Parcelable.Creator<Connections> CREATOR = new Parcelable.Creator<Connections>() {
        @Override
        public Connections createFromParcel(Parcel source) {
            return new Connections(source);
        }

        @Override
        public Connections[] newArray(int size) {
            return new Connections[size];
        }
    };
}