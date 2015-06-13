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
public class Embed implements Parcelable {

    // region Member Variables
    @SerializedName("html")
    private String html;
    // endregion

    // region Getters
    public String getHtml() {
        if (TextUtils.isEmpty(html))
            return "";
        else
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
        dest.writeString(getHtml());
    }
    // endregion

    public static final Creator<Embed> CREATOR = new Creator<Embed>() {

        @Override
        public Embed createFromParcel(Parcel source) {
            Embed embed = new Embed();

            embed.setHtml(source.readString());

            return embed;
        }

        @Override
        public Embed[] newArray(int size) {
            return new Embed[size];
        }
    };
}
