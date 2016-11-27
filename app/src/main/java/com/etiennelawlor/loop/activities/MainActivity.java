package com.etiennelawlor.loop.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.fragments.ExploreFragment;
import com.etiennelawlor.loop.fragments.LikedVideosFragment;
import com.etiennelawlor.loop.fragments.PlaceholderFragment;
import com.etiennelawlor.loop.fragments.WatchLaterVideosFragment;
import com.etiennelawlor.loop.fragments.WatchNowFragment;
import com.etiennelawlor.loop.network.models.response.AuthorizedUser;
import com.etiennelawlor.loop.network.models.response.Picture;
import com.etiennelawlor.loop.otto.BusProvider;
import com.etiennelawlor.loop.otto.events.LeftDrawableClickedEvent;
import com.etiennelawlor.loop.prefs.LoopPrefs;
import com.etiennelawlor.loop.utilities.EmailUtility;
import com.etiennelawlor.loop.utilities.FontCache;
import com.etiennelawlor.loop.utilities.TrestleUtility;
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

    // region Constants
    private static final String WATCH_NOW = "Watch Now";
    private static final String LIKES = "Likes";
    private static final String WATCH_LATER = "Watch Later";
    private static final String EXPLORE = "Explore";
    private static final String SETTINGS = "Settings";
    private static final String HELP_AND_FEEDBACK = "Help and Feedback";
    private static final String LOGOUT = "Logout";
    // endregion

    // region Views
    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @Bind(R.id.nav_view)
    NavigationView navigationView;

    private CircleImageView avatarImageView;
    private TextView fullNameTextView;
    // endregion

    // region Member Variables
    private Typeface font;
    private AuthorizedUser authorizedUser;
    // endregion

    // region Listeners
    private NavigationView.OnNavigationItemSelectedListener mNavigationViewOnNavigationItemSelectedListener
            = new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    drawerLayout.closeDrawers();

                    String title = menuItem.getTitle().toString();
                    switch (title) {
                        case WATCH_NOW:
                            if(!menuItem.isChecked()){
                                menuItem.setChecked(true);
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                                        .replace(R.id.content_fl, WatchNowFragment.newInstance(), "")
                                        .commit();
                            }
                            break;
                        case LIKES:
                            if(!menuItem.isChecked()) {
                                menuItem.setChecked(true);
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                                        .replace(R.id.content_fl, LikedVideosFragment.newInstance(), "")
                                        .commit();
                            }
                            break;
                        case WATCH_LATER:
                            if(!menuItem.isChecked()) {
                                menuItem.setChecked(true);
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                                        .replace(R.id.content_fl, WatchLaterVideosFragment.newInstance(), "")
                                        .commit();
                            }
                            break;
                        case EXPLORE:
                            if(!menuItem.isChecked()) {
                                menuItem.setChecked(true);
                                getSupportFragmentManager()
                                        .beginTransaction()
                                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                                        .replace(R.id.content_fl, ExploreFragment.newInstance(), "")
                                        .commit();
                            }
                            break;
                        case SETTINGS:
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                                    .replace(R.id.content_fl, PlaceholderFragment.newInstance(), "")
                                    .commit();
                            break;
                        case HELP_AND_FEEDBACK:
                            try {
                                startActivity(EmailUtility.getEmailIntent(MainActivity.this));
                            } catch (android.content.ActivityNotFoundException ex) {
                                Snackbar.make(findViewById(android.R.id.content),
                                        TrestleUtility.getFormattedText("There are no email apps installed on your device", font, 16),
                                        Snackbar.LENGTH_LONG)
                                        .show();
                            }
                            break;
                        case LOGOUT:
                            LoopPrefs.signOut(MainActivity.this);
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

        font = FontCache.getTypeface("Ubuntu-Medium.ttf", this);

        authorizedUser = LoopPrefs.getAuthorizedUser(this);

        View header = LayoutInflater.from(this).inflate(R.layout.nav_header, null);
        avatarImageView = (CircleImageView) header.findViewById(R.id.user_avatar_riv);
        fullNameTextView = (TextView) header.findViewById(R.id.full_name_tv);
        navigationView.addHeaderView(header);

        setUpAvatar();
        setUpFullName();
        formatMenuItems();

        // Setup NavigationView
        navigationView.setNavigationItemSelectedListener(mNavigationViewOnNavigationItemSelectedListener);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_fl, WatchNowFragment.newInstance(), "")
                .commit();

        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
    }
    // endregion

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
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
    public void onLeftDrawableClicked(LeftDrawableClickedEvent event) {
        Timber.d("onLeftDrawableClickedEvent");

        LeftDrawableClickedEvent.Type type = event.getType();

        if(type == LeftDrawableClickedEvent.Type.MENU)
            drawerLayout.openDrawer(GravityCompat.START);

    }
    // endregion

    // region Helper Methods
    private void setUpAvatar(){
        if(authorizedUser != null){
            List<Picture> pictures = authorizedUser.getPictures();
            if(pictures != null && pictures.size()>0){
                Picture picture = pictures.get(pictures.size() - 1);
                if(picture != null){
                    String link = picture.getLink();
                    if(!TextUtils.isEmpty(link)){
                        Glide.with(this)
                                .load(link)
//                                .placeholder(R.drawable.ic_placeholder)
//                                .error(R.drawable.ic_error)
                                .into(avatarImageView);
                    }
                }
            }
        }
    }

    private void setUpFullName(){
        if(authorizedUser != null){
            String name = authorizedUser.getName();
            if(!TextUtils.isEmpty(name)){
                fullNameTextView.setText(name);
            }
        }
    }

    private void formatMenuItems() {
        Menu menu = navigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem mi = menu.getItem(i);

            SubMenu subMenu = mi.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }

            applyFontToMenuItem(mi);
        }
    }

    private void applyFontToMenuItem(MenuItem mi) {
        mi.setTitle(TrestleUtility.getFormattedText(mi.getTitle().toString(), font));
    }
    // endregion
}
