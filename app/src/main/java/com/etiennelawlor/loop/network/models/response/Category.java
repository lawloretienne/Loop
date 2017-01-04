package com.etiennelawlor.loop.network.models.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class Category implements Parcelable {

    // region Fields
    @SerializedName("uri")
    private String uri;
    @SerializedName("name")
    private String name;
    @SerializedName("link")
    private String link;
    @SerializedName("top_level")
    private boolean topLevel;
    @SerializedName("pictures")
    private Pictures pictures;
    @SerializedName("metadata")
    private Metadata metadata;
    // endregion

    // region Constructors
    public Category() {
    }

    protected Category(Parcel in) {
        this.uri = in.readString();
        this.name = in.readString();
        this.link = in.readString();
        this.topLevel = in.readByte() != 0;
        this.pictures = in.readParcelable(Pictures.class.getClassLoader());
        this.metadata = in.readParcelable(Metadata.class.getClassLoader());
    }
    // endregion

    // region Getters

    public String getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    public boolean isTopLevel() {
        return topLevel;
    }

    public Pictures getPictures() {
        return pictures;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public String getThumbnailUrl() {
        String thumbnailUrl = "";
        if(pictures != null) {
            List<Size> sizes = pictures.getSizes();
            if (sizes != null && sizes.size() > 0) {
                Size size = sizes.get(sizes.size() - 1);
                if (size != null) {
                    thumbnailUrl = size.getLink();
                }
            }
        }
        return thumbnailUrl;
    }

    public String getFormattedCategoryName(){
        String formattedCategoryName = name;
        if(formattedCategoryName.contains("&")){
            formattedCategoryName = formattedCategoryName.replace("&", "\n&");
        }
        return formattedCategoryName;
    }
    // endregion

    // region Setters

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setTopLevel(boolean topLevel) {
        this.topLevel = topLevel;
    }

    public void setPictures(Pictures pictures) {
        this.pictures = pictures;
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
        dest.writeString(this.name);
        dest.writeString(this.link);
        dest.writeByte(this.topLevel ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.pictures, flags);
        dest.writeParcelable(this.metadata, flags);
    }
    // endregion

    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel source) {
            return new Category(source);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
}