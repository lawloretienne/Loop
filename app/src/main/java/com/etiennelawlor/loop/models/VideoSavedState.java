package com.etiennelawlor.loop.models;

/**
 * Created by etiennelawlor on 10/8/15.
 */
public class VideoSavedState {

    // region Fields
    private String videoUrl;
    private long currentPosition;
    // endregion

    public VideoSavedState(){

    }

    // region Getters
    public String getVideoUrl() {
        return videoUrl;
    }

    public long getCurrentPosition() {
        return currentPosition;
    }
    // endregion

    // region Setters
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setCurrentPosition(long currentPosition) {
        this.currentPosition = currentPosition;
    }
    // endregion
}
