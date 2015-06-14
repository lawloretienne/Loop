package com.etiennelawlor.loop.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.fragments.VideoDetailsFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * Created by etiennelawlor on 5/23/15.
 */
public class VideoDetailsActivity extends AppCompatActivity {

    // region Member Variables
    @InjectView(R.id.toolbar) Toolbar mToolbar;
    // endregion

    // region Lifecycle Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_details);
        ButterKnife.inject(this);

        setSupportActionBar(mToolbar);

        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("");

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.content_fl, VideoDetailsFragment.newInstance(getIntent().getExtras()), "")
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
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
//                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
