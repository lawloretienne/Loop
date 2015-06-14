package com.etiennelawlor.loop.activities;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Toast;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.fragments.ExploreFragment;
import com.etiennelawlor.loop.fragments.PlaceholderFragment;
import com.etiennelawlor.loop.fragments.WatchNowFragment;
import com.etiennelawlor.loop.utilities.LoopUtility;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;


/**
 * Created by etiennelawlor on 5/23/15.
 */
public class MainActivity extends AppCompatActivity {

    // region Member Variables
    private CharSequence mTitle;
    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @InjectView(R.id.nav_view)
    NavigationView mNavigationView;
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
                        case "Favorites":
                            Timber.d("");
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.content_fl, PlaceholderFragment.newInstance(), "")
                                    .commit();
                            break;
                        case "History":
                            Timber.d("");
                            getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.content_fl, PlaceholderFragment.newInstance(), "")
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
        ButterKnife.inject(this);

        // Setup NavigationView
        mNavigationView.setNavigationItemSelectedListener(mNavigationViewOnNavigationItemSelectedListener);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_fl, WatchNowFragment.newInstance(), "")
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    // region Helper Methods
    // endregion
}
