package com.etiennelawlor.loop.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.models.AccessToken;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoService;
import com.etiennelawlor.loop.network.interceptors.UnauthorizedNetworkInterceptor;
import com.etiennelawlor.loop.network.models.response.AuthorizedUser;
import com.etiennelawlor.loop.network.models.response.OAuthResponse;
import com.etiennelawlor.loop.prefs.LoopPrefs;
import com.etiennelawlor.loop.utilities.NetworkLogUtility;
import com.etiennelawlor.loop.utilities.NetworkUtility;

import java.net.ConnectException;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 6/13/15.
 */
public class LoginActivity extends AppCompatActivity {

    // region Views
    @Bind(R.id.wv)
    WebView webView;
    // endregion

    // region Member Variables
    private VimeoService vimeoService;

    private WebViewClient webViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri uri = Uri.parse(url);

            String code = uri.getQueryParameter("code");
            String state = uri.getQueryParameter("state");
            if(!TextUtils.isEmpty(code)
                && !TextUtils.isEmpty(state)
                && state.equals(getString(R.string.vimeo_state))) {

                Call exchangeCodeCall = vimeoService.exchangeCode("authorization_code",
                        code,
                        getString(R.string.client_redirect_uri));
                exchangeCodeCall.enqueue(exchangeCodeCallback);
            }


            return super.shouldOverrideUrlLoading(view, url);
        }
    };
    // endregion

    // region Callbacks
    private Callback<OAuthResponse> exchangeCodeCallback = new Callback<OAuthResponse>() {
        @Override
        public void onResponse(Call<OAuthResponse> call, Response<OAuthResponse> response) {

            if (!response.isSuccessful()) {
                int responseCode = response.code();
                if(responseCode == 504) { // 504 Unsatisfiable Request (only-if-cached)
//                    errorTextView.setText("Can't load data.\nCheck your network connection.");
//                    errorLinearLayout.setVisibility(View.VISIBLE);
                }
                return;
            }

            OAuthResponse oAuthResponse = response.body();
            if (oAuthResponse != null) {
                String accessToken = oAuthResponse.getAccessToken();
                String tokenType = oAuthResponse.getTokenType();
                AuthorizedUser authorizedUser = oAuthResponse.getUser();

                AccessToken token = new AccessToken(tokenType, accessToken);
                LoopPrefs.saveAccessToken(getApplicationContext(), token);
                LoopPrefs.saveAuthorizedUser(getApplicationContext(), authorizedUser);

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }

        @Override
        public void onFailure(Call<OAuthResponse> call, Throwable t) {
            NetworkLogUtility.logFailure(call, t);

            if (!call.isCanceled()){
                if(NetworkUtility.isKnownException(t)){
//                errorTextView.setText("Can't load data.\nCheck your network connection.");
//                errorLinearLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    };
    // endregion

    // region Lifecycle Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        vimeoService = ServiceGenerator.createService(
                VimeoService.class,
                VimeoService.BASE_URL,
                new UnauthorizedNetworkInterceptor(this));

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);
        webSettings.setUseWideViewPort(true);

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setWebViewClient(webViewClient);

        webView.loadUrl(setUpUrl());
    }
    // endregion

    // region Helper Methods
    private String setUpUrl() {
        String authroizeUrl = getString(R.string.authorize_url);
        String webLoginClientId = getString(R.string.client_id);
        String redirectUri = getString(R.string.client_redirect_uri);
        String scope = TextUtils.join(" ", getResources().getStringArray(R.array.scopes));
        String state = getString(R.string.vimeo_state);

        String url =
                String.format("%s?client_id=%s&response_type=code&redirect_uri=%s&state=%s&scope=%s",
                        authroizeUrl,
                        webLoginClientId,
                        redirectUri,
                        state,
                        scope);

        return url;
    }
    // endregion
}
