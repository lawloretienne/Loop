package com.etiennelawlor.loop.network.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Connections implements Parcelable {

    // region Member Variables
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
        dest.writeParcelable(getComments(), flags);
//        dest.writeParcelable(getCredits(), flags);
//        dest.writeParcelable(getLikes(), flags);
        dest.writeParcelable(getPictures(), flags);
        dest.writeParcelable(getTexttracks(), flags);
    }
    // endregion

    public static final Parcelable.Creator<Connections> CREATOR = new Parcelable.Creator<Connections>() {

        @Override
        public Connections createFromParcel(Parcel source) {
            Connections connections = new Connections();

            connections.setComments((Connection) source.readParcelable(Connection.class.getClassLoader()));
//            connections.setCredits((Connection) source.readParcelable(Connection.class.getClassLoader()));
//            connections.setLikes((Connection) source.readParcelable(Connection.class.getClassLoader()));
            connections.setPictures((Connection) source.readParcelable(Connection.class.getClassLoader()));
            connections.setTexttracks((Connection) source.readParcelable(Connection.class.getClassLoader()));

            return connections;
        }

        @Override
        public Connections[] newArray(int size) {
            return new Connections[size];
        }
    };
}