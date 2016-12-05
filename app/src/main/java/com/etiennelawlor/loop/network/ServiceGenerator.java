package com.etiennelawlor.loop.network;

import android.util.Base64;

import com.etiennelawlor.loop.BuildConfig;
import com.etiennelawlor.loop.LoopApplication;
import com.etiennelawlor.loop.models.AccessToken;
import com.etiennelawlor.loop.utilities.RequestUtility;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 6/14/15.
 */
public class ServiceGenerator {

    private static Retrofit.Builder retrofitBuilder =
            new Retrofit.Builder();

    // No need to instantiate this class.
    private ServiceGenerator() {
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl, final String clientId, final String clientSecret) {

        OkHttpClient defaultOkHttpClient = LoopApplication.getOkHttpClient();

        OkHttpClient modifiedOkHttpClient = defaultOkHttpClient.newBuilder()
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        if (chain != null) {
                            Request originalRequest = chain.request();

                            Map<String, String> headersMap = new HashMap<>();

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
                })
                .addInterceptor(getHttpLoggingInterceptor())
                .build();

        retrofitBuilder.client(modifiedOkHttpClient);
        retrofitBuilder.baseUrl(baseUrl);
        retrofitBuilder.addCallAdapterFactory(RxJavaCallAdapterFactory.create());
        retrofitBuilder.addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = retrofitBuilder.build();
        return retrofit.create(serviceClass);
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl, final AccessToken accessToken) {

        OkHttpClient defaultOkHttpClient = LoopApplication.getOkHttpClient();

        OkHttpClient modifiedOkHttpClient = defaultOkHttpClient.newBuilder()
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        if (chain != null) {
                            Request originalRequest = chain.request();

                            if (accessToken != null) {
                                Map<String, String> headersMap = new HashMap<>();
                                String authorization = accessToken.getTokenType() + " " + accessToken.getAccessToken();
                                headersMap.put("Authorization", authorization);
                                headersMap.put("Accept", "application/vnd.vimeo.*+json; version=3.2");
                                Request modifiedRequest = RequestUtility.updateHeaders(originalRequest, headersMap);

                                Timber.d("Authorization : " + authorization);

                                return chain.proceed(modifiedRequest);
                            } else {
                                return chain.proceed(originalRequest);
                            }
                        }

                        return null;
                    }
                })
                .addInterceptor(getHttpLoggingInterceptor())
                .build();

        retrofitBuilder.client(modifiedOkHttpClient);
        retrofitBuilder.baseUrl(baseUrl);
        retrofitBuilder.addCallAdapterFactory(RxJavaCallAdapterFactory.create());
        retrofitBuilder.addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = retrofitBuilder.build();
        return retrofit.create(serviceClass);
    }

    private static HttpLoggingInterceptor getHttpLoggingInterceptor(){
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        if (BuildConfig.DEBUG) {
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
        return httpLoggingInterceptor;
    }

    private static class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            long t1 = System.nanoTime();
            Timber.i(String.format("Sending request %s on %s%n%s",
                    request.url(), chain.connection(), request.headers()));

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            Timber.i(String.format("Received response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.headers()));

            return response;
        }
    }
}

