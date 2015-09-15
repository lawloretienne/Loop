package com.etiennelawlor.loop.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.etiennelawlor.loop.LoopApplication;
import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import timber.log.Timber;

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

        String className = this.getClass().toString();
        Timber.d("onDestory() : className - "+ className);
        if(this instanceof VideosFragment){
            String query = ((VideosFragment)this).getQuery();
            Timber.d("onDestory() : query - "+ query);
        }

        Timber.d("onDestory() : mCalls.size() - "+ mCalls.size());

        for(Call call : mCalls){
            Timber.d("onDestory() : call.cancel()");

            call.cancel();
        }
    }
}
