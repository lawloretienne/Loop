package com.etiennelawlor.loop.network;

import com.etiennelawlor.loop.network.models.VideoConfig;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by etiennelawlor on 6/14/15.
 */
public interface VimeoPlayerService {

    public static final String BASE_URL = "http://player.vimeo.com";

    @GET("/video/{videoId}/config")
    void getVideoConfig(@Path("videoId") Long videoId,
                        Callback<VideoConfig> cb);
}
