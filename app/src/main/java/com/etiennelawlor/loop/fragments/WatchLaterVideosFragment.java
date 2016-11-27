package com.etiennelawlor.loop.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.etiennelawlor.loop.activities.VideoDetailsActivity;
import com.etiennelawlor.loop.adapters.VideosAdapter;
import com.etiennelawlor.loop.models.AccessToken;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoService;
import com.etiennelawlor.loop.network.models.response.Video;
import com.etiennelawlor.loop.network.models.response.VideosCollection;
import com.etiennelawlor.loop.otto.BusProvider;
import com.etiennelawlor.loop.otto.events.WatchLaterEvent;
import com.etiennelawlor.loop.prefs.LoopPrefs;
import com.etiennelawlor.loop.ui.LoadingImageView;
import com.etiennelawlor.loop.utilities.FontCache;
import com.etiennelawlor.loop.utilities.LogUtility;
import com.etiennelawlor.loop.utilities.TrestleUtility;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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
public class WatchLaterVideosFragment extends BaseFragment implements VideosAdapter.OnItemClickListener {

    // region Constants
    public static final int PAGE_SIZE = 30;
    // endregion

    // region Views
    @Bind(R.id.videos_rv)
    RecyclerView videosRecyclerView;
    @Bind(android.R.id.empty)
    View emptyView;
    @Bind(R.id.empty_tv)
    TextView emptyTextView;
    @Bind(R.id.loading_iv)
    LoadingImageView loadingImageView;
    @Bind(R.id.error_ll)
    LinearLayout errorLinearLayout;
    @Bind(R.id.error_tv)
    TextView errorTextView;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    // endregion

    // region Member Variables
    private boolean isLastPage = false;
    private int currentPage = 1;
    private int selectedSortByKey = 0;
    private int selectedSortOrderKey = 1;
    private boolean isLoading = false;
    private String sortByValue = "date";
    private String sortOrderValue = "desc";
    private VideosAdapter videosAdapter;
    private String query;
    private LinearLayoutManager layoutManager;
    private VimeoService vimeoService;
    private WatchLaterEvent watchLaterEvent;
    private Typeface font;
    // endregion

    // region Listeners
    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

