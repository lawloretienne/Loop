package com.etiennelawlor.loop.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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
import com.etiennelawlor.loop.network.models.response.Video;
import com.etiennelawlor.loop.network.models.response.VideosEnvelope;
import com.etiennelawlor.loop.prefs.LoopPrefs;
import com.etiennelawlor.loop.ui.LoadingImageView;
import com.etiennelawlor.loop.utilities.NetworkLogUtility;
import com.etiennelawlor.loop.utilities.NetworkUtility;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class RelatedVideosFragment extends BaseFragment implements VideosAdapter.OnItemClickListener, VideosAdapter.OnReloadClickListener {

    // region Constants
    public static final int PAGE_SIZE = 30;
    public static final String KEY_TRANSITION = "KEY_TRANSITION";
    // endregion

    // region Views
    @BindView(R.id.rv)
    RecyclerView recyclerView;
    @BindView(android.R.id.empty)
    View emptyView;
    @BindView(R.id.loading_iv)
    LoadingImageView loadingImageView;
    @BindView(R.id.error_ll)
    LinearLayout errorLinearLayout;
    @BindView(R.id.error_tv)
    TextView errorTextView;
    @BindView(R.id.reload_btn)
    Button reloadButton;
    // endregion

    // region Member Variables
    private boolean isLastPage = false;
    private int currentPage = 1;
    private boolean isLoading = false;
    private String sortByValue = "relevant";
    private String sortOrderValue = "desc";
    private Unbinder unbinder;
    private long videoId;
    private VideosAdapter videosAdapter;
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

        Call findRelatedVideosCall = vimeoService.findRelatedVideos(videoId, currentPage, PAGE_SIZE);
        calls.add(findRelatedVideosCall);
        findRelatedVideosCall.enqueue(findRelatedVideosFirstFetchCallback);
    }
    // endregion

    // region Callbacks
    private Callback<VideosEnvelope> findRelatedVideosFirstFetchCallback = new Callback<VideosEnvelope>() {
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

                    if (videos.size() >= PAGE_SIZE) {
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
                isLoading = false;
                loadingImageView.setVisibility(View.GONE);

                if(NetworkUtility.isKnownException(t)){
                    errorTextView.setText("Can't load data.\nCheck your network connection.");
                    errorLinearLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    private Callback<VideosEnvelope> findRelatedVideosNextFetchCallback = new Callback<VideosEnvelope>() {
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
    public RelatedVideosFragment() {
    }
    // endregion

    // region Factory Methods
    public static RelatedVideosFragment newInstance() {
        return new RelatedVideosFragment();
    }

    public static RelatedVideosFragment newInstance(Bundle extras) {
        RelatedVideosFragment fragment = new RelatedVideosFragment();
        fragment.setArguments(extras);
        return fragment;
    }
    // endregion

    // region Lifecycle Methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            videoId = getArguments().getLong(VideoDetailsFragment.KEY_VIDEO_ID);
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
        unbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        Call findRelatedVideosCall = vimeoService.findRelatedVideos(videoId, currentPage, PAGE_SIZE);
        calls.add(findRelatedVideosCall);
        findRelatedVideosCall.enqueue(findRelatedVideosFirstFetchCallback);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeListeners();
        currentPage = 1;
        unbinder.unbind();
    }
    // endregion

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
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);

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

        Call findRelatedVideosCall = vimeoService.findRelatedVideos(videoId, currentPage, PAGE_SIZE);
        calls.add(findRelatedVideosCall);
        findRelatedVideosCall.enqueue(findRelatedVideosNextFetchCallback);
    }

    // endregion

    // region Helper Methods
    private void loadMoreItems() {
        isLoading = true;

        currentPage += 1;

        Call findRelatedVideosCall = vimeoService.findRelatedVideos(videoId, currentPage, PAGE_SIZE);
        calls.add(findRelatedVideosCall);
        findRelatedVideosCall.enqueue(findRelatedVideosNextFetchCallback);
    }

    private void removeListeners(){
        recyclerView.removeOnScrollListener(recyclerViewOnScrollListener);
    }
    // endregion
}
