package com.etiennelawlor.loop.network;

import com.etiennelawlor.loop.network.models.CategoriesCollection;
import com.etiennelawlor.loop.network.models.OAuthResponse;
import com.etiennelawlor.loop.network.models.VideosCollection;

import retrofit.Call;
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

    String BASE_URL = "https://api.vimeo.com";

    @GET("/videos")
    Call<VideosCollection> findVideos(@Query("query") String query,
                    @Query("sort") String sort,
                    @Query("direction") String direction,
                    @Query("page") Integer page,
                    @Query("per_page") Integer perPage);

    @GET("/me/likes")
    Call<VideosCollection> findLikedVideos(@Query("query") String query,
                        @Query("sort") String sort,
                        @Query("direction") String direction,
                        @Query("page") Integer page,
                        @Query("per_page") Integer perPage);

    @GET("/me/watchlater")
    Call<VideosCollection> findWatchLaterVideos(@Query("query") String query,
                             @Query("sort") String sort,
                             @Query("direction") String direction,
                             @Query("page") Integer page,
                             @Query("per_page") Integer perPage);

    @GET("/videos/{videoId}/videos?filter=related")
    Call<VideosCollection> findRelatedVideos( @Path("videoId") Long videoId,
                            @Query("page") Integer page,
                            @Query("per_page") Integer perPage);

    @GET("/categories")
    Call<CategoriesCollection> getCategories();

    @FormUrlEncoded
    @POST("/oauth/access_token")
    Call<OAuthResponse> exchangeCode(@Field("grant_type") String grantType,
                      @Field("code") String code,
                      @Field("redirect_uri") String redirectUri);

}
