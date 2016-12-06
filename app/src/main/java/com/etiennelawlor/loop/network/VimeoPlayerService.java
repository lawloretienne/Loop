package com.etiennelawlor.loop.network;

import com.etiennelawlor.loop.network.models.response.VideoConfig;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by etiennelawlor on 6/14/15.
 */
public interface VimeoPlayerService {

    String BASE_URL = "http://player.vimeo.com";

    @GET("/video/{videoId}/config")
    Call<VideoConfig> getVideoConfig(@Path("videoId") Long videoId);
}
