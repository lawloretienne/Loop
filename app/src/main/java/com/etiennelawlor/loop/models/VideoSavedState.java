package com.etiennelawlor.loop.models;

/**
 * Created by etiennelawlor on 10/8/15.
 */
public class VideoSavedState {

    // region Fields
    private String mVideoUrl;
    private int mCurrentPosition;
    // endregion

    public VideoSavedState(){

    }

    // region Getters
    public String getVideoUrl() {
        return mVideoUrl;
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }
    // endregion

    // region Setters
    public void setVideoUrl(String videoUrl) {
        mVideoUrl = videoUrl;
    }

    public void setCurrentPosition(int currentPosition) {
        mCurrentPosition = currentPosition;
    }
    // endregion
}
