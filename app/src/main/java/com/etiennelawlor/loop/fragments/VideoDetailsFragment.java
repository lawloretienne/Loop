package com.etiennelawlor.loop.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.activities.VideoDetailsActivity;
import com.etiennelawlor.loop.activities.VideoPlayerActivity;
import com.etiennelawlor.loop.adapters.RelatedVideosAdapter;
import com.etiennelawlor.loop.helper.PreferencesHelper;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoService;
import com.etiennelawlor.loop.network.models.AccessToken;
import com.etiennelawlor.loop.network.models.Pictures;
import com.etiennelawlor.loop.network.models.Size;
import com.etiennelawlor.loop.network.models.Video;
import com.etiennelawlor.loop.network.models.VideosCollection;
import com.etiennelawlor.loop.otto.BusProvider;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class VideoDetailsFragment extends BaseFragment implements RelatedVideosAdapter.OnItemClickListener {

    // region Constants
    public static final int PAGE_SIZE = 30;
    // endregion

    // region Member Variables

    @Bind(R.id.video_thumbnail_iv)
    ImageView mVideoThumbnailImageView;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.videos_rv)
    RecyclerView mVideosRecyclerView;

    private Video mVideo;
    private RelatedVideosAdapter mRelatedVideosAdapter;
    private VimeoService mVimeoService;
    private LinearLayoutManager mLayoutManager;
    private Long mVideoId = -1L;
    private boolean mIsLastPage = false;
    private int mCurrentPage = 1;
    private boolean mIsLoading = false;
    // endregion

    // region Listeners
    @OnClick(R.id.play_fab)
    public void onPlayFABClicked(final View v) {
        Timber.d("onPlayFABClicked");
        if(mVideoId != -1L){
            Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);

            Bundle bundle = new Bundle();
            bundle.putLong("video_id", mVideoId);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

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
    // endregion

    // region Callbacks

    private Callback<VideosCollection> mGetRelatedVideosFirstFetchCallback = new Callback<VideosCollection>() {
        @Override
        public void onResponse(Response<VideosCollection> response) {

            if(response != null){
                if(response.isSuccess()){
                    VideosCollection videosCollection = response.body();
                    if (videosCollection != null) {
                        List<Video> videos = videosCollection.getVideos();
                        if (videos != null) {
                            mRelatedVideosAdapter.addAll(videos);

                            if(videos.size() >= PAGE_SIZE){
                                mRelatedVideosAdapter.addLoading();
                            } else {
                                mIsLastPage = true;
                            }
                        }
                    }
                } else {
                    ResponseBody responseBody = response.errorBody();
                    com.squareup.okhttp.Response rawResponse = response.raw();
                    if (rawResponse != null) {
                        String message = rawResponse.message();
                        int code = rawResponse.code();
                        Timber.d("onResponse() : message - " + message);
                        Timber.d("onResponse() : code - " + code);

                        switch (code) {
                            case 500:
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
            Timber.d("onFailure()");

            if (t != null) {
                Throwable cause = t.getCause();
                String message = t.getMessage();

                if (cause != null) {
                    Timber.e("failure() : cause.toString() -" + cause.toString());
                }

                if (TextUtils.isEmpty(message)) {
                    Timber.e("failure() : message - " + message);
                }

                t.printStackTrace();

                if (t instanceof SocketTimeoutException || t instanceof UnknownHostException) {
                    Timber.e("Timeout occurred");
//                    mIsLoading = false;
//                    mProgressBar.setVisibility(View.GONE);

//                    mErrorTextView.setText("Can't load data.\nCheck your network connection.");
//                    mErrorLinearLayout.setVisibility(View.VISIBLE);
                } else if(t instanceof IOException){
                    if(message.equals("Canceled")){
                        Timber.e("onFailure() : Canceled");
                    } else {
//                        mIsLoading = false;
//                        mProgressBar.setVisibility(View.GONE);
                    }
                }
            }
        }
    };

    private Callback<VideosCollection> mGetRelatedVideosNextFetchCallback = new Callback<VideosCollection>() {
        @Override
        public void onResponse(Response<VideosCollection> response) {

            mRelatedVideosAdapter.removeLoading();
            mIsLoading = false;

            if(response != null){
                if(response.isSuccess()){
                    VideosCollection videosCollection = response.body();
                    if (videosCollection != null) {
                        List<Video> videos = videosCollection.getVideos();
                        if (videos != null) {
                            mRelatedVideosAdapter.addAll(videos);
                            mRelatedVideosAdapter.addLoading();
                        }
                    }
                } else {
                    ResponseBody responseBody = response.errorBody();
                    com.squareup.okhttp.Response rawResponse = response.raw();
                    if (rawResponse != null) {
                        String message = rawResponse.message();
                        int code = rawResponse.code();
                        Timber.d("onResponse() : message - " + message);
                        Timber.d("onResponse() : code - " + code);

                        switch (code) {
                            case 500:
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
            Timber.d("onFailure()");

            mRelatedVideosAdapter.removeLoading();
            mIsLoading = false;

            if (t != null) {
                Throwable cause = t.getCause();
                String message = t.getMessage();

                if (cause != null) {
                    Timber.e("failure() : cause.toString() -" + cause.toString());
                }

                if (TextUtils.isEmpty(message)) {
                    Timber.e("failure() : message - " + message);
                }

                t.printStackTrace();

                if (t instanceof SocketTimeoutException || t instanceof UnknownHostException) {
                    Timber.e("Timeout occurred");
//                    mIsLoading = false;
//                    mProgressBar.setVisibility(View.GONE);

//                    mErrorTextView.setText("Can't load data.\nCheck your network connection.");
//                    mErrorLinearLayout.setVisibility(View.VISIBLE);
                } else if(t instanceof IOException){
                    if(message.equals("Canceled")){
                        Timber.e("onFailure() : Canceled");
                    } else {
//                        mIsLoading = false;
//                        mProgressBar.setVisibility(View.GONE);
                    }
                }
            }
        }
    };

    // endregion

    // region Constructors
    public static VideoDetailsFragment newInstance(Bundle extras) {
        VideoDetailsFragment fragment = new VideoDetailsFragment();
        fragment.setArguments(extras);
        return fragment;
    }

    public static VideoDetailsFragment newInstance() {
        VideoDetailsFragment fragment = new VideoDetailsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public VideoDetailsFragment() {
    }
    // endregion

    // region Lifecycle Methods
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BusProvider.get().register(this);

        if (getArguments() != null) {
            mVideo = (Video) getArguments().get("video");
        }

        AccessToken token = PreferencesHelper.getAccessToken(getActivity());
        mVimeoService = ServiceGenerator.createService(
                VimeoService.class,
                VimeoService.BASE_URL,
                token);

        Timber.d("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_video_details, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }

        if (mVideo != null) {
            setUpVideoThumbnail();

            String uri = mVideo.getUri();
            if (!TextUtils.isEmpty(uri)) {
                String lastPathSegment = Uri.parse(uri).getLastPathSegment();
                mVideoId = Long.parseLong(lastPathSegment);

                mLayoutManager = new LinearLayoutManager(getActivity());
                mVideosRecyclerView.setLayoutManager(mLayoutManager);
                mRelatedVideosAdapter = new RelatedVideosAdapter(mVideo);
                mRelatedVideosAdapter.setOnItemClickListener(this);
                mRelatedVideosAdapter.addHeader();
                mVideosRecyclerView.setAdapter(mRelatedVideosAdapter);

                // Pagination
                mVideosRecyclerView.addOnScrollListener(mRecyclerViewOnScrollListener);

                Call findRelatedVideosCall = mVimeoService.findRelatedVideos(mVideoId, mCurrentPage, PAGE_SIZE);
                mCalls.add(findRelatedVideosCall);
                findRelatedVideosCall.enqueue(mGetRelatedVideosFirstFetchCallback);
            }
        }

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    // endregion

    // region RelatedVideosAdapter.OnItemClickListener Methods
    @Override
    public void onItemClick(int position, View view) {
        Video video = mRelatedVideosAdapter.getItem(position);
        if (video != null) {
            Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);

            Bundle bundle = new Bundle();
            bundle.putParcelable("video", video);
            intent.putExtras(bundle);

            Pair<View, String> p1 = Pair.create((View) view.findViewById(R.id.video_thumbnail_iv), "videoTransition");
//                Pair<View, String> p2 = Pair.create((View) view.findViewById(R.id.title_tv), "titleTransition");
//                Pair<View, String> p3 = Pair.create((View) view.findViewById(R.id.subtitle_tv), "subtitleTransition");
//        Pair<View, String> p4 = Pair.create((View)view.findViewById(R.id.uploaded_tv), "uploadedTransition");

//                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
//                        p1, p2, p3);

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                    p1);


            ActivityCompat.startActivity(getActivity(), intent, options.toBundle());

//        startActivity(intent);
        }
    }
    // endregion

    // region Helper Methods

    private void setUpVideoThumbnail() {
        Pictures pictures = mVideo.getPictures();
        if (pictures != null) {
            List<Size> sizes = pictures.getSizes();
            if (sizes != null && sizes.size() > 0) {
                Size size = sizes.get(sizes.size() - 1);
                if (size != null) {
                    String link = size.getLink();
                    if (!TextUtils.isEmpty(link)) {
                        Glide.with(getActivity())
                                .load(link)
//                                .placeholder(R.drawable.ic_placeholder)
//                                .error(R.drawable.ic_error)
                                .into(mVideoThumbnailImageView);
                    }
                }
            }
        }
    }

    private void loadMoreItems() {
        mIsLoading = true;

        mCurrentPage += 1;

        Call findRelatedVideosCall = mVimeoService.findRelatedVideos(mVideoId, mCurrentPage, PAGE_SIZE);
        mCalls.add(findRelatedVideosCall);
        findRelatedVideosCall.enqueue(mGetRelatedVideosNextFetchCallback);
    }

//    private String formatViewCount(int viewCount) {
//        String formattedViewCount = "";
//
//        if (viewCount < 1000000000 && viewCount >= 1000000) {
//            formattedViewCount = String.format("%dM views", viewCount / 1000000);
//        } else if (viewCount < 1000000 && viewCount >= 1000) {
//            formattedViewCount = String.format("%dK views", viewCount / 1000);
//        } else if (viewCount < 1000 && viewCount > 1) {
//            formattedViewCount = String.format("%d views", viewCount);
//        } else if (viewCount == 1) {
//            formattedViewCount = String.format("%d view", viewCount);
//        }
//
//        return formattedViewCount;
//    }
    // endregion
}
