package com.etiennelawlor.loop.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.activities.SearchableActivity;
import com.etiennelawlor.loop.otto.BusProvider;
import com.etiennelawlor.loop.otto.events.HideSearchSuggestionsEvent;
import com.etiennelawlor.loop.otto.events.SearchPerformedEvent;
import com.etiennelawlor.loop.otto.events.ShowSearchSuggestionsEvent;
import com.etiennelawlor.loop.realm.RealmUtility;
import com.etiennelawlor.loop.ui.MaterialSearchView;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by etiennelawlor on 5/23/15.
 */
public class WatchNowFragment extends BaseFragment {

    // region Constants
    // endregion

    // region Member Variables
    @Bind(R.id.viewpager)
    ViewPager mViewPager;
    @Bind(R.id.tabs)
    TabLayout mTabLayout;
    @Bind(R.id.material_sv)
    MaterialSearchView mMaterialSearchView;
    // endregion

    // region Callbacks
    // endregion

    // region Constructors
    public WatchNowFragment() {
    }
    // endregion

    // region Factory Methods
    public static WatchNowFragment newInstance() {
        return new WatchNowFragment();
    }

    public static WatchNowFragment newInstance(Bundle extras) {
        WatchNowFragment fragment = new WatchNowFragment();
        fragment.setArguments(extras);
        return fragment;
    }
    // endregion

    // region Lifecycle Methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
//            mQuery = getArguments().getString("query");
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_watch_now, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
//
//        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();

//        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
//        ab.setHomeAsUpIndicator(R.drawable.ic_menu_black);
//        ab.setDisplayHomeAsUpEnabled(true);
//        ab.setTitle("Watch Now");
//        ab.setTitle("");

        if (mViewPager != null) {
            setupViewPager(mViewPager);
        }

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
    // endregion

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

//        inflater.inflate(R.menu.watch_now_menu, menu);

        // Get the SearchView and set the searchable configuration
//        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
//
//        // Assumes current activity is the searchable activity
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
//        searchView.setQueryRefinementEnabled(true);
//        searchView.setIconifiedByDefault(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    // region Otto Methods
    @Subscribe
    public void onSearchPerformed(SearchPerformedEvent event) {
        String query = event.getQuery();
        if (!TextUtils.isEmpty(query)) {
            mMaterialSearchView.setQuery("");
            launchSearchActivity(query);
        }
    }

    @Subscribe
    public void onShowSearchSuggestions(ShowSearchSuggestionsEvent event) {
        String query = event.getQuery();

        mMaterialSearchView.addSuggestions(RealmUtility.getSuggestions(query));
    }

    @Subscribe
    public void onHideSearchSuggestions(HideSearchSuggestionsEvent event) {
//        showFAB();
    }
    // endregion

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == Activity.RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
//                    mMaterialSearchView.setQuery(searchWrd);
                    mMaterialSearchView.setQuery("");
                    launchSearchActivity(searchWrd);
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // region Helper Methods
    private void setupViewPager(ViewPager viewPager) {
//        Adapter adapter = new Adapter(getActivity().getSupportFragmentManager());
        Adapter adapter = new Adapter(getChildFragmentManager());

        adapter.addFragment(setUpFragment(getString(R.string.bodyboarding)), getString(R.string.bodyboarding));
        adapter.addFragment(setUpFragment(getString(R.string.surfing)), getString(R.string.surfing));
        adapter.addFragment(setUpFragment(getString(R.string.wind_surfing)), getString(R.string.wind_surfing));
        adapter.addFragment(setUpFragment(getString(R.string.snowboarding)), getString(R.string.snowboarding));
        adapter.addFragment(setUpFragment(getString(R.string.skiing)), getString(R.string.skiing));
        adapter.addFragment(setUpFragment(getString(R.string.skateboarding)), getString(R.string.skateboarding));
        adapter.addFragment(setUpFragment(getString(R.string.bmx)), getString(R.string.bmx));
        adapter.addFragment(setUpFragment(getString(R.string.motocross)), getString(R.string.motocross));
        viewPager.setAdapter(adapter);
    }

    private Fragment setUpFragment(String query) {
        Bundle bundle = new Bundle();
        bundle.putString("query", query);

        return VideosFragment.newInstance(bundle);
//        return PlaceholderFragment.newInstance();
    }

    private void launchSearchActivity(String query) {
        Intent intent = new Intent(getContext(), SearchableActivity.class);
        intent.setAction(Intent.ACTION_SEARCH);
        intent.putExtra(SearchManager.QUERY, query);
        getContext().startActivity(intent);
    }
    // endregion

    // region Inner Classes
    public static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }
    // endregion
}
