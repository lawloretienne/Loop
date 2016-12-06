package com.etiennelawlor.loop.network.interceptors;

import android.content.Context;
import android.util.Base64;

import com.etiennelawlor.loop.R;
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

public class UnauthorizedNetworkInterceptor implements Interceptor {

    private Context context;

    public UnauthorizedNetworkInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (chain != null) {
            Request originalRequest = chain.request();

            Map<String, String> headersMap = new HashMap<>();

            String clientId = context.getString(R.string.client_id);
            String clientSecret = context.getString(R.string.client_secret);

            // concatenate username and password with colon for authentication
            final String credentials = clientId + ":" + clientSecret;
            String authorization = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
            headersMap.put("Authorization", authorization);
            headersMap.put("Accept", "application/json");
            Request modifiedRequest = RequestUtility.updateHeaders(originalRequest, headersMap);

            return chain.proceed(modifiedRequest);
        }

        return null;
    }
}
