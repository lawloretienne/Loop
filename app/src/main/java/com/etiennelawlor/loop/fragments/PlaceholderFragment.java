package com.etiennelawlor.loop.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.etiennelawlor.loop.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class PlaceholderFragment extends BaseFragment {

    // region Constants
    // endregion

    // region Member Variables
    private Unbinder unbinder;
    // endregion

    // region Callbacks
    // endregion

    // region Constructors
    public PlaceholderFragment() {
    }
    // endregion

    // region Factory Methods
    public static PlaceholderFragment newInstance() {
        return new PlaceholderFragment();
    }

    public static PlaceholderFragment newInstance(Bundle extras) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        fragment.setArguments(extras);
        return fragment;
    }
    // endregion

    // region Lifecycle Methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null){
//            mQuery = getArguments().getString("query");
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_placeholder, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
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
