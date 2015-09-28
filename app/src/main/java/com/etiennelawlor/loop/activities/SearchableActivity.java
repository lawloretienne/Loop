package com.etiennelawlor.loop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.fragments.SearchableFragment;
import com.google.android.gms.actions.SearchIntents;

import butterknife.ButterKnife;

/**
 * Created by etiennelawlor on 9/23/15.
 */
public class SearchableActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        ButterKnife.bind(this);


        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())
                ||  SearchIntents.ACTION_SEARCH.equals(getIntent().getAction())) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, SearchableFragment.newInstance(getIntent().getExtras()), "")
                    .commit();
        }
    }

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
