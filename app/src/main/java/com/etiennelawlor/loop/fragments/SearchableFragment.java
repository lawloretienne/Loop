package com.etiennelawlor.loop.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.activities.SearchableActivity;
import com.etiennelawlor.loop.activities.VideoDetailsActivity;
import com.etiennelawlor.loop.adapters.VideosAdapter;
import com.etiennelawlor.loop.helper.PreferencesHelper;
import com.etiennelawlor.loop.models.AccessToken;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoService;
import com.etiennelawlor.loop.network.models.response.Video;
import com.etiennelawlor.loop.network.models.response.VideosCollection;
import com.etiennelawlor.loop.otto.BusProvider;
import com.etiennelawlor.loop.otto.events.FilterClickedEvent;
import com.etiennelawlor.loop.otto.events.SearchPerformedEvent;
import com.etiennelawlor.loop.otto.events.ShowSearchSuggestionsEvent;
import com.etiennelawlor.loop.realm.RealmUtility;
import com.etiennelawlor.loop.ui.LoadingImageView;
import com.etiennelawlor.loop.ui.MaterialSearchView;
import com.etiennelawlor.loop.utilities.LogUtility;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class SearchableFragment extends BaseFragment implements VideosAdapter.OnItemClickListener {

    // region Constants
    public static final int PAGE_SIZE = 30;
    // endregion

    // region Member Variables
    @Bind(R.id.videos_rv)
    RecyclerView mVideosRecyclerView;
    @Bind(android.R.id.empty)
    View mEmptyView;
    @Bind(R.id.empty_tv)
    TextView mEmptyTextView;
    @Bind(R.id.loading_iv)
    LoadingImageView mLoadingImageView;
    @Bind(R.id.error_ll)
    LinearLayout mErrorLinearLayout;
    @Bind(R.id.error_tv)
    TextView mErrorTextView;
    @Bind(R.id.material_sv)
    MaterialSearchView mMaterialSearchView;
//    @Bind(R.id.toolbar)
//    Toolbar mToolbar;

    private boolean mIsLastPage = false;
    private int mCurrentPage = 1;
    private int mSelectedSortByKey = 0;
    private int mSelectedSortOrderKey = 1;
    private boolean mIsLoading = false;
    private String mSortByValue = "relevant";
    private String mSortOrderValue = "desc";
    private VideosAdapter mVideosAdapter;
    private String mQuery;
    private LinearLayoutManager mLayoutManager;
    private VimeoService mVimeoService;
    // endregion

    // region Listeners
    private RecyclerView.OnScrollListener mRecyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mLayoutManager.getChildCount();
            int totalItemCount = mLayoutManager.getItemCount();
            int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();

            if (!mIsLoading && !mIsLastPage) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
                    loadMoreItems();
                }
            }
        }
    };

    private View.OnClickListener mReloadOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCurrentPage -= 1;
            mVideosAdapter.addLoading();
            loadMoreItems();
        }
    };

    @OnClick(R.id.reload_btn)
    public void onReloadButtonClicked() {
        mErrorLinearLayout.setVisibility(View.GONE);
        mLoadingImageView.setVisibility(View.VISIBLE);

        mMaterialSearchView.disableFilter();

        Call findVideosCall = mVimeoService.findVideos(mQuery,
                mSortByValue,
                mSortOrderValue,
                mCurrentPage,
                PAGE_SIZE);
        mCalls.add(findVideosCall);
        findVideosCall.enqueue(mFindVideosFirstFetchCallback);
    }

