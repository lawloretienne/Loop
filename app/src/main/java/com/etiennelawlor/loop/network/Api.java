package com.etiennelawlor.loop.network;

import com.etiennelawlor.loop.BuildConfig;
import com.etiennelawlor.loop.LoopApplication;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import retrofit.Endpoint;
import retrofit.Endpoints;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public final class Api {

    // region Constants
    private static final int DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB
    // endregion

    // region Static Variables
    private static final ConcurrentHashMap<EndpointUrl, Service> sServices = new ConcurrentHashMap<>();
    // endregion

    // region Member Variables
    private final String mUrl;
    // endregion

    // region Constructors
    private Api(EndpointUrl url) {
        mUrl = url.toString();
    }
    // endregion

    // region Helper Methods
    private Endpoint getEndpoint() {
        return Endpoints.newFixedEndpoint(mUrl);
    }

    private RequestInterceptor getHeaders() {
        return new ApiHeaders();
    }

    private RestAdapter getRestAdapter() {
        RestAdapter.LogLevel logLevel;
        if (BuildConfig.DEBUG){
            logLevel = RestAdapter.LogLevel.FULL;
        } else {
            logLevel = RestAdapter.LogLevel.NONE;
        }

        return new RestAdapter.Builder()
                .setEndpoint(getEndpoint())
                .setClient(getClient())
                .setRequestInterceptor(getHeaders())
                .setLogLevel(logLevel)
//                .setLog(getLog())
                .build();
    }

    private Client getClient() {
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

    private SSLSocketFactory createBadSslSocketFactory() {
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
    // endregion

    public static Service getService(EndpointUrl endpointUrl) {
        if (sServices.containsKey(endpointUrl)) {
            return sServices.get(endpointUrl);
        } else {
            Service service = (new Api(endpointUrl))
                    .getRestAdapter()
                    .create(Service.class);

            sServices.putIfAbsent(endpointUrl, service);

            return service;
        }
    }

    public static EndpointUrl getEndpointUrl(){
        return EndpointUrl.VIMEO_API;
    }
}
