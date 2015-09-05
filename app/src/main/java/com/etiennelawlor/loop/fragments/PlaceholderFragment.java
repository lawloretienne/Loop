package com.etiennelawlor.loop.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.otto.BusProvider;

import butterknife.ButterKnife;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class PlaceholderFragment extends BaseFragment {

    // region Constants
    // endregion

    // region Member Variables
    // endregion

    // region Callbacks
    // endregion

    // region Constructors
    public static PlaceholderFragment newInstance() {
        PlaceholderFragment fragment = new PlaceholderFragment();
        return fragment;
    }

    public static PlaceholderFragment newInstance(Bundle extras) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        fragment.setArguments(extras);
        return fragment;
    }

    public PlaceholderFragment() {
    }
    // endregion

    // region Lifecycle Methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BusProvider.get().register(this);

        if(getArguments() != null){
//            mQuery = getArguments().getString("query");
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_placeholder, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    // region Helper Methods
    // endregion
}
