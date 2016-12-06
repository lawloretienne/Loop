package com.etiennelawlor.loop.network.interceptors;

import com.etiennelawlor.loop.models.AccessToken;
import com.etiennelawlor.loop.utilities.RequestUtility;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by etiennelawlor on 12/5/16.
 */

public class AuthorizedNetworkInterceptor implements Interceptor {

    private AccessToken accessToken;

    public AuthorizedNetworkInterceptor(AccessToken accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (chain != null) {
            Request originalRequest = chain.request();

            if (accessToken != null) {
                Map<String, String> headersMap = new HashMap<>();
                headersMap.put("Authorization", String.format("%s %s", accessToken.getTokenType(), accessToken.getAccessToken()));
                headersMap.put("Accept", "application/vnd.vimeo.*+json; version=3.2");
                Request modifiedRequest = RequestUtility.updateHeaders(originalRequest, headersMap);

                return chain.proceed(modifiedRequest);
            } else {
                return chain.proceed(originalRequest);
            }
        }

        return null;
    }
}
