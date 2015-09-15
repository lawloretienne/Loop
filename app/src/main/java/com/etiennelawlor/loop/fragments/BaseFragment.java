package com.etiennelawlor.loop.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.etiennelawlor.loop.LoopApplication;
import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;

/**
 * Created by etiennelawlor on 6/13/15.
 */
public abstract class BaseFragment extends Fragment {

    protected List<Call> mCalls;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCalls = new ArrayList<>();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        RefWatcher refWatcher = LoopApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);

        for(Call call : mCalls){
            call.cancel();
        }
    }
}
