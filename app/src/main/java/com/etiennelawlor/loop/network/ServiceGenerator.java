package com.etiennelawlor.loop.network;

import android.text.TextUtils;
import android.util.Base64;

import com.etiennelawlor.loop.BuildConfig;
import com.etiennelawlor.loop.LoopApplication;
import com.etiennelawlor.loop.network.models.AccessToken;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 6/14/15.
 */
public class ServiceGenerator {

    // region Constants
    private static final int DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB
    // endregion

    private static RestAdapter.Builder builder =
        new RestAdapter.Builder()
            .setClient(getClient())
            .setLogLevel(getLogLevel());

    // No need to instantiate this class.
    private ServiceGenerator() {
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl, String clientId, String clientSecret) {
        // set endpoint url
        builder.setEndpoint(baseUrl);

        if (!TextUtils.isEmpty(clientId) && !TextUtils.isEmpty(clientSecret)) {
            // concatenate username and password with colon for authentication
            final String credentials = clientId + ":" + clientSecret;

            builder.setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    // create Base64 encoded string
                    String string = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                    request.addHeader("Authorization", string);
                    request.addHeader("Accept", "application/json");
                }
            });
        }

        RestAdapter adapter = builder.build();

        return adapter.create(serviceClass);
    }

    public static <S> S createService(Class<S> serviceClass, String baseUrl, final AccessToken accessToken) {
        // set endpoint url
        builder.setEndpoint(baseUrl);

        if (accessToken != null) {
            builder.setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addHeader("Authorization", accessToken.getTokenType() + " " + accessToken.getAccessToken());
//                    request.addHeader("Accept", "application/json");
                    request.addHeader("Accept", "application/vnd.vimeo.*+json; version=3.2");

                }
            });
        }

        RestAdapter adapter = builder.build();

        return adapter.create(serviceClass);
    }

    private static Client getClient() {
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

        return new OkClient(client);
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

    private static RestAdapter.LogLevel getLogLevel(){
        RestAdapter.LogLevel logLevel;
        if (BuildConfig.DEBUG){
            logLevel = RestAdapter.LogLevel.FULL;
        } else {
            logLevel = RestAdapter.LogLevel.NONE;
        }

        return logLevel;
    }
}

