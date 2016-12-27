package com.etiennelawlor.loop.fragments;

import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.models.AccessToken;
import com.etiennelawlor.loop.models.VideoSavedState;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoPlayerService;
import com.etiennelawlor.loop.network.interceptors.AuthorizedNetworkInterceptor;
import com.etiennelawlor.loop.network.models.response.Files;
import com.etiennelawlor.loop.network.models.response.H264;
import com.etiennelawlor.loop.network.models.response.HLS;
import com.etiennelawlor.loop.network.models.response.ProgressiveData;
import com.etiennelawlor.loop.network.models.response.Request;
import com.etiennelawlor.loop.network.models.response.VP6;
import com.etiennelawlor.loop.network.models.response.VideoConfig;
import com.etiennelawlor.loop.network.models.response.VideoFormat;
import com.etiennelawlor.loop.prefs.LoopPrefs;
import com.etiennelawlor.loop.ui.LoadingImageView;
import com.etiennelawlor.loop.utilities.FontCache;
import com.etiennelawlor.loop.utilities.NetworkLogUtility;
import com.etiennelawlor.loop.utilities.NetworkUtility;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class VideoPlayerFragment extends BaseFragment {

    // region Constants
    // endregion

    // region Views
    @BindView(R.id.sepv)
    SimpleExoPlayerView simpleExoPlayerView;
    @BindView(R.id.loading_iv)
    LoadingImageView loadingImageView;
    @BindView(R.id.error_ll)
    LinearLayout errorLinearLayout;
    @BindView(R.id.error_tv)
    TextView errorTextView;
    @BindView(R.id.exo_replay)
    ImageButton replayImageButton;
    @BindView(R.id.exo_btns_fl)
    FrameLayout exoButtonsFrameLayout;
    @BindView(R.id.control_view_ll)
    LinearLayout controlViewLinearLayout;
    // endregion

    // region Member Variables
    private Long videoId;
    private String videoUrl;
    private VimeoPlayerService vimeoPlayerService;
    private VideoSavedState videoSavedState;
    private Typeface font;
    private Unbinder unbinder;
    private SimpleExoPlayer player;
    // endregion

    // region Listeners
    @OnClick(R.id.exo_replay)
    public void onReplayButtonClicked() {
        replayImageButton.setVisibility(View.GONE);
        exoButtonsFrameLayout.setVisibility(View.VISIBLE);
        player.seekTo(0);
    }

    @OnClick(R.id.reload_btn)
    public void onReloadButtonClicked() {
        errorLinearLayout.setVisibility(View.GONE);
        loadingImageView.setVisibility(View.VISIBLE);

        Call getVideoConfigCall = vimeoPlayerService.getVideoConfig(videoId);
        calls.add(getVideoConfigCall);
        getVideoConfigCall.enqueue(getVideoConfigCallback);
    }

    private ExoPlayer.EventListener exoPlayerEventListener = new ExoPlayer.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState){
                case ExoPlayer.STATE_BUFFERING:
                case ExoPlayer.STATE_READY:
                    replayImageButton.setVisibility(View.GONE);
                    exoButtonsFrameLayout.setVisibility(View.VISIBLE);
                    break;
                case ExoPlayer.STATE_ENDED:
                    exoButtonsFrameLayout.setVisibility(View.GONE);
                    replayImageButton.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {

        }

        @Override
        public void onPositionDiscontinuity() {

        }
    };

    private PlaybackControlView.VisibilityListener playbackControlViewVisibilityListener = new PlaybackControlView.VisibilityListener() {
        @Override
        public void onVisibilityChange(int visibility) {
            if(visibility == View.GONE){
                hideSystemUI();
            } else {
                showSystemUI();
            }
        }
    };
    // endregion

    // region Callbacks
    private Callback<VideoConfig> getVideoConfigCallback = new Callback<VideoConfig>() {
        @Override
        public void onResponse(Call<VideoConfig> call, Response<VideoConfig> response) {
            loadingImageView.setVisibility(View.GONE);

            if (!response.isSuccessful()) {

                int responseCode = response.code();
                switch (responseCode){
                    case 504: // 504 Unsatisfiable Request (only-if-cached)
                        errorTextView.setText("Can't load data.\nCheck your network connection.");
                        errorLinearLayout.setVisibility(View.VISIBLE);
                        break;
                    case 403: // Forbidden
                        // TODO show UI for "Cannot play this video"
//                        Snackbar.make(getActivity().findViewById(R.id.main_content),
//                                TrestleUtility.getFormattedText("Cannot play this video.", font, 16),
//                                Snackbar.LENGTH_LONG)
//                                .show();
                        break;
                }
                return;
            }

            VideoConfig videoConfig = response.body();
            if (videoConfig != null) {
                videoUrl = getVideoUrl(videoConfig);
                if (!TextUtils.isEmpty(videoUrl)) {

                    // Prepare the player with the source.
                    player.prepare(getMediaSource(videoUrl));
                }
            }
        }

        @Override
        public void onFailure(Call<VideoConfig> call, Throwable t) {
            NetworkLogUtility.logFailure(call, t);

            if (!call.isCanceled()){
                loadingImageView.setVisibility(View.GONE);

                if(NetworkUtility.isKnownException(t)){
                    errorTextView.setText("Can't load data.\nCheck your network connection.");
                    errorLinearLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    };
    // endregion

    // region Constructors
    public VideoPlayerFragment() {
    }
    // endregion

    // region Factory Methods
    public static VideoPlayerFragment newInstance() {
        return new VideoPlayerFragment();
    }

    public static VideoPlayerFragment newInstance(Bundle extras) {
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        fragment.setArguments(extras);
        return fragment;
    }
    // endregion

    // region Lifecycle Methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        if(getArguments() != null){
            videoId = getArguments().getLong(VideoDetailsFragment.KEY_VIDEO_ID);
        }

        font = FontCache.getTypeface("Ubuntu-Medium.ttf", getContext());

        AccessToken token = LoopPrefs.getAccessToken(getActivity());
        vimeoPlayerService = ServiceGenerator.createService(
                VimeoPlayerService.class,
                VimeoPlayerService.BASE_URL,
                new AuthorizedNetworkInterceptor(token));

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_video_player, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Create a default TrackSelector
        TrackSelector trackSelector = createTrackSelector();

        // Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();

        // Create the player
        player = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector, loadControl);
        player.setPlayWhenReady(true);

        simpleExoPlayerView.setPlayer(player);
        player.addListener(exoPlayerEventListener);

        showSystemUI();

        simpleExoPlayerView.setControllerVisibilityListener(playbackControlViewVisibilityListener);

        VideoSavedState videoSavedState = getVideoSavedState();
        if(videoSavedState != null && !TextUtils.isEmpty(videoSavedState.getVideoUrl())){
            loadingImageView.setVisibility(View.GONE);
            String videoUrl = videoSavedState.getVideoUrl();
            long currentPosition = videoSavedState.getCurrentPosition();
            player.seekTo(currentPosition);
            player.prepare(getMediaSource(videoUrl));
        } else {
            Call getVideoConfigCall = vimeoPlayerService.getVideoConfig(videoId);
            calls.add(getVideoConfigCall);
            getVideoConfigCall.enqueue(getVideoConfigCallback);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        if(!TextUtils.isEmpty(videoUrl)){
            VideoSavedState videoSavedState = new VideoSavedState();
            videoSavedState.setVideoUrl(videoUrl);
            videoSavedState.setCurrentPosition(player.getCurrentPosition());
            setVideoSavedState(videoSavedState);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        removeListeners();
        player.release();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    // endregion

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    // region Helper Methods
    private void removeListeners(){
        player.removeListener(exoPlayerEventListener);
//        simpleExoPlayerView.setControllerVisibilityListener(null);
    }

    // This snippet hides the system bars.
    private void hideSystemUI() {
        final View decorView = getActivity().getWindow().getDecorView();

        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        final View decorView = getActivity().getWindow().getDecorView();

        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private String getVideoUrl(VideoConfig videoConfig) {
        String videoUrl = "";

        if (videoConfig != null) {
            Request request = videoConfig.getRequest();
            if (request != null) {
                Files files = request.getFiles();
                if (files != null) {
                    H264 h264 = files.getH264();
                    HLS hls = files.getHls();
                    VP6 vp6 = files.getVp6();
                    List<ProgressiveData> progressiveDataList = files.getProgressive();

                    String progressiveDataUrl = getProgressiveDataUrl(progressiveDataList);
                    String h264VideoUrl = getH264VideoUrl(h264);
                    String vp6VideoUrl = getVP6VideoUrl(vp6);
                    String hlsVideoUrl = getHLSVideoUrl(hls);

                    if (!TextUtils.isEmpty(progressiveDataUrl)) {
                        videoUrl = progressiveDataUrl;
                    } else if (!TextUtils.isEmpty(h264VideoUrl)) {
                        videoUrl = h264VideoUrl;
                    } else if (!TextUtils.isEmpty(vp6VideoUrl)) {
                        videoUrl = vp6VideoUrl;
                    } else if (!TextUtils.isEmpty(hlsVideoUrl)) {
                        videoUrl = hlsVideoUrl;
                    }
                }
            }
        }

        return videoUrl;
    }

    private String getHLSVideoUrl(HLS hls) {
        String videoUrl = "";
        if (hls != null) {
            String url = hls.getUrl();
            videoUrl = url;
        }
        return videoUrl;
    }

    private String getProgressiveDataUrl(List<ProgressiveData> progressiveDataList){
        String progressiveDataUrl = "";

        String progessiveData270pUrl = "";
        String progessiveData360pUrl = "";
        String progessiveData1080pUrl = "";

        for(ProgressiveData progressiveData : progressiveDataList){
            String quality = progressiveData.getQuality();
            switch (quality) {
                case "1080p":
                    progessiveData1080pUrl = progressiveData.getUrl();
                    break;
                case "360p":
                    progessiveData360pUrl = progressiveData.getUrl();
                    break;
                case "270p":
                    progessiveData270pUrl = progressiveData.getUrl();
                    break;
            }
        }

        if(!TextUtils.isEmpty(progessiveData1080pUrl)){
            progressiveDataUrl = progessiveData1080pUrl;
        } else if(!TextUtils.isEmpty(progessiveData360pUrl)){
            progressiveDataUrl = progessiveData360pUrl;
        } else if(!TextUtils.isEmpty(progessiveData270pUrl)){
            progressiveDataUrl = progessiveData270pUrl;
        }

        return progressiveDataUrl;
    }

    private String getH264VideoUrl(H264 h264) {
        String videoUrl = "";
        if (h264 != null) {

            VideoFormat hdVideoFormat = h264.getHd();
            VideoFormat sdVideoFormat = h264.getSd();
            VideoFormat mobileVideoFormat = h264.getMobile();

            int width = -1;
            int height = -1;
            if (hdVideoFormat != null) {
                videoUrl = hdVideoFormat.getUrl();
                width = hdVideoFormat.getWidth();
                height = hdVideoFormat.getHeight();
            } else if (sdVideoFormat != null) {
                videoUrl = sdVideoFormat.getUrl();
                width = sdVideoFormat.getWidth();
                height = sdVideoFormat.getHeight();
            } else if (mobileVideoFormat != null) {
                videoUrl = mobileVideoFormat.getUrl();
                width = mobileVideoFormat.getWidth();
                height = mobileVideoFormat.getHeight();
            }

            Timber.d(String.format("mGetVideoConfigCallback : url - %s", videoUrl));
            Timber.d(String.format("mGetVideoConfigCallback : width - %d : height - %d", width, height));
        }

        return videoUrl;
    }

    private String getVP6VideoUrl(VP6 vp6) {
        String videoUrl = "";
        if (vp6 != null) {

            VideoFormat hdVideoFormat = vp6.getHd();
            VideoFormat sdVideoFormat = vp6.getSd();
            VideoFormat mobileVideoFormat = vp6.getMobile();

            int width = -1;
            int height = -1;
            if (hdVideoFormat != null) {
                videoUrl = hdVideoFormat.getUrl();
                width = hdVideoFormat.getWidth();
                height = hdVideoFormat.getHeight();
            } else if (sdVideoFormat != null) {
                videoUrl = sdVideoFormat.getUrl();
                width = sdVideoFormat.getWidth();
                height = sdVideoFormat.getHeight();
            } else if (mobileVideoFormat != null) {
                videoUrl = mobileVideoFormat.getUrl();
                width = mobileVideoFormat.getWidth();
                height = mobileVideoFormat.getHeight();
            }

            Timber.d(String.format("mGetVideoConfigCallback : url - %s", videoUrl));
            Timber.d(String.format("mGetVideoConfigCallback : width - %d : height - %d", width, height));
        }

        return videoUrl;
    }

    public void setVideoSavedState(VideoSavedState videoSavedState) {
        this.videoSavedState = videoSavedState;
    }

    public VideoSavedState getVideoSavedState() {
        return videoSavedState;
    }

    private TrackSelector createTrackSelector(){
        // Create a default TrackSelector
        Handler mainHandler = new Handler();
        // Measures bandwidth during playback. Can be null if not required.
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
//        TrackSelector trackSelector =
//                new DefaultTrackSelector(mainHandler, videoTrackSelectionFactory);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        return trackSelector;
    }

    private MediaSource getMediaSource(String videoUrl){
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(),
                Util.getUserAgent(getContext(), "Loop"), bandwidthMeter);
        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        // This is the MediaSource representing the media to be played.
        MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(videoUrl),
                dataSourceFactory, extractorsFactory, null, null);
        // Loops the video indefinitely.
//        LoopingMediaSource loopingSource = new LoopingMediaSource(mediaSource);

//        MediaSource videoSource = new ExtractorMediaSource(mp4VideoUri,
//                dataSourceFactory, extractorsFactory, null, null);
        return mediaSource;
    }
    // endregion
}
