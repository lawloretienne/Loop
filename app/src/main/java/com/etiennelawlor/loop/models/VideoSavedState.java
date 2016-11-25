package com.etiennelawlor.loop.models;

/**
 * Created by etiennelawlor on 10/8/15.
 */
public class VideoSavedState {

    // region Fields
    private String videoUrl;
    private int currentPosition;
    // endregion

    public VideoSavedState(){

    }

    // region Getters
    public String getVideoUrl() {
        return videoUrl;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }
    // endregion

    // region Setters
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }
    // endregion
}
