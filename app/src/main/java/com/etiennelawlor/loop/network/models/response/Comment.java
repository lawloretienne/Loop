package com.etiennelawlor.loop.network.models.response;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.utilities.DateUtility;
import com.etiennelawlor.trestle.library.Span;
import com.etiennelawlor.trestle.library.Trestle;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Comment implements Parcelable {

    // region Constants
    public static final String PATTERN = "yyyy-MM-dd'T'hh:mm:ssZ";
    // endregion

    // region Fields
    @SerializedName("uri")
    private String uri;
    @SerializedName("type")
    private String type;
    @SerializedName("text")
    private String text;
    @SerializedName("created_on")
    private String createdOn;
    @SerializedName("user")
    private User user;
    @SerializedName("metadata")
    private Metadata metadata;
    // endregion

    // region Constructors
    public Comment() {
    }

    protected Comment(Parcel in) {
        this.uri = in.readString();
        this.type = in.readString();
        this.text = in.readString();
        this.createdOn = in.readString();
        this.user = in.readParcelable(User.class.getClassLoader());
        this.metadata = in.readParcelable(Metadata.class.getClassLoader());
    }
    // endregion

    // region Getters
    public String getUri() {
        return TextUtils.isEmpty(uri) ? "" : uri;
    }

    public String getType() {
        return TextUtils.isEmpty(type) ? "" : type;
    }

    public String getText() {
        return TextUtils.isEmpty(text) ? "" : text;
    }

    public String getCreatedOn() {
        return TextUtils.isEmpty(createdOn) ? "" : createdOn;
    }

    public User getUser() {
        return user;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public long getId() {
        long id = -1L;
        String uri = getUri();
        if (!TextUtils.isEmpty(uri)) {
            String lastPathSegment = Uri.parse(uri).getLastPathSegment();
            id = Long.parseLong(lastPathSegment);
        }
        return id;
    }

    public String getFormattedCreatedOn(){
        String formattedCreatedOn = DateUtility.getFormattedDateAndTime(DateUtility.getCalendar(createdOn, PATTERN), DateUtility.FORMAT_RELATIVE);
        return formattedCreatedOn;
    }
    // endregion

    // region Setters
    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
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
        dest.writeString(this.type);
        dest.writeString(this.text);
        dest.writeString(this.createdOn);
        dest.writeParcelable(this.user, flags);
        dest.writeParcelable(this.metadata, flags);
    }
    // endregion

    public static final Parcelable.Creator<Comment> CREATOR = new Parcelable.Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel source) {
            return new Comment(source);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };
}