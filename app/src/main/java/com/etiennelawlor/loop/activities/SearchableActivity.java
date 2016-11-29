package com.etiennelawlor.loop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.bus.RxBus;
import com.etiennelawlor.loop.bus.events.LeftDrawableClickedEvent;
import com.etiennelawlor.loop.fragments.SearchableFragment;
import com.google.android.gms.actions.SearchIntents;

import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 9/23/15.
 */
public class SearchableActivity extends AppCompatActivity {

    // region Member Variables
    private CompositeSubscription compositeSubscription;
    // endregion

    // region Lifecycle Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        ButterKnife.bind(this);

        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())
                ||  SearchIntents.ACTION_SEARCH.equals(getIntent().getAction())) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_fl);
            if(fragment == null){
                fragment = SearchableFragment.newInstance(getIntent().getExtras());
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content_fl, fragment, "")
                        .commit();
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .attach(fragment)
                        .commit();
            }
        }

        compositeSubscription = new CompositeSubscription();

        setUpRxBusSubscription();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeSubscription.unsubscribe();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_fl);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    // region Helper Methods
    private void setUpRxBusSubscription(){
        Subscription rxBusSubscription = RxBus.getInstance().toObserverable()
                .observeOn(AndroidSchedulers.mainThread()) // UI Thread
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if (event == null) {
                            return;
                        }

                        if(event instanceof LeftDrawableClickedEvent){
                            LeftDrawableClickedEvent.Type type = ((LeftDrawableClickedEvent)event).getType();

                            if(type == LeftDrawableClickedEvent.Type.BACK)
                                finish();
//            onBackPressed();
                        }
                    }
                });

        compositeSubscription.add(rxBusSubscription);
    }
    // endregion
}
