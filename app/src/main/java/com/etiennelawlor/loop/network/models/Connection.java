package com.etiennelawlor.loop.network.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Connection implements Parcelable {

    // region Member Variables
    @SerializedName("uri")
    private String uri;
    @SerializedName("options")
    private List<String> options;
    @SerializedName("total")
    private Integer total;
    // endregion

    // region Getters
    public String getUri() {
        if (TextUtils.isEmpty(uri))
            return "";
        else
            return uri;
    }

    public List<String> getOptions() {
        return options;
    }

    public Integer getTotal() {
        if(total == null)
            return -1;
        else
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

    public void setTotal(Integer total) {
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
        dest.writeString(getUri());
        dest.writeStringList(getOptions());
        dest.writeInt(getTotal());
    }
    // endregion

    public static final Parcelable.Creator<Connection> CREATOR = new Parcelable.Creator<Connection>() {

        @Override
        public Connection createFromParcel(Parcel source) {
            Connection connection = new Connection();

            connection.setUri(source.readString());

            List<String> options = new ArrayList<String>();
            source.readStringList(options);
            connection.setOptions(options);

            connection.setTotal(source.readInt());

            return connection;
        }

        @Override
        public Connection[] newArray(int size) {
            return new Connection[size];
        }
    };
}