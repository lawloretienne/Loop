package com.etiennelawlor.loop.network;

import com.etiennelawlor.loop.network.models.OAuthResponse;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by etiennelawlor on 6/14/15.
 */
public interface LoginService {

    public static final String BASE_URL = "https://api.vimeo.com";

    @FormUrlEncoded
    @POST("/oauth/access_token")
    void exchangeCode(@Field("grant_type") String grantType,
                      @Field("code") String code,
                      @Field("redirect_uri") String redirectUri,
                      Callback<OAuthResponse> cb);
}
