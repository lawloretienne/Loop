package com.etiennelawlor.loop.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.activities.VideoDetailsActivity;
import com.etiennelawlor.loop.adapters.VideosAdapter;
import com.etiennelawlor.loop.models.AccessToken;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoService;
import com.etiennelawlor.loop.network.interceptors.AuthorizedNetworkInterceptor;
import com.etiennelawlor.loop.network.models.response.FeedItem;
import com.etiennelawlor.loop.network.models.response.FeedItemsEnvelope;
import com.etiennelawlor.loop.network.models.response.Video;
import com.etiennelawlor.loop.prefs.LoopPrefs;
import com.etiennelawlor.loop.ui.LoadingImageView;
import com.etiennelawlor.loop.utilities.NetworkLogUtility;
import com.etiennelawlor.loop.utilities.NetworkUtility;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class MyFeedFragment extends BaseFragment implements VideosAdapter.OnItemClickListener, VideosAdapter.OnReloadClickListener {

    // region Constants
    public static final int PAGE_SIZE = 30;
    public static final String KEY_TRANSITION = "KEY_TRANSITION";
    // endregion

    // region Views
    @Bind(R.id.rv)
    RecyclerView recyclerView;
    @Bind(android.R.id.empty)
    View emptyView;
    @Bind(R.id.loading_iv)
    LoadingImageView loadingImageView;
    @Bind(R.id.error_ll)
    LinearLayout errorLinearLayout;
    @Bind(R.id.error_tv)
    TextView errorTextView;
    @Bind(R.id.reload_btn)
    Button reloadButton;
    // endregion

    // region Member Variables
    private boolean isLastPage = false;
    private int currentPage = 1;
//    private int selectedSortByKey = 0;
//    private int selectedSortOrderKey = 1;
    private boolean isLoading = false;
//    private String sortByValue = "relevant";
//    private String sortOrderValue = "desc";
//    private String filter;
    private VideosAdapter videosAdapter;
//    private String query;
    private LinearLayoutManager layoutManager;
    private VimeoService vimeoService;
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
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= PAGE_SIZE) {
                    loadMoreItems();
                }
            }
        }
    };

    @OnClick(R.id.reload_btn)
    public void onReloadButtonClicked() {
        errorLinearLayout.setVisibility(View.GONE);
        loadingImageView.setVisibility(View.VISIBLE);

        Call findMyFeedVideosCall = vimeoService.findMyFeedVideos(currentPage, PAGE_SIZE);
        calls.add(findMyFeedVideosCall);
        findMyFeedVideosCall.enqueue(findMyFeedVideosFirstFetchCallback);
    }
    // endregion

    // region Callbacks
    private Callback<FeedItemsEnvelope> findMyFeedVideosFirstFetchCallback = new Callback<FeedItemsEnvelope>() {
        @Override
        public void onResponse(Call<FeedItemsEnvelope> call, Response<FeedItemsEnvelope> response) {
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

            FeedItemsEnvelope feedItemsEnvelope = response.body();
            if (feedItemsEnvelope != null) {
                List<FeedItem> feedItems = feedItemsEnvelope.getFeedItems();
                if (feedItems != null) {
                    List<Video> videos = new ArrayList<>();
                    for(FeedItem feedItem : feedItems){
                        Video video = feedItem.getClip();
                        videos.add(video);
                    }

                    if(videos.size()>0)
                        videosAdapter.addAll(videos);

                    if (videos.size() >= PAGE_SIZE) {
                        videosAdapter.addFooter();
                    } else {
                        isLastPage = true;
                    }
                }
            }
        }

        @Override
        public void onFailure(Call<FeedItemsEnvelope> call, Throwable t) {
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

    private Callback<FeedItemsEnvelope> findMyFeedVideosNextFetchCallback = new Callback<FeedItemsEnvelope>() {
        @Override
        public void onResponse(Call<FeedItemsEnvelope> call, Response<FeedItemsEnvelope> response) {
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

            FeedItemsEnvelope feedItemsEnvelope = response.body();
            if (feedItemsEnvelope != null) {
                List<FeedItem> feedItems = feedItemsEnvelope.getFeedItems();
                if (feedItems != null) {
                    List<Video> videos = new ArrayList<>();
                    for(FeedItem feedItem : feedItems){
                        Video video = feedItem.getClip();
                        videos.add(video);
                    }

                    if(videos.size()>0)
                        videosAdapter.addAll(videos);

                    if (videos.size() >= PAGE_SIZE) {
                        videosAdapter.addFooter();
                    } else {
                        isLastPage = true;
                    }
                }
            }
        }

        @Override
        public void onFailure(Call<FeedItemsEnvelope> call, Throwable t) {
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
    public MyFeedFragment() {
    }
    // endregion

    // region Factory Methods
    public static MyFeedFragment newInstance() {
        return new MyFeedFragment();
    }

    public static MyFeedFragment newInstance(Bundle extras) {
        MyFeedFragment fragment = new MyFeedFragment();
        fragment.setArguments(extras);
        return fragment;
    }
    // endregion

    // region Lifecycle Methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
//            query = getArguments().getString(WatchNowFragment.KEY_QUERY);
//            filter = getArguments().getString(WatchNowFragment.KEY_FILTER);
        }

        AccessToken token = LoopPrefs.getAccessToken(getActivity());
        vimeoService = ServiceGenerator.createService(
                VimeoService.class,
                VimeoService.BASE_URL,
                new AuthorizedNetworkInterceptor(token));

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_videos, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(mQuery);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        videosAdapter = new VideosAdapter();
        videosAdapter.setOnItemClickListener(this);
        videosAdapter.setOnReloadClickListener(this);

        recyclerView.setItemAnimator(new SlideInUpAnimator());
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(videosAdapter);

        // Pagination
        recyclerView.addOnScrollListener(recyclerViewOnScrollListener);

        Call findMyFeedVideosCall = vimeoService.findMyFeedVideos(currentPage, PAGE_SIZE);
        calls.add(findMyFeedVideosCall);
        findMyFeedVideosCall.enqueue(findMyFeedVideosFirstFetchCallback);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeListeners();
        currentPage = 1;
        ButterKnife.unbind(this);
    }
    // endregion

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

//        inflater.inflate(R.menu.videos_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.sort:
//                showSortDialog();
//                break;
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
            ImageView iv = (ImageView)view.findViewById(R.id.video_thumbnail_iv);

//            intent.putExtra("TRANSITION_KEY", ViewCompat.getTransitionName(iv));
            bundle.putString(KEY_TRANSITION, ViewCompat.getTransitionName(iv));

            intent.putExtras(bundle);

//            Pair<View, String> p1 = Pair.create(view.findViewById(R.id.video_thumbnail_iv), "videoTransition");

//                Pair<View, String> p2 = Pair.create((View) view.findViewById(R.id.title_tv), "titleTransition");
//                Pair<View, String> p3 = Pair.create((View) view.findViewById(R.id.subtitle_tv), "subtitleTransition");
//        Pair<View, String> p4 = Pair.create((View)view.findViewById(R.id.uploaded_tv), "uploadedTransition");

//            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
//                    p1);

//                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
//                        p1, p2, p3);


//            ImageView iv = (ImageView) view.findViewById(R.id.video_thumbnail_iv);

//            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
//                    iv, "videoTransition");

//            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                    getActivity(), iv, ViewCompat.getTransitionName(iv));


//            ActivityCompat.startActivity(getActivity(), intent, options.toBundle());

            startActivity(intent);
        }

    }
    // endregion

    // region VideosAdapter.OnReloadClickListener Methods

    @Override
    public void onReloadClick() {
        videosAdapter.updateFooter(VideosAdapter.FooterType.LOAD_MORE);

        Call findMyFeedVideosCall = vimeoService.findMyFeedVideos(currentPage, PAGE_SIZE);
        calls.add(findMyFeedVideosCall);
        findMyFeedVideosCall.enqueue(findMyFeedVideosNextFetchCallback);
    }

    // endregion

    // region Helper Methods
    private void loadMoreItems() {
        isLoading = true;

        currentPage += 1;

        Call findMyFeedVideosCall = vimeoService.findMyFeedVideos(currentPage, PAGE_SIZE);
        calls.add(findMyFeedVideosCall);
        findMyFeedVideosCall.enqueue(findMyFeedVideosNextFetchCallback);
    }

