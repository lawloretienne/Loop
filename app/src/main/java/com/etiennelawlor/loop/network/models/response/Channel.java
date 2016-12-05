package com.etiennelawlor.loop.network.models.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 12/5/16.
 */

public class Channel {

    // region Fields
    @SerializedName("uri")
    public String uri;
    @SerializedName("name")
    public String name;
    @SerializedName("description")
    public String description;
    @SerializedName("link")
    public String link;
    @SerializedName("created_time")
    public String createdTime;
    @SerializedName("modified_time")
    public String modifiedTime;
    @SerializedName("user")
    public User user;
    @SerializedName("pictures")
    public Pictures pictures;
    @SerializedName("header")
    public Object header;
//    @SerializedName("privacy")
//    public Privacy privacy;
    @SerializedName("metadata")
    public Metadata metadata;
    @SerializedName("resource_key")
    public String resourceKey;
    // endregion

    // region Getters

    public String getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public String getModifiedTime() {
        return modifiedTime;
    }

    public User getUser() {
        return user;
    }

    public Pictures getPictures() {
        return pictures;
    }

    public Object getHeader() {
        return header;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    // endregion

    // region Setters

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public void setModifiedTime(String modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setPictures(Pictures pictures) {
        this.pictures = pictures;
    }

    public void setHeader(Object header) {
        this.header = header;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    // endregion
}
