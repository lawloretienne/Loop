package com.etiennelawlor.loop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.fragments.ExploreFragment;
import com.etiennelawlor.loop.fragments.LikedVideosFragment;
import com.etiennelawlor.loop.fragments.PlaceholderFragment;
import com.etiennelawlor.loop.fragments.WatchLaterVideosFragment;
import com.etiennelawlor.loop.fragments.WatchNowFragment;
import com.etiennelawlor.loop.helper.PreferencesHelper;
import com.etiennelawlor.loop.network.models.response.AuthorizedUser;
import com.etiennelawlor.loop.network.models.response.Picture;
import com.etiennelawlor.loop.otto.BusProvider;
import com.etiennelawlor.loop.otto.events.LeftDrawableClickedEvent;
import com.etiennelawlor.loop.utilities.LoopUtility;
import com.squareup.otto.Subscribe;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;


/**
 * Created by etiennelawlor on 5/23/15.
 */
public class MainActivity extends AppCompatActivity {

    // region Member Variables
    private CharSequence mTitle;
    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @Bind(R.id.nav_view)
    NavigationView mNavigationView;
    @Bind(R.id.user_avatar_riv)
    CircleImageView mAvatarImageView;
    @Bind(R.id.full_name_tv)
    TextView mFullNameTextView;

    private AuthorizedUser mAuthorizedUser;
    // endregion

    // region Listeners
    private NavigationView.OnNavigationItemSelectedListener mNavigationViewOnNavigationItemSelectedListener
            = new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    menuItem.setChecked(true);
                    mDrawerLayout.closeDrawers();

                    String title = menuItem.getTitle().toString();
                    switch (title) {
                        case "Watch Now":
                            Timber.d("");

                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.content_fl, WatchNowFragment.newInstance(), "")
                                    .commit();

                            break;
                        case "Likes":
                            Timber.d("");
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.content_fl, LikedVideosFragment.newInstance(), "")
                                    .commit();
                            break;
                        case "Watch Later":
                            Timber.d("");
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.content_fl, WatchLaterVideosFragment.newInstance(), "")
                                    .commit();
                            break;
                        case "Explore":
                            Timber.d("");
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.content_fl, ExploreFragment.newInstance(), "")
                                    .commit();
                            break;
                        case "Settings":
                            Timber.d("");
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.content_fl, PlaceholderFragment.newInstance(), "")
                                    .commit();
                            break;
                        case "Help and Feedback":
                            try {
                                startActivity(LoopUtility.getEmailIntent(MainActivity.this));
                            } catch (android.content.ActivityNotFoundException ex) {
                                Toast.makeText(MainActivity.this, "There are no email clients installed", Toast.LENGTH_SHORT);
                            }
                            break;
                        case "Logout":
                            PreferencesHelper.signOut(MainActivity.this);
                            startActivity(new Intent(MainActivity.this, LauncherActivity.class));
                        default:
                            break;
                    }
                    return true;
                }
    };
    // endregion

    // region Lifecycle Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if(intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
//                mAuthorizedUser = (AuthorizedUser) extras.get(getString(R.string.authorized_user));
            }
        }

        mAuthorizedUser = PreferencesHelper.getAuthorizedUser(this);

        setUpAvatar();
        setUpFullName();

        // Setup NavigationView
        mNavigationView.setNavigationItemSelectedListener(mNavigationViewOnNavigationItemSelectedListener);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_fl, WatchNowFragment.newInstance(), "")
                .commit();

        BusProvider.get().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.get().unregister(this);
    }
    // endregion

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mTitle);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_fl);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    // region Otto Methods
    @Subscribe
    public void onLeftDrawableClickedEvent(LeftDrawableClickedEvent event) {
        Timber.d("onLeftDrawableClickedEvent");

        LeftDrawableClickedEvent.Type type = event.getType();

        if(type == LeftDrawableClickedEvent.Type.MENU)
            mDrawerLayout.openDrawer(GravityCompat.START);

    }
    // endregion

    // region Helper Methods
    private void setUpAvatar(){
        if(mAuthorizedUser != null){
            List<Picture> pictures = mAuthorizedUser.getPictures();
            if(pictures != null && pictures.size()>0){
                Picture picture = pictures.get(pictures.size() - 1);
                if(picture != null){
                    String link = picture.getLink();
                    if(!TextUtils.isEmpty(link)){
                        Glide.with(this)
                                .load(link)
//                                .placeholder(R.drawable.ic_placeholder)
//                                .error(R.drawable.ic_error)
                                .into(mAvatarImageView);
                    }
                }
            }
        }
    }

    private void setUpFullName(){
        if(mAuthorizedUser != null){
            String name = mAuthorizedUser.getName();
            if(!TextUtils.isEmpty(name)){
                mFullNameTextView.setText(name);
            }
        }
    }
    // endregion
}
