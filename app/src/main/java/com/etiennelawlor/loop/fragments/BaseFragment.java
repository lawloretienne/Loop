package com.etiennelawlor.loop.fragments;

import android.support.v4.app.Fragment;

import com.etiennelawlor.loop.LoopApplication;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by etiennelawlor on 6/13/15.
 */
public abstract class BaseFragment extends Fragment {

    @Override
    public void onDestroy() {
        super.onDestroy();

        RefWatcher refWatcher = LoopApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }
}
