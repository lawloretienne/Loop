package com.etiennelawlor.loop.network.models.response;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Comment implements Parcelable {

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
        dest.writeString(getUri());
        dest.writeString(getType());
        dest.writeString(getText());
        dest.writeString(getCreatedOn());
        dest.writeParcelable(getUser(), flags);
        dest.writeParcelable(getMetadata(), flags);
    }
    // endregion

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {

        @Override
        public Comment createFromParcel(Parcel source) {
            Comment video = new Comment();

            video.setUri(source.readString());
            video.setType(source.readString());
            video.setText(source.readString());
            video.setCreatedOn(source.readString());
            video.setUser((User) source.readParcelable(User.class.getClassLoader()));
            video.setMetadata((Metadata) source.readParcelable(Metadata.class.getClassLoader()));

            return video;
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };
}