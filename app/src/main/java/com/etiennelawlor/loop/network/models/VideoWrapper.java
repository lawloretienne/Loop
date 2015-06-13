package com.etiennelawlor.loop.network.models;

import android.support.annotation.IntDef;

public final class VideoWrapper {

    //region SearchWrapper Types
    public static final int VIDEO = 0;
    public static final int LOADING = 2;
    public static final int HEADER = 3;
    public static final int NONE = -1;
    //endregion

    //region Variables
    private Video Video;
    private int Type;
    //endregion

    //region Constructors
    private VideoWrapper() {
        Video = null;
        Type = NONE;
    }

    public static VideoWrapper createVideoType(Video item) {
        VideoWrapper vw = new VideoWrapper();
        vw.Type = VIDEO;
        vw.Video = item;

        return vw;
    }

    public static VideoWrapper createHeaderType() {
        VideoWrapper vw = new VideoWrapper();
        vw.Type = HEADER;

        return vw;
    }

    public static VideoWrapper createLoadingType() {
        VideoWrapper vw = new VideoWrapper();
        vw.Type = LOADING;

        return vw;
    }
    //endregion

    //region Getters
    public Video getVideo() {
        return Video;
    }

    @Type public int getType() {
        return Type;
    }
    //endregion

    // region Setters
    public void setVideo(Video video) {
        Video = video;
    }

    public void setType(@Type int type) {
        Type = type;
    }
    // endregion

    @IntDef({VIDEO, NONE, HEADER, LOADING})
    public @interface Type {
    }
}
