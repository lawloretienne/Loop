package com.etiennelawlor.loop.network.models.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Connection implements Parcelable {

    // region Fields
    @SerializedName("uri")
    private String uri;
    @SerializedName("options")
    private List<String> options;
    @SerializedName("total")
    private int total;
    // endregion

    // region Constructors
    public Connection() {
    }

    protected Connection(Parcel in) {
        this.uri = in.readString();
        this.options = in.createStringArrayList();
        this.total = in.readInt();
    }
    // endregion

    // region Getters

    public String getUri() {
        return uri;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getTotal() {
        return total;
    }

    // endregion

    // region Setters

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    // endregion

    // region Parcelable Methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uri);
        dest.writeStringList(this.options);
        dest.writeInt(this.total);
    }
    // endregion

    public static final Parcelable.Creator<Connection> CREATOR = new Parcelable.Creator<Connection>() {
        @Override
        public Connection createFromParcel(Parcel source) {
            return new Connection(source);
        }

        @Override
        public Connection[] newArray(int size) {
            return new Connection[size];
        }
    };
}