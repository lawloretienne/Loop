package com.etiennelawlor.loop.network;

import com.etiennelawlor.loop.network.models.request.CommentPost;
import com.etiennelawlor.loop.network.models.response.CategoriesCollection;
import com.etiennelawlor.loop.network.models.response.Comment;
import com.etiennelawlor.loop.network.models.response.CommentsCollection;
import com.etiennelawlor.loop.network.models.response.OAuthResponse;
import com.etiennelawlor.loop.network.models.response.VideosCollection;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    @GET("/videos/{videoId}/comments")
    Call<CommentsCollection> getComments(@Path("videoId") Long videoId,
                                         @Query("sort") String sort,
                                         @Query("direction") String direction,
                                         @Query("page") Integer page,
                                         @Query("per_page") Integer perPage);

    @POST("/videos/{videoId}/comments")
    Call<Comment> addComment(@Path("videoId") Long videoId,
                             @Body CommentPost commentPost);

    @DELETE("/videos/{videoId}/comments/{commentId}")
    Call<ResponseBody> deleteComment(@Path("videoId") Long videoId,
                               @Path("commentId") Long commentId);

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

    @PUT("/me/likes/{videoId}")
    Call<ResponseBody> likeVideo(@Path("videoId") String videoId);


    @DELETE("/me/likes/{videoId}")
    Call<ResponseBody> unlikeVideo(@Path("videoId") String videoId);

    @PUT("/me/watchlater/{videoId}")
    Call<ResponseBody> addVideoToWatchLater(@Path("videoId") String videoId);


    @DELETE("/me/watchlater/{videoId}")
    Call<ResponseBody> removeVideoFromWatchLater(@Path("videoId") String videoId);

}
