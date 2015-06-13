package com.etiennelawlor.loop.network;

import retrofit.RequestInterceptor;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class ApiHeaders implements RequestInterceptor {

    // region Constructors
    public ApiHeaders(){
    }
    // endregion

    @Override
    public void intercept(RequestFacade request) {

        String token = "8b07ae8d27264b6436ee72c740e8017c";
        request.addHeader("Authorization", "bearer " + token);
        request.addHeader("Accept", "application/vnd.vimeo.*+json; version=3.2");
//        request.addQueryParam("client_id", "309011f9713d22ace9b976909ed34a80");
    }
}