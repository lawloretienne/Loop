package com.etiennelawlor.loop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.helper.PreferencesHelper;
import com.etiennelawlor.loop.network.models.AccessToken;
import com.etiennelawlor.loop.network.models.AuthorizedUser;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by etiennelawlor on 6/20/15.
 */
public class LauncherActivity extends AppCompatActivity {

    // region Member Variables
    // endregion

    // region Listeners
    @OnClick(R.id.login_btn)
    void onLoginClicked() {
        startActivity(new Intent(LauncherActivity.this, LoginActivity.class));
    }
    // endregion

    // region Callbacks
    // endregion

    // region Lifecycle Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        ButterKnife.bind(this);

        AccessToken token = PreferencesHelper.getAccessToken(this);
        AuthorizedUser authorizedUser = PreferencesHelper.getAuthorizedUser(this);
        if(token != null && authorizedUser != null){
            Intent intent = new Intent(LauncherActivity.this, MainActivity.class);
//            intent.putExtra(getString(R.string.authorized_user), authorizedUser);
            startActivity(intent);
            finish();
        }

    }
    // endregion
}
