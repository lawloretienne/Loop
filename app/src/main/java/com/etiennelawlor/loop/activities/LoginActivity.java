package com.etiennelawlor.loop.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.helper.PreferencesHelper;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoService;
import com.etiennelawlor.loop.network.models.AccessToken;
import com.etiennelawlor.loop.network.models.AuthorizedUser;
import com.etiennelawlor.loop.network.models.OAuthResponse;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 6/13/15.
 */
public class LoginActivity extends AppCompatActivity {

    // region Member Variables
    @InjectView(R.id.wv)
    WebView mWebView;

    private WebViewClient mWebViewClient = new WebViewClient() {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Timber.i("Processing webview url click..."); // http://localhost/?code=0e4c71d1ed6f61c70b708a6098f37337033082ff
            if(!TextUtils.isEmpty(url) && !url.startsWith(getString(R.string.client_redirect_uri))){
                view.loadUrl(url); // Uri.parse(url).getQueryParameter("code")
            }

            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url)  {
            Timber.i("Finished loading URL: " +url);
//                setSupportProgressBarIndeterminateVisibility(false);
//                mSmoothProgressBar.setVisibility(View.GONE);

            if(!TextUtils.isEmpty(url) && url.startsWith(getString(R.string.client_redirect_uri))){
                Uri uri = Uri.parse(url);
                String state = uri.getQueryParameter("state");
                if(state.equals(getString(R.string.vimeo_state))){
                    String code = uri.getQueryParameter("code");

                    VimeoService vimeoService = ServiceGenerator.createService(
                            VimeoService.class,
                            VimeoService.BASE_URL,
                            getString(R.string.client_id),
                            getString(R.string.client_secret));
                    vimeoService.exchangeCode("authorization_code",
                            code,
                            getString(R.string.client_redirect_uri),
                            mExchangeCodeCallback);
                }

            }
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Timber.e("Error: " + errorCode + ":" + description + ":" + failingUrl);

//                mSmoothProgressBar.setVisibility(View.GONE);

            if(!isFinishing()){
//                    alertDialog.setTitle("Error");
//                    alertDialog.setMessage("Unable to connect. Please check your internet connectivity.");
//                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    });
//
//                    alertDialog.show();
            }
        }
    };
    // endregion

    // region Listeners
    // endregion

    // region Callbacks
    private Callback<OAuthResponse> mExchangeCodeCallback = new Callback<OAuthResponse>() {
        @Override
        public void success(OAuthResponse oAuthResponse, Response response) {
            if(oAuthResponse != null){
                String accessToken = oAuthResponse.getAccessToken();
                String tokenType = oAuthResponse.getTokenType();
                AuthorizedUser authorizedUser = oAuthResponse.getUser();

                AccessToken token = new AccessToken(tokenType, accessToken);
                PreferencesHelper.saveAccessToken(getApplicationContext(), token);
                PreferencesHelper.saveAuthorizedUser(getApplicationContext(), authorizedUser);

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra(getString(R.string.authorized_user), authorizedUser);
                startActivity(intent);
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Timber.d("");
        }
    };
    // endregion

    // region Lifecycle Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);
        webSettings.setUseWideViewPort(true);

        mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mWebView.setWebViewClient(mWebViewClient);

        mWebView.loadUrl(setUpAuthorizeUrl());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    // endregion

    // region Helper Methods
    private String setUpAuthorizeUrl(){
        String authroizeUrl;

        String webLoginClientId = getString(R.string.client_id);
        String scope = TextUtils.join(" ", getResources().getStringArray(R.array.scopes));
        String redirect_uri = getString(R.string.client_redirect_uri);
        String state = getString(R.string.vimeo_state);

        authroizeUrl =
                String.format("https://api.vimeo.com/oauth/authorize?scope=%s&client_id=%s&response_type=code&redirect_uri=%s&state=%s",
                        scope,
                        webLoginClientId,
                        redirect_uri,
                        state);

        return authroizeUrl;
    }
    // endregion
}
