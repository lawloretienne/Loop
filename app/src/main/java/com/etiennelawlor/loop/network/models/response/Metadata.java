package com.etiennelawlor.loop.network.models.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Metadata implements Parcelable {

    // region Fields
    @SerializedName("connections")
    private Connections connections;
    @SerializedName("interactions")
    private Interactions interactions;
    // endregion

    // region Getters
    public Connections getConnections() {
        return connections;
    }

    public Interactions getInteractions() {
        return interactions;
    }
    // endregion

    // region Setters
    public void setConnections(Connections connections) {
        this.connections = connections;
    }

    public void setInteractions(Interactions interactions) {
        this.interactions = interactions;
    }
    // endregion

    // region Parcelable Methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(getConnections(), flags);
        dest.writeParcelable(getInteractions(), flags);
    }
    // endregion

    public static final Parcelable.Creator<Metadata> CREATOR = new Parcelable.Creator<Metadata>() {

        @Override
        public Metadata createFromParcel(Parcel source) {
            Metadata metadata = new Metadata();

            metadata.setConnections((Connections) source.readParcelable(Connections.class.getClassLoader()));
            metadata.setInteractions((Interactions) source.readParcelable(Interactions.class.getClassLoader()));

            return metadata;
        }

        @Override
        public Metadata[] newArray(int size) {
            return new Metadata[size];
        }
    };
}