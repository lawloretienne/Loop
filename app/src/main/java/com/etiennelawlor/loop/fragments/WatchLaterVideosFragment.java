package com.etiennelawlor.loop.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
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
import com.etiennelawlor.loop.bus.RxBus;
import com.etiennelawlor.loop.bus.events.WatchLaterClickedEvent;
import com.etiennelawlor.loop.models.AccessToken;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoService;
import com.etiennelawlor.loop.network.interceptors.AuthorizedNetworkInterceptor;
import com.etiennelawlor.loop.network.models.response.Video;
import com.etiennelawlor.loop.network.models.response.VideosEnvelope;
import com.etiennelawlor.loop.prefs.LoopPrefs;
import com.etiennelawlor.loop.ui.LoadingImageView;
import com.etiennelawlor.loop.utilities.FontCache;
import com.etiennelawlor.loop.utilities.NetworkLogUtility;
import com.etiennelawlor.loop.utilities.NetworkUtility;
import com.etiennelawlor.loop.utilities.TrestleUtility;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class WatchLaterVideosFragment extends BaseFragment implements VideosAdapter.OnItemClickListener, VideosAdapter.OnReloadClickListener {

    // Cast SDK v3 Guide
    // https://codelabs.developers.google.com/codelabs/cast-videos-android/#0

    // region Constants
    public static final int PAGE_SIZE = 30;
    // endregion

    // region Views
    @BindView(R.id.rv)
    RecyclerView recyclerView;
    @BindView(android.R.id.empty)
    View emptyView;
    @BindView(R.id.empty_tv)
    TextView emptyTextView;
    @BindView(R.id.loading_iv)
    LoadingImageView loadingImageView;
    @BindView(R.id.error_ll)
    LinearLayout errorLinearLayout;
    @BindView(R.id.error_tv)
    TextView errorTextView;
    @BindView(R.id.toolbar)
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
    private String query = "";
    private Unbinder unbinder;
    private LinearLayoutManager layoutManager;
    private VimeoService vimeoService;
    private Typeface font;
    private CompositeSubscription compositeSubscription;
    private CastContext castContext;
    private MenuItem mediaRouteMenuItem;
    private CastStateListener castStateListener;
    private IntroductoryOverlay introductoryOverlay;
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

    @OnClick(R.id.reload_btn)
    public void onReloadButtonClicked() {
        errorLinearLayout.setVisibility(View.GONE);
        loadingImageView.setVisibility(View.VISIBLE);

        Call findWatchLaterVideosCall = vimeoService.findWatchLaterVideos(null,
                sortByValue,
                sortOrderValue,
                currentPage,
                PAGE_SIZE);
        calls.add(findWatchLaterVideosCall);
        findWatchLaterVideosCall.enqueue(findVideosFirstFetchCallback);
    }
    // endregion

    // region Callbacks
    private Callback<VideosEnvelope> findVideosFirstFetchCallback = new Callback<VideosEnvelope>() {
        @Override
        public void onResponse(Call<VideosEnvelope> call, Response<VideosEnvelope> response) {
            loadingImageView.setVisibility(View.GONE);
            isLoading = false;

            if (!response.isSuccessful()) {
                int responseCode = response.code();
                if(responseCode == 504) { // 504 Unsatisfiable Request (only-if-cached)
                    errorTextView.setText("Can't load data.\nCheck your network connection.");
                    errorLinearLayout.setVisibility(View.VISIBLE);
                }
                return;
            }

            VideosEnvelope videosEnvelope = response.body();
            if (videosEnvelope != null) {
                List<Video> videos = videosEnvelope.getVideos();
                if (videos != null) {
                    if(videos.size()>0)
                        videosAdapter.addAll(videos);

                    if(videos.size() >= PAGE_SIZE){
                        videosAdapter.addFooter();
                    } else {
                        isLastPage = true;
                    }
                }
            }

            if (videosAdapter.isEmpty()) {
                emptyTextView.setText(getString(R.string.watch_later_empty_prompt));
//                Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_watch_later_large);
//                DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), R.color.grey_500));
//                emptyTextView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
                emptyView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onFailure(Call<VideosEnvelope> call, Throwable t) {
            NetworkLogUtility.logFailure(call, t);

            if (!call.isCanceled()){
                isLoading = false;
                loadingImageView.setVisibility(View.GONE);

                if(NetworkUtility.isKnownException(t)){
                    errorTextView.setText("Can't load data.\nCheck your network connection.");
                    errorLinearLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    private Callback<VideosEnvelope> findVideosNextFetchCallback = new Callback<VideosEnvelope>() {
        @Override
        public void onResponse(Call<VideosEnvelope> call, Response<VideosEnvelope> response) {
            videosAdapter.removeFooter();
            isLoading = false;

            if (!response.isSuccessful()) {
                int responseCode = response.code();
                switch (responseCode){
                    case 504: // 504 Unsatisfiable Request (only-if-cached)
                        break;
                    case 400:
                        isLastPage = true;
                        break;
                }
                return;
            }

            VideosEnvelope videosEnvelope = response.body();
            if (videosEnvelope != null) {
                List<Video> videos = videosEnvelope.getVideos();
                if (videos != null) {
                    if(videos.size()>0)
                        videosAdapter.addAll(videos);

                    if(videos.size() >= PAGE_SIZE){
                        videosAdapter.addFooter();
                    } else {
                        isLastPage = true;
                    }
                }
            }
        }

        @Override
        public void onFailure(Call<VideosEnvelope> call, Throwable t) {
            NetworkLogUtility.logFailure(call, t);

            if (!call.isCanceled()){
                if(NetworkUtility.isKnownException(t)){
                    videosAdapter.updateFooter(VideosAdapter.FooterType.ERROR);
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

        AccessToken token = LoopPrefs.getAccessToken(getActivity());
        vimeoService = ServiceGenerator.createService(
                VimeoService.class,
                VimeoService.BASE_URL,
                new AuthorizedNetworkInterceptor(token));

        setHasOptionsMenu(true);

        font = FontCache.getTypeface("Ubuntu-Medium.ttf", getContext());
        compositeSubscription = new CompositeSubscription();

        castContext = CastContext.getSharedInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_watch_later_videos, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(ab != null){
            ab.setHomeAsUpIndicator(R.drawable.ic_menu_light);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(TrestleUtility.getFormattedText(getString(R.string.watch_later), font));
        }

        castStateListener = new CastStateListener() {
            @Override
            public void onCastStateChanged(int newState) {
                if (newState != CastState.NO_DEVICES_AVAILABLE) {
                    showIntroductoryOverlay();
                }
            }
        };

        setUpRxBusSubscription();

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        videosAdapter = new VideosAdapter();
        videosAdapter.setOnItemClickListener(this);
        videosAdapter.setOnReloadClickListener(this);

        recyclerView.setItemAnimator(new SlideInUpAnimator());
        recyclerView.setAdapter(videosAdapter);

        // Pagination
        recyclerView.addOnScrollListener(recyclerViewOnScrollListener);

        Call findWatchLaterVideosCall = vimeoService.findWatchLaterVideos(null,
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
        castContext.addCastStateListener(castStateListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        castContext.removeCastStateListener(castStateListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeListeners();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        compositeSubscription.unsubscribe();
    }
    // endregion

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.videos_menu, menu);

        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getContext().getApplicationContext(), menu, R.id.media_route);

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
            bundle.putParcelable(LikedVideosFragment.KEY_VIDEO, video);
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

    // region VideosAdapter.OnReloadClickListener Methods

    @Override
    public void onReloadClick() {
        videosAdapter.updateFooter(VideosAdapter.FooterType.LOAD_MORE);

        Call findWatchLaterVideosCall = vimeoService.findWatchLaterVideos(null,
                sortByValue,
                sortOrderValue,
                currentPage,
                PAGE_SIZE);
        calls.add(findWatchLaterVideosCall);
        findWatchLaterVideosCall.enqueue(findVideosNextFetchCallback);
    }

    // endregion

    // region Helper Methods
    private void loadMoreItems() {
        isLoading = true;

        currentPage += 1;

        Call findWatchLaterVideosCall = vimeoService.findWatchLaterVideos(null,
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

                Call findWatchLaterVideosCall = vimeoService.findWatchLaterVideos(null,
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

        Call findWatchLaterVideosCall = vimeoService.findWatchLaterVideos(null,
                sortByValue,
                sortOrderValue,
                currentPage,
                PAGE_SIZE);
        calls.add(findWatchLaterVideosCall);
        findWatchLaterVideosCall.enqueue(findVideosFirstFetchCallback);
    }

    private void removeListeners(){
        recyclerView.removeOnScrollListener(recyclerViewOnScrollListener);
    }

    private void setUpRxBusSubscription(){
        Subscription rxBusSubscription = RxBus.getInstance().toObserverable()
                .observeOn(AndroidSchedulers.mainThread()) // UI Thread
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
//                        if (event == null || !isResumed()) {
//                            return;
//                        }

                        if (event == null) {
                            return;
                        }

                        if(event instanceof WatchLaterClickedEvent) {
                            refreshAdapter();
                        }
                    }
                });

        compositeSubscription.add(rxBusSubscription);
    }

    private void showIntroductoryOverlay() {
        if (introductoryOverlay != null) {
            introductoryOverlay.remove();
        }
        if ((mediaRouteMenuItem != null) && mediaRouteMenuItem.isVisible()) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    introductoryOverlay = new IntroductoryOverlay.Builder(
                            getActivity(), mediaRouteMenuItem)
                            .setTitleText("Touch to cast videos to your TV")
                            .setSingleTime()
                            .setOnOverlayDismissedListener(
                                    new IntroductoryOverlay.OnOverlayDismissedListener() {
                                        @Override
                                        public void onOverlayDismissed() {
                                            introductoryOverlay = null;
                                        }
                                    })
                            .build();
                    introductoryOverlay.show();
                }
            });
        }
    }
    // endregion
}
