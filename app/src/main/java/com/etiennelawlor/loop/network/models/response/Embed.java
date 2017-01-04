package com.etiennelawlor.loop.network.models.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Embed implements Parcelable {

    // region Fields
    @SerializedName("html")
    private String html;
    // endregion

    // region Constructors
    public Embed() {
    }

    protected Embed(Parcel in) {
        this.html = in.readString();
    }
    // endregion

    // region Getters

    public String getHtml() {
        return html;
    }

    // endregion

    // region Setters
    public void setHtml(String html) {
        this.html = html;
    }
    // endregion

    // region Parcelable Methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.html);
    }
    // endregion

    public static final Parcelable.Creator<Embed> CREATOR = new Parcelable.Creator<Embed>() {
        @Override
        public Embed createFromParcel(Parcel source) {
            return new Embed(source);
        }

        @Override
        public Embed[] newArray(int size) {
            return new Embed[size];
        }
    };
}
