package com.etiennelawlor.loop.network;

import com.etiennelawlor.loop.network.models.CategoriesCollection;
import com.etiennelawlor.loop.network.models.VideoConfig;
import com.etiennelawlor.loop.network.models.VideosCollection;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public interface Service {

    @GET("/videos")
    void findVideos(@Query("query") String query,
                    @Query("sort") String sort,
                    @Query("direction") String direction,
                    @Query("page") Integer page,
                    @Query("per_page") Integer perPage,
                    Callback<VideosCollection> cb);

    @GET("/videos/{videoId}/videos?filter=related")
    void findRelatedVideos( @Path("videoId") Long videoId,
                            @Query("page") Integer page,
                            @Query("per_page") Integer perPage,
                            Callback<VideosCollection> cb);

    @GET("/video/{videoId}/config")
    void getVideoConfig(@Path("videoId") Long videoId,
                        Callback<VideoConfig> cb);

    @GET("/categories")
    void getCategories(Callback<CategoriesCollection> cb);

}