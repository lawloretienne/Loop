package com.etiennelawlor.loop.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.adapters.CategoriesAdapter;
import com.etiennelawlor.loop.helper.PreferencesHelper;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoService;
import com.etiennelawlor.loop.models.AccessToken;
import com.etiennelawlor.loop.network.models.response.CategoriesCollection;
import com.etiennelawlor.loop.network.models.response.Category;
import com.etiennelawlor.loop.otto.BusProvider;
import com.etiennelawlor.loop.ui.GridSpacesItemDecoration;
import com.etiennelawlor.loop.ui.LoadingImageView;
import com.etiennelawlor.loop.utilities.LogUtility;
import com.etiennelawlor.loop.utilities.LoopUtility;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 6/13/15.
 */
public class ExploreFragment extends BaseFragment implements CategoriesAdapter.OnItemClickListener {

    // region Constants
    // endregion

    // region Member Variables
    @Bind(R.id.categories_rv)
    RecyclerView mCategoriesRecyclerView;
    @Bind(android.R.id.empty)
    View mEmptyView;
    @Bind(R.id.loading_iv)
    LoadingImageView mLoadingImageView;
    @Bind(R.id.error_ll)
    LinearLayout mErrorLinearLayout;
    @Bind(R.id.error_tv)
    TextView mErrorTextView;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    private boolean mIsLoading = false;
    private CategoriesAdapter mCategoriesAdapter;
    private LinearLayoutManager mLayoutManager;
    private VimeoService mVimeoService;
    // endregion

    // region Listeners
    // endregion

    // region Callbacks
    private Callback<CategoriesCollection> mGetCategoriesCallback = new Callback<CategoriesCollection>() {
        @Override
        public void onResponse(Response<CategoriesCollection> response, Retrofit retrofit) {
            Timber.d("onResponse()");

            mLoadingImageView.setVisibility(View.GONE);
            mIsLoading = false;

            if (response != null) {
                if(response.isSuccess()){
                    CategoriesCollection categoriesCollection = response.body();
                    if (categoriesCollection != null) {
                        List<Category> categories = categoriesCollection.getCategories();
                        mCategoriesAdapter.addAll(categories);
                    }
                } else {
                    com.squareup.okhttp.Response rawResponse = response.raw();
                    if (rawResponse != null) {
                        LogUtility.logFailedResponse(rawResponse);

                        int code = rawResponse.code();
                        switch (code) {
                            case 500:
                                mErrorTextView.setText("Can't load data.\nCheck your network connection.");
                                mErrorLinearLayout.setVisibility(View.VISIBLE);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }

        @Override
        public void onFailure(Throwable t) {
            Timber.d("onFailure()");

            if (t != null) {
                String message = t.getMessage();
                LogUtility.logFailure(t);

                if (t instanceof SocketTimeoutException || t instanceof UnknownHostException) {
                    Timber.e("Timeout occurred");
                    mIsLoading = false;
                    mLoadingImageView.setVisibility(View.GONE);

                    mErrorTextView.setText("Can't load data.\nCheck your network connection.");
                    mErrorLinearLayout.setVisibility(View.VISIBLE);
                } else if(t instanceof IOException){
                    if(message.equals("Canceled")){
                        Timber.e("onFailure() : Canceled");
                    } else {
                        mIsLoading = false;
                        mLoadingImageView.setVisibility(View.GONE);
                    }
                }
            }
        }
    };
    // endregion

    // region Constructors
    public static ExploreFragment newInstance() {
        ExploreFragment fragment = new ExploreFragment();
        return fragment;
    }

    public static ExploreFragment newInstance(Bundle extras) {
        ExploreFragment fragment = new ExploreFragment();
        fragment.setArguments(extras);
        return fragment;
    }

    public ExploreFragment() {
    }
    // endregion

    // region Lifecycle Methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
//            mQuery = getArguments().getString("query");
        }

        AccessToken token = PreferencesHelper.getAccessToken(getActivity());
        mVimeoService = ServiceGenerator.createService(
                VimeoService.class,
                VimeoService.BASE_URL,
                token);

        setHasOptionsMenu(true);
        BusProvider.get().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_explore, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Explore");

//        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(mQuery);

        mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mCategoriesRecyclerView.setLayoutManager(mLayoutManager);
        mCategoriesRecyclerView.addItemDecoration(new GridSpacesItemDecoration(LoopUtility.dp2px(getActivity(), 8)));

        mCategoriesAdapter = new CategoriesAdapter();
        mCategoriesAdapter.setOnItemClickListener(this);

        mCategoriesRecyclerView.setItemAnimator(new SlideInUpAnimator());
        mCategoriesRecyclerView.setAdapter(mCategoriesAdapter);

        Call getCategoriesCall = mVimeoService.getCategories();
        mCalls.add(getCategoriesCall);
        getCategoriesCall.enqueue(mGetCategoriesCallback);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

//        mVideosRecyclerView.removeOnScrollListener(mRecyclerViewOnScrollListener);
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

        inflater.inflate(R.menu.explore_menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setQueryRefinementEnabled(true);
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//
//        inflater.inflate(R.menu.main_menu, menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
////            case R.id.sort_by:
////                showSortByDialog();
////                break;
////            case R.id.sort_order:
////                showSortOrderDialog();
////                break;
//            default:
//                // do nothing
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    // region CategoriesAdapter.OnItemClickListener Methods
    @Override
    public void onItemClick(int position, View view) {

        Category category = mCategoriesAdapter.getItem(position);
        Timber.d("");

//        if (videoWrapper != null) {
//            Video video = videoWrapper.getVideo();
//            if (video != null) {
//                Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
//
//                Bundle bundle = new Bundle();
//                bundle.putParcelable("video", video);
//                intent.putExtras(bundle);
//
//                Pair<View, String> p1 = Pair.create((View) view.findViewById(R.id.video_thumbnail_iv), "videoTransition");
//                Pair<View, String> p2 = Pair.create((View) view.findViewById(R.id.title_tv), "titleTransition");
//                Pair<View, String> p3 = Pair.create((View) view.findViewById(R.id.subtitle_tv), "subtitleTransition");
////        Pair<View, String> p4 = Pair.create((View)view.findViewById(R.id.uploaded_tv), "uploadedTransition");
//
//                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
//                        p1, p2, p3);
//
//
//                ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
//
////                startActivity(intent);
//            }
//        }

    }
    // endregion

    // region Helper Methods
    // endregion
}
