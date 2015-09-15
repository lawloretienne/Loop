package com.etiennelawlor.loop.network;

import android.text.TextUtils;
import android.util.Base64;

import com.etiennelawlor.loop.BuildConfig;
import com.etiennelawlor.loop.LoopApplication;
import com.etiennelawlor.loop.network.models.AccessToken;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 6/14/15.
 */
public class ServiceGenerator {

    // region Constants
    private static final int DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB
    // endregion

//    private static RestAdapter.Builder builder =
//        new RestAdapter.Builder()
//            .setClient(getClient())
//            .setLogLevel(getLogLevel());

    private static Retrofit.Builder sRetrofitBuilder =
            new Retrofit.Builder();

    // No need to instantiate this class.
    private ServiceGenerator() {
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl, final String clientId, final String clientSecret) {

        OkHttpClient okHttpClient = getClient();
        okHttpClient.interceptors().add(new LoggingInterceptor());
        okHttpClient.networkInterceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                if (chain != null) {
                    Request originalRequest = chain.request();

                    if (!TextUtils.isEmpty(clientId) && !TextUtils.isEmpty(clientSecret)) {
                        // concatenate username and password with colon for authentication
                        final String credentials = clientId + ":" + clientSecret;

                        String authorization = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

                        Request modifiedRequest = originalRequest.newBuilder()
                                .header("Authorization", authorization)
                                .header("Accept", "application/json")
                                .build();

                        return chain.proceed(modifiedRequest);
                    } else {
                        return chain.proceed(originalRequest);
                    }
                }

                return null;
            }
        });

        sRetrofitBuilder.client(okHttpClient);
        sRetrofitBuilder.baseUrl(baseUrl);
        sRetrofitBuilder.addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = sRetrofitBuilder.build();
        return retrofit.create(serviceClass);
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl, final AccessToken accessToken) {
        OkHttpClient okHttpClient = getClient();
        okHttpClient.interceptors().add(new LoggingInterceptor());
        okHttpClient.networkInterceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                if(chain != null){
                    Request originalRequest = chain.request();

                    if (accessToken != null) {
                        Request modifiedRequest = originalRequest.newBuilder()
                                .header("Authorization", accessToken.getTokenType() + " " + accessToken.getAccessToken())
                                .header("Accept", "application/vnd.vimeo.*+json; version=3.2")
                                .build();

                        return chain.proceed(modifiedRequest);
                    } else {
                        return chain.proceed(originalRequest);
                    }
                }

                return null;
            }
        });

        sRetrofitBuilder.client(okHttpClient);
        sRetrofitBuilder.baseUrl(baseUrl);
        sRetrofitBuilder.addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = sRetrofitBuilder.build();
        return retrofit.create(serviceClass);
    }

    private static OkHttpClient getClient() {
        OkHttpClient client = new OkHttpClient();

        // Install an HTTP cache in the application cache directory.
        try {
            File cacheDir = new File(LoopApplication.getCacheDirectory(), "http");
            Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);
            client.setCache(cache);
        } catch (Exception e) {
            Timber.e(e, "Unable to install disk cache.");
        }
        client.setSslSocketFactory(createBadSslSocketFactory());

        return client;
    }

    private static SSLSocketFactory createBadSslSocketFactory() {
        try {
            // Construct SSLSocketFactory that accepts any cert.
            SSLContext context = SSLContext.getInstance("TLS");
            TrustManager permissive = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            context.init(null, new TrustManager[] { permissive }, null);
            return context.getSocketFactory();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static class LoggingInterceptor implements Interceptor {
        @Override public Response intercept(Chain chain) throws IOException {
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

//    private static RestAdapter.LogLevel getLogLevel(){
//        RestAdapter.LogLevel logLevel;
//        if (BuildConfig.DEBUG){
//            logLevel = RestAdapter.LogLevel.FULL;
//        } else {
//            logLevel = RestAdapter.LogLevel.NONE;
//        }
//
//        return logLevel;
//    }
}