            if (!isLoading && !isLastPage) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
                    loadMoreItems();
                }
            }
        }
    };

    private View.OnClickListener reloadOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            currentPage -= 1;
            videosAdapter.addLoading();
            loadMoreItems();
        }
    };

    @OnClick(R.id.reload_btn)
    public void onReloadButtonClicked() {
        errorLinearLayout.setVisibility(View.GONE);
        loadingImageView.setVisibility(View.VISIBLE);

        Call findWatchLaterVideosCall = vimeoService.findWatchLaterVideos(query,
                sortByValue,
                sortOrderValue,
                currentPage,
                PAGE_SIZE);
        calls.add(findWatchLaterVideosCall);
        findWatchLaterVideosCall.enqueue(findVideosFirstFetchCallback);
    }
    // endregion

    // region Callbacks
    private Callback<VideosCollection> findVideosFirstFetchCallback = new Callback<VideosCollection>() {
        @Override
        public void onResponse(Response<VideosCollection> response, Retrofit retrofit) {
            Timber.d("onResponse()");
            loadingImageView.setVisibility(View.GONE);
            isLoading = false;

            if (response != null) {
                if(response.isSuccess()){
                    VideosCollection videosCollection = response.body();
                    if (videosCollection != null) {
                        List<Video> videos = videosCollection.getVideos();
                        if (videos != null) {
                            videosAdapter.addAll(videos);

                            if(videos.size() >= PAGE_SIZE){
                                videosAdapter.addLoading();
                            } else {
                                isLastPage = true;
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
                                errorTextView.setText("Can't load data.\nCheck your network connection.");
                                errorLinearLayout.setVisibility(View.VISIBLE);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }

            if (videosAdapter.isEmpty()) {
                emptyTextView.setText(getString(R.string.watch_later_empty_prompt));
                Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_watch_later_large);
                DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), R.color.grey_500));
                emptyTextView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
                emptyView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onFailure(Throwable t) {
            if (t != null) {
                String message = t.getMessage();
                LogUtility.logFailure(t);

                if (t instanceof SocketTimeoutException || t instanceof UnknownHostException) {
                    Timber.e("Timeout occurred");
                    isLoading = false;
                    loadingImageView.setVisibility(View.GONE);

                    errorTextView.setText("Can't load data.\nCheck your network connection.");
                    errorLinearLayout.setVisibility(View.VISIBLE);
                } else if(t instanceof IOException){
                    if(message.equals("Canceled")){
                        Timber.e("onFailure() : Canceled");
                    } else {
                        isLoading = false;
                        loadingImageView.setVisibility(View.GONE);
                    }
                }
            }
        }
    };

    private Callback<VideosCollection> findVideosNextFetchCallback = new Callback<VideosCollection>() {
        @Override
        public void onResponse(Response<VideosCollection> response, Retrofit retrofit) {
            Timber.d("onResponse()");
            videosAdapter.removeLoading();
            isLoading = false;

            if (response != null) {
                if(response.isSuccess()){
                    VideosCollection videosCollection = response.body();
                    if (videosCollection != null) {
                        List<Video> videos = videosCollection.getVideos();
                        if (videos != null) {
                            videosAdapter.addAll(videos);

                            if(videos.size() >= PAGE_SIZE){
                                videosAdapter.addLoading();
                            } else {
                                isLastPage = true;
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
            videosAdapter.removeLoading();
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
    public WatchLaterVideosFragment() {
    }
    // endregion

    // region Factory Methods
    public static WatchLaterVideosFragment newInstance() {
        return new WatchLaterVideosFragment();
    }

    public static WatchLaterVideosFragment newInstance(Bundle extras) {
        WatchLaterVideosFragment fragment = new WatchLaterVideosFragment();
        fragment.setArguments(extras);
        return fragment;
    }
    // endregion

    // region Lifecycle Methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            query = getArguments().getString("query");
        }

        AccessToken token = LoopPrefs.getAccessToken(getActivity());
        vimeoService = ServiceGenerator.createService(
                VimeoService.class,
                VimeoService.BASE_URL,
                token);

        setHasOptionsMenu(true);
        BusProvider.getInstance().register(this);

        font = FontCache.getTypeface("Ubuntu-Medium.ttf", getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_watch_later_videos, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(ab != null){
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(TrestleUtility.getFormattedText(getString(R.string.watch_later), font));
        }

        layoutManager = new LinearLayoutManager(getActivity());
        videosRecyclerView.setLayoutManager(layoutManager);
        videosAdapter = new VideosAdapter();
        videosAdapter.setOnItemClickListener(this);

        videosRecyclerView.setItemAnimator(new SlideInUpAnimator());
        videosRecyclerView.setAdapter(videosAdapter);

        // Pagination
        videosRecyclerView.addOnScrollListener(recyclerViewOnScrollListener);

        Call findWatchLaterVideosCall = vimeoService.findWatchLaterVideos(query,
                sortByValue,
                sortOrderValue,
                currentPage,
                PAGE_SIZE);
        calls.add(findWatchLaterVideosCall);
        findWatchLaterVideosCall.enqueue(findVideosFirstFetchCallback);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(watchLaterEvent != null){
            refreshAdapter();
            watchLaterEvent = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeListeners();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister Otto Bus
        BusProvider.getInstance().unregister(this);
    }
    // endregion

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.main_menu, menu);

        // Get the SearchView and set the searchable configuration
//        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
//
//        // Assumes current activity is the searchable activity
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
//        searchView.setQueryRefinementEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort:
                showSortDialog();
                break;
            default:
                // do nothing
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // region VideosAdapter.OnItemClickListener Methods
    @Override
    public void onItemClick(int position, View view) {
        Video video = videosAdapter.getItem(position);
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
    public void onWatchLater(WatchLaterEvent event) {
        Timber.d("onWatchLaterEvent");

        if (isResumed()) {
            refreshAdapter();
            watchLaterEvent = null;
        } else {
            watchLaterEvent = event;
        }
    }
    // endregion

    // region Helper Methods
    private void loadMoreItems() {
        isLoading = true;

        currentPage += 1;

        Call findWatchLaterVideosCall = vimeoService.findWatchLaterVideos(query,
                sortByValue,
                sortOrderValue,
                currentPage,
                PAGE_SIZE);
        calls.add(findWatchLaterVideosCall);
        findWatchLaterVideosCall.enqueue(findVideosNextFetchCallback);
    }

    private void showSortDialog() {
        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptsView = li.inflate(R.layout.sort_dialog, null);
        final Spinner sortBySpinner = (Spinner) promptsView.findViewById(R.id.sort_by_s);
        final Spinner sortOrderSpinner = (Spinner) promptsView.findViewById(R.id.sort_order_s);

        String[] mSortByKeysArray = getResources().getStringArray(R.array.watchlater_sort_by_keys);
        ArrayAdapter<String> sortByAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mSortByKeysArray);
        sortBySpinner.setAdapter(sortByAdapter);

        String[] mSortOrderKeysArray = getResources().getStringArray(R.array.watchlater_sort_order_keys);
        ArrayAdapter<String> sortOrderAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mSortOrderKeysArray);
        sortOrderSpinner.setAdapter(sortOrderAdapter);

        sortBySpinner.setSelection(selectedSortByKey);
        sortOrderSpinner.setSelection(selectedSortOrderKey);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedSortByKey = sortBySpinner.getSelectedItemPosition();
                selectedSortOrderKey = sortOrderSpinner.getSelectedItemPosition();

                String[] sortByValues = getResources().getStringArray(R.array.watchlater_sort_by_values);
                sortByValue = sortByValues[selectedSortByKey];

                String[] sortOrderValues = getResources().getStringArray(R.array.watchlater_sort_order_values);
                sortOrderValue = sortOrderValues[selectedSortOrderKey];

                videosAdapter.clear();

                loadingImageView.setVisibility(View.VISIBLE);

                currentPage = 1;

                Call findWatchLaterVideosCall = vimeoService.findWatchLaterVideos(query,
                        sortByValue,
                        sortOrderValue,
                        currentPage,
                        PAGE_SIZE);
                calls.add(findWatchLaterVideosCall);
                findWatchLaterVideosCall.enqueue(findVideosFirstFetchCallback);

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

    private void refreshAdapter(){
        videosAdapter.clear();

        loadingImageView.setVisibility(View.VISIBLE);

        currentPage = 1;

        Call findWatchLaterVideosCall = vimeoService.findWatchLaterVideos(query,
                sortByValue,
                sortOrderValue,
                currentPage,
                PAGE_SIZE);
        calls.add(findWatchLaterVideosCall);
        findWatchLaterVideosCall.enqueue(findVideosFirstFetchCallback);
    }

    private void showReloadSnackbar(String message){
        Snackbar.make(getActivity().findViewById(android.R.id.content),
                message,
                Snackbar.LENGTH_INDEFINITE)
                .setAction("Reload", reloadOnClickListener)
//                                .setActionTextColor(Color.RED)
                .show();
    }

    private void removeListeners(){
        videosRecyclerView.removeOnScrollListener(recyclerViewOnScrollListener);
    }
    // endregion
}