//    private void showSortDialog() {
//        LayoutInflater li = LayoutInflater.from(getActivity());
//        View promptsView = li.inflate(R.layout.sort_dialog, null);
//        final Spinner sortBySpinner = (Spinner) promptsView.findViewById(R.id.sort_by_s);
//        final Spinner sortOrderSpinner = (Spinner) promptsView.findViewById(R.id.sort_order_s);
//
//        String[] mSortByKeysArray = getResources().getStringArray(R.array.videos_sort_by_keys);
//        ArrayAdapter<String> sortByAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mSortByKeysArray);
//        sortBySpinner.setAdapter(sortByAdapter);
//
//        String[] mSortOrderKeysArray = getResources().getStringArray(R.array.videos_sort_order_keys);
//        ArrayAdapter<String> sortOrderAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mSortOrderKeysArray);
//        sortOrderSpinner.setAdapter(sortOrderAdapter);
//
//        sortBySpinner.setSelection(selectedSortByKey);
//        sortOrderSpinner.setSelection(selectedSortOrderKey);
//
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
//        alertDialogBuilder.setView(promptsView);
//        alertDialogBuilder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                selectedSortByKey = sortBySpinner.getSelectedItemPosition();
//                selectedSortOrderKey = sortOrderSpinner.getSelectedItemPosition();
//
//                String[] sortByValues = getResources().getStringArray(R.array.videos_sort_by_values);
//                sortByValue = sortByValues[selectedSortByKey];
//
//                String[] sortOrderValues = getResources().getStringArray(R.array.videos_sort_order_values);
//                sortOrderValue = sortOrderValues[selectedSortOrderKey];
//
//                videosAdapter.clear();
//
//                loadingImageView.setVisibility(View.VISIBLE);
//
//                currentPage = 1;
//
//                Call findVideosCall = vimeoService.findVideos(query,
//                        sortByValue,
//                        sortOrderValue,
//                        currentPage,
//                        PAGE_SIZE,
//                        filter);
//                calls.add(findVideosCall);
//                findVideosCall.enqueue(findVideosFirstFetchCallback);
//
//                dialog.dismiss();
//            }
//        });
//        alertDialogBuilder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//
//        alertDialogBuilder.show();
//    }

//    public String getQuery() {
//        return query;
//    }

    private void removeListeners(){
        recyclerView.removeOnScrollListener(recyclerViewOnScrollListener);
    }
    // endregion
}
