package com.etiennelawlor.loop.network;

import com.etiennelawlor.loop.network.models.CategoriesCollection;
import com.etiennelawlor.loop.network.models.OAuthResponse;
import com.etiennelawlor.loop.network.models.VideosCollection;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by etiennelawlor on 6/14/15.
 */
public interface VimeoService {

    public static final String BASE_URL = "https://api.vimeo.com";

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

    @GET("/categories")
    void getCategories(Callback<CategoriesCollection> cb);

    @FormUrlEncoded
    @POST("/oauth/access_token")
    void exchangeCode(@Field("grant_type") String grantType,
                      @Field("code") String code,
                      @Field("redirect_uri") String redirectUri,
                      Callback<OAuthResponse> cb);

}
