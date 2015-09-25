package com.etiennelawlor.loop.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.otto.BusProvider;

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
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    // endregion

    // region Callbacks
    // endregion

    // region Constructors
    public static WatchNowFragment newInstance() {
        WatchNowFragment fragment = new WatchNowFragment();
        return fragment;
    }

    public static WatchNowFragment newInstance(Bundle extras) {
        WatchNowFragment fragment = new WatchNowFragment();
        fragment.setArguments(extras);
        return fragment;
    }

    public WatchNowFragment() {
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
        BusProvider.get().register(this);
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

//        getActivity()).setSupportActionBar(mToolbar);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Watch Now");


        if (mViewPager != null) {
            setupViewPager(mViewPager);
        }

        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister Otto Bus
        BusProvider.get().unregister(this);
    }
    // endregion

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.watch_now_menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setQueryRefinementEnabled(true);
        searchView.setIconifiedByDefault(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
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
    }
    // endregion

    // region Inner Classes
    static class Adapter extends FragmentPagerAdapter {
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
