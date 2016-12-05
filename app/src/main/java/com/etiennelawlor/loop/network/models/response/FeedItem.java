package com.etiennelawlor.loop.network.models.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by etiennelawlor on 12/5/16.
 */

public class FeedItem {

    // region Fields
    @SerializedName("uri")
    public Object uri;
    @SerializedName("clip")
    public Video clip;
    @SerializedName("type")
    public String type;
    @SerializedName("time")
    public String time;
    @SerializedName("metadata")
    public Metadata metadata;
    @SerializedName("channel")
    public Channel channel;
    // endregion

    // region Getters

    public Object getUri() {
        return uri;
    }

    public Video getClip() {
        return clip;
    }

    public String getType() {
        return type;
    }

    public String getTime() {
        return time;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public Channel getChannel() {
        return channel;
    }

    // endregion

    // region Setters

    public void setUri(Object uri) {
        this.uri = uri;
    }

    public void setClip(Video clip) {
        this.clip = clip;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    // endregion
}