//    @OnClick(R.id.fab)
//    @SuppressWarnings("UnusedDeclaration")
//    public void onSortFABClicked() {
//        showSortDialog();
//    }
    // endregion

    // region Callbacks
    private Callback<VideosCollection> mFindVideosFirstFetchCallback = new Callback<VideosCollection>() {
        @Override
        public void onResponse(Response<VideosCollection> response, Retrofit retrofit) {
            Timber.d("onResponse()");
            mLoadingImageView.setVisibility(View.GONE);
            mIsLoading = false;

            if (response != null) {
                if(response.isSuccess()){
                    VideosCollection videosCollection = response.body();
                    if (videosCollection != null) {
                        List<Video> videos = videosCollection.getVideos();
                        if (videos != null) {
                            mVideosAdapter.addAll(videos);

                            if(videos.size() >= PAGE_SIZE){
                                mVideosAdapter.addLoading();
                            } else {
                                mIsLastPage = true;
                            }
                        }
                    }

                    mMaterialSearchView.enableFilter();
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

            if (mVideosAdapter.isEmpty()) {
                mEmptyTextView.setText(getString(R.string.watch_later_empty_prompt));
                Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_watch_later_large);
                DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), R.color.grey_500));
                mEmptyTextView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
                mEmptyView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onFailure(Throwable t) {
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

    private Callback<VideosCollection> mFindVideosNextFetchCallback = new Callback<VideosCollection>() {
        @Override
        public void onResponse(Response<VideosCollection> response, Retrofit retrofit) {
            Timber.d("onResponse()");
            mVideosAdapter.removeLoading();
            mIsLoading = false;

            if (response != null) {
                if(response.isSuccess()){
                    VideosCollection videosCollection = response.body();
                    if (videosCollection != null) {
                        List<Video> videos = videosCollection.getVideos();
                        if (videos != null) {
                            mVideosAdapter.addAll(videos);

                            if(videos.size() >= PAGE_SIZE){
                                mVideosAdapter.addLoading();
                            } else {
                                mIsLastPage = true;
                            }
                        }
                    }
                } else {
                    com.squareup.okhttp.Response rawResponse = response.raw();
                    if (rawResponse != null) {
                        LogUtility.logFailedResponse(rawResponse);

                        int code = rawResponse.code();
                        switch (code) {
                            case 500:
                                Timber.e("Display error message in place of load more");
//                                mErrorTextView.setText("Can't load data.\nCheck your network connection.");
//                                mErrorLinearLayout.setVisibility(View.VISIBLE);
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
            mVideosAdapter.removeLoading();
            if (t != null) {
                String message = t.getMessage();
                LogUtility.logFailure(t);

                if (t instanceof SocketTimeoutException) {
                    showReloadSnackbar(String.format("message - %s", message));
                } else if (t instanceof UnknownHostException) {
                    Timber.e("Timeout occurred");
                    showReloadSnackbar("Can't load data. Check your network connection.");
                } else if(t instanceof IOException){
                    if(message.equals("Canceled")){
                        Timber.e("onFailure() : Canceled");
                    }
                }
            }
        }
    };
    // endregion

    // region Constructors
    public SearchableFragment() {
    }
    // endregion

    // region Factory Methods
    public static SearchableFragment newInstance() {
        return new SearchableFragment();
    }

    public static SearchableFragment newInstance(Bundle extras) {
        SearchableFragment fragment = new SearchableFragment();
        fragment.setArguments(extras);
        return fragment;
    }
    // endregion

    // region Lifecycle Methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null){
            mQuery = getArguments().getString(SearchManager.QUERY);
//            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
//                    CustomSearchRecentSuggestionsProvider.AUTHORITY, CustomSearchRecentSuggestionsProvider.MODE);
//            suggestions.saveRecentQuery(mQuery, null);

            RealmUtility.saveQuery(mQuery);

//            performSearch(query);
        }

        AccessToken token = PreferencesHelper.getAccessToken(getActivity());
        mVimeoService = ServiceGenerator.createService(
                VimeoService.class,
                VimeoService.BASE_URL,
                token);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_searchable, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
//
//        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
//        ab.setDisplayHomeAsUpEnabled(true);
//        ab.setTitle("");

        setupSearchView();

//        mSearchViewWidget.setQuery(mQuery, false);
//
//        mSearchViewWidget.setOnQueryTextListener(new SearchViewWidget2.OnQueryTextListener() {
//
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                Intent intent = new Intent(mSearchViewWidget.getContext(), SearchableActivity.class);
//                intent.setAction(Intent.ACTION_SEARCH);
//                intent.putExtra(SearchManager.QUERY, query);
//                mSearchViewWidget.getContext().startActivity(intent);
//
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return false;
//            }
//        });
//
//        mSearchViewWidget.setOnSearchViewListener(new SearchViewWidget2.SearchViewListener() {
//
//            @Override
//            public void onSearchViewShown() {
//            }
//
//            @Override
//            public void onSearchViewClosed() {
//            }
//        });
//
//        List<SearchViewItem> mSuggestionsList = new ArrayList<>();
//        mSuggestionsList.add(new SearchViewItem(R.drawable.ic_search_black_24dp, "Wi-Fi"));
//        mSuggestionsList.add(new SearchViewItem(R.drawable.ic_search_black_24dp, "Bluetooth"));
//        mSuggestionsList.add(new SearchViewItem(R.drawable.ic_search_black_24dp, "GPS"));
//        mSuggestionsList.add(new SearchViewItem(R.drawable.ic_search_black_24dp, "Ad-Hoc"));
//        mSuggestionsList.add(new SearchViewItem(R.drawable.ic_search_black_24dp, "Google"));
//        mSuggestionsList.add(new SearchViewItem(R.drawable.ic_search_black_24dp, "Android"));
//        mSuggestionsList.add(new SearchViewItem(R.drawable.ic_search_black_24dp, "Piconet"));
//        mSuggestionsList.add(new SearchViewItem(R.drawable.ic_search_black_24dp, "Scatternet"));
//
//        List<SearchViewItem> mResultsList = new ArrayList<>();
//        // choose true for Light Theme, false for Dark Theme.
//        SearchViewAdapter mSearchViewAdapter = new SearchViewAdapter(getActivity(), mResultsList, mSuggestionsList, true);
//        mSearchViewAdapter.setOnItemClickListener(new SearchViewAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//                TextView mText = (TextView) view.findViewById(R.id.textView_result);
//                CharSequence text = "Hello toast!";
////                int duration = Toast.LENGTH_SHORT;
////                Toast toast = Toast.makeText(getApplicationContext(), text, duration);
////                toast.show();
//
//            }
//        });
//        mSearchViewWidget.setAdapter(mSearchViewAdapter);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mVideosRecyclerView.setLayoutManager(mLayoutManager);
        mVideosAdapter = new VideosAdapter();
        mVideosAdapter.setOnItemClickListener(this);

        mVideosRecyclerView.setItemAnimator(new SlideInUpAnimator());
        mVideosRecyclerView.setAdapter(mVideosAdapter);

        // Pagination
        mVideosRecyclerView.addOnScrollListener(mRecyclerViewOnScrollListener);

        Call findVideosCall = mVimeoService.findVideos(mQuery,
                mSortByValue,
                mSortOrderValue,
                mCurrentPage,
                PAGE_SIZE);
        mCalls.add(findVideosCall);
        findVideosCall.enqueue(mFindVideosFirstFetchCallback);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.get().register(this);

        setupSearchView();
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.get().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mVideosRecyclerView.removeOnScrollListener(mRecyclerViewOnScrollListener);
        ButterKnife.unbind(this);
    }
    // endregion

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

//        inflater.inflate(R.menu.searchable_menu, menu);

        // Get the SearchView and set the searchable configuration
//        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        // Assumes current activity is the searchable activity
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
//        searchView.setQueryRefinementEnabled(true);

//        searchView.onActionViewExpanded();
//        searchView.setIconified(false);
//        searchView.setQuery(mQuery, false);
//        searchView.clearFocus();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                // do nothing
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == Activity.RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
//                    mMaterialSearchView.setQuery(searchWrd);
                    launchSearchActivity(searchWrd);
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // region VideosAdapter.OnItemClickListener Methods
    @Override
    public void onItemClick(int position, View view) {
        Video video = mVideosAdapter.getItem(position);
        if (video != null) {
            Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);

            Bundle bundle = new Bundle();
            bundle.putParcelable("video", video);
            intent.putExtras(bundle);

            Pair<View, String> p1 = Pair.create(view.findViewById(R.id.video_thumbnail_iv), "videoTransition");
//                Pair<View, String> p2 = Pair.create((View) view.findViewById(R.id.title_tv), "titleTransition");
//                Pair<View, String> p3 = Pair.create((View) view.findViewById(R.id.subtitle_tv), "subtitleTransition");
//        Pair<View, String> p4 = Pair.create((View)view.findViewById(R.id.uploaded_tv), "uploadedTransition");

//                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
//                        p1, p2, p3);

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                    p1);

//            ActivityCompat.startActivity(getActivity(), intent, options.toBundle());

                startActivity(intent);
        }

    }
    // endregion

    // region Otto Methods
    @Subscribe
    public void onFilterClickedEvent(FilterClickedEvent event) {
        showSortDialog();
    }

    @Subscribe
    public void onSearchPerformedEvent(SearchPerformedEvent event) {
        String query = event.getQuery();
        if (!TextUtils.isEmpty(query)) {
            launchSearchActivity(query);
        }
    }

    @Subscribe
    public void onShowSearchSuggestionsEvent(ShowSearchSuggestionsEvent event) {
        String query = event.getQuery();

        mMaterialSearchView.addSuggestions(RealmUtility.getSuggestions(query));
    }
    // endregion

    // region Helper Methods
    private void loadMoreItems() {
        mIsLoading = true;

        mCurrentPage += 1;

        Call findVideosCall = mVimeoService.findVideos(mQuery,
                mSortByValue,
                mSortOrderValue,
                mCurrentPage,
                PAGE_SIZE);
        mCalls.add(findVideosCall);
        findVideosCall.enqueue(mFindVideosNextFetchCallback);
    }

    private void showSortDialog() {
        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptsView = li.inflate(R.layout.sort_dialog, null);
        final Spinner sortBySpinner = (Spinner) promptsView.findViewById(R.id.sort_by_s);
        final Spinner sortOrderSpinner = (Spinner) promptsView.findViewById(R.id.sort_order_s);

        String[] mSortByKeysArray = getResources().getStringArray(R.array.videos_sort_by_keys);
        ArrayAdapter<String> sortByAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mSortByKeysArray);
        sortBySpinner.setAdapter(sortByAdapter);

        String[] mSortOrderKeysArray = getResources().getStringArray(R.array.videos_sort_order_keys);
        ArrayAdapter<String> sortOrderAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mSortOrderKeysArray);
        sortOrderSpinner.setAdapter(sortOrderAdapter);

        sortBySpinner.setSelection(mSelectedSortByKey);
        sortOrderSpinner.setSelection(mSelectedSortOrderKey);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSelectedSortByKey = sortBySpinner.getSelectedItemPosition();
                mSelectedSortOrderKey = sortOrderSpinner.getSelectedItemPosition();

                String[] sortByValues = getResources().getStringArray(R.array.videos_sort_by_values);
                mSortByValue = sortByValues[mSelectedSortByKey];

                String[] sortOrderValues = getResources().getStringArray(R.array.videos_sort_order_values);
                mSortOrderValue = sortOrderValues[mSelectedSortOrderKey];

                mVideosAdapter.clear();

                mLoadingImageView.setVisibility(View.VISIBLE);

                mCurrentPage = 1;

                mMaterialSearchView.disableFilter();

                Call findVideosCall = mVimeoService.findVideos(mQuery,
                        mSortByValue,
                        mSortOrderValue,
                        mCurrentPage,
                        PAGE_SIZE);
                mCalls.add(findVideosCall);
                findVideosCall.enqueue(mFindVideosFirstFetchCallback);

                dialog.dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialogBuilder.show();
    }

    private void launchSearchActivity(String query){
        Intent intent = new Intent(getContext(), SearchableActivity.class);
        intent.setAction(Intent.ACTION_SEARCH);
        intent.putExtra(SearchManager.QUERY, query);
        getContext().startActivity(intent);
        getActivity().overridePendingTransition(0, 0);
    }

    private void setupSearchView(){
        mMaterialSearchView.setQuery(mQuery);
    }

    private void showReloadSnackbar(String message){
        Snackbar.make(getActivity().findViewById(android.R.id.content),
                message,
                Snackbar.LENGTH_INDEFINITE)
                .setAction("Reload", mReloadOnClickListener)
//                                .setActionTextColor(Color.RED)
                .show();
    }
    // endregion
}
