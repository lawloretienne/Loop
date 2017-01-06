package com.etiennelawlor.loop.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.etiennelawlor.loop.LoopApplication;
import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.models.AccessToken;
import com.etiennelawlor.loop.models.VideoSavedState;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoPlayerService;
import com.etiennelawlor.loop.network.interceptors.AuthorizedNetworkInterceptor;
import com.etiennelawlor.loop.network.models.response.Video;
import com.etiennelawlor.loop.network.models.response.VideoConfig;
import com.etiennelawlor.loop.prefs.LoopPrefs;
import com.etiennelawlor.loop.utilities.FontCache;
import com.etiennelawlor.loop.utilities.NetworkLogUtility;
import com.etiennelawlor.loop.utilities.NetworkUtility;
import com.etiennelawlor.loop.utilities.TrestleUtility;
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
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

/**
 * Created by etiennelawlor on 1/3/17.
 */

public class VideoDetailsFragment extends BaseFragment {

    // region Constants
    private static final int VIDEO_SHARE_REQUEST_CODE = 1002;
    public static final String KEY_VIDEO_ID = "KEY_VIDEO_ID";
    public static final String KEY_VIDEO = "KEY_VIDEO";
    // endregion

    // region Views
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @Nullable
    @BindView(R.id.viewpager)
    ViewPager viewPager;
    @Nullable
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.sepv)
    SimpleExoPlayerView simpleExoPlayerView;
//    @BindView(R.id.loading_iv)
//    LoadingImageView loadingImageView;
//    @BindView(R.id.error_ll)
//    LinearLayout errorLinearLayout;
//    @BindView(R.id.error_tv)
//    TextView errorTextView;
    @BindView(R.id.exo_thumbnail)
    ImageView thumbnailImageView;
    @BindView(R.id.cast_info_tv)
    TextView castInfoTextView;
    @BindView(R.id.exo_shutter)
    View shutterView;
    @BindView(R.id.exo_progress)
    SeekBar exoSeekBar;
    @BindView(R.id.exo_play)
    ImageButton exoPlayButton;
    @BindView(R.id.exo_pause)
    ImageButton exoPauseButton;
    @BindView(R.id.exo_replay)
    ImageButton replayImageButton;
    @BindView(R.id.exo_btns_fl)
    FrameLayout exoButtonsFrameLayout;
    @BindView(R.id.control_view_ll)
    LinearLayout controlViewLinearLayout;
    @BindView(R.id.mrb)
    MediaRouteButton mediaRouteButton;
    @BindView(R.id.exo_content_frame)
    AspectRatioFrameLayout aspectRatioFrameLayout;
    // endregion

    // region Member Variables
    private Video video;
    private String videoUrl;
    private VimeoPlayerService vimeoPlayerService;
    private VideoSavedState videoSavedState;
    private Unbinder unbinder;
    private Typeface font;
    private SimpleExoPlayer exoPlayer;
    private CastContext castContext;
    private PlaybackLocation location;
    private CastSession castSession;
    private PlaybackState playbackState;
    private boolean isLocalVideoPrepared = false;
    private long currentPosition = 0;
    private SessionManagerListener<CastSession> sessionManagerListener;
    // endregion

    // region Listeners
    @OnClick(R.id.exo_play)
    public void onPlayButtonClicked(){
        playbackState = PlaybackState.PLAYING;
        exoPlayButton.setVisibility(View.GONE);
        exoPauseButton.setVisibility(View.VISIBLE);

        resumeLocalVideo();
        if(location == PlaybackLocation.REMOTE){
            resumeRemoteVideo();
            castInfoTextView.setText(String.format("Casting to %s", castSession.getCastDevice().getFriendlyName()));
        }
    }

    @OnClick(R.id.exo_pause)
    public void onPauseButtonClicked(){
        playbackState = PlaybackState.PAUSED;
        exoPauseButton.setVisibility(View.GONE);
        exoPlayButton.setVisibility(View.VISIBLE);

        pauseLocalVideo();
        if(location == PlaybackLocation.REMOTE){
            pauseRemoteVideo();
            castInfoTextView.setText(getString(R.string.cast_is_paused));
        }
    }

    @OnClick(R.id.exo_replay)
    public void onReplayButtonClicked() {
        replayImageButton.setVisibility(View.GONE);
        exoButtonsFrameLayout.setVisibility(View.VISIBLE);
        switch (location){
            case LOCAL:
                updateLocalVideoPosition(0);
                break;
            case REMOTE:
                updateLocalVideoPosition(0);
                pauseLocalVideo();
                playRemoteVideo(0, true);
                break;
            default:
                break;
        }
    }

//    @OnClick(R.id.reload_btn)
//    public void onReloadButtonClicked() {
////        errorLinearLayout.setVisibility(View.GONE);
////        loadingImageView.setVisibility(View.VISIBLE);
//
//        Call getVideoConfigCall = vimeoPlayerService.getVideoConfig(videoId);
//        calls.add(getVideoConfigCall);
//        getVideoConfigCall.enqueue(getVideoConfigCallback);
//    }

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
            int orientation = getContext().getResources().getConfiguration().orientation;
            switch (orientation){
                case ORIENTATION_PORTRAIT:
                    if(visibility == View.GONE){
                        toolbar.setVisibility(View.GONE);
                        hidePortraitSystemUI();
                    } else {
                        showPortraitSystemUI();
                        toolbar.setVisibility(View.VISIBLE);
                    }
                    break;
                case ORIENTATION_LANDSCAPE:
                    if(visibility == View.GONE){
                        toolbar.setVisibility(View.GONE);
                        hideLandscapeSystemUI();
                    } else {
                        showLandscapeSystemUI();
                        toolbar.setVisibility(View.VISIBLE);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener seekBarOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser){
                currentPosition = (long)(exoPlayer.getDuration() * (progress/1000.0D));

                switch (location) {
                    case LOCAL:
                        updateLocalVideoPosition(currentPosition);
                        break;
                    case REMOTE:
                        updateLocalVideoPosition(currentPosition);
                        pauseLocalVideo();
                        castSession.getRemoteMediaClient().addListener(new RemoteMediaClient.Listener() {
                            @Override
                            public void onStatusUpdated() {
                                if(isResumed()){
                                    if(isRemoteVideoPlaying()){
                                        playbackState = PlaybackState.PLAYING;
                                        resumeLocalVideo();
                                        castInfoTextView.setText(String.format("Casting to %s", castSession.getCastDevice().getFriendlyName()));
                                    } else {
//                                    playbackState = PlaybackState.PAUSED;
                                        pauseLocalVideo();
//                                    castInfoTextView.setText(getString(R.string.cast_is_paused));

                                        if(playbackState == PlaybackState.PLAYING){
                                            castInfoTextView.setText(getString(R.string.cast_is_loading));
                                        }
                                    }

                                    castInfoTextView.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onMetadataUpdated() {
                            }

                            @Override
                            public void onQueueStatusUpdated() {
                            }

                            @Override
                            public void onPreloadStatusUpdated() {
                            }

                            @Override
                            public void onSendingRemoteMediaRequest() {
                            }

                            @Override
                            public void onAdBreakStatusUpdated() {
                            }
                        });
                        updateRemoteVideoPosition(currentPosition);
                        break;
                    default:
                        break;
                }
            } else {
//                        playbackState = PlaybackState.PLAYING;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

//    private RemoteMediaClient.Listener remoteMediaClientListener = new RemoteMediaClient.Listener() {
//        @Override
//        public void onStatusUpdated() {
//            Timber.d("VideoDetailsFragment : remoteMediaClientListener : onStatusUpdated()");
//            if(isResumed()){
//                Timber.d("VideoDetailsFragment : remoteMediaClientListener : onStatusUpdated() : isResumed()");
//
//                if(isRemoteVideoPlaying()){
//                    playbackState = PlaybackState.PLAYING;
//
//                    Timber.d("VideoDetailsFragment : loadRemoteMedia() : onStatusUpdated() : isResumed() : isRemoteVideoPlaying() : getFormattedPosition(position) - %s", getFormattedPosition(currentPosition));
//                    Timber.d("VideoDetailsFragment : loadRemoteMedia() : onStatusUpdated() : isResumed() : isRemoteVideoPlaying() : getFormattedPosition(castSession.getRemoteMediaClient().getApproximateStreamPosition()) - %s", getFormattedPosition(castSession.getRemoteMediaClient().getApproximateStreamPosition()));
//
//                    updateLocalVideoVolume(0.0f);
//
//                    if(!isLocalVideoPrepared)
//                        // Prepare the player with the source.
//                        exoPlayer.prepare(getMediaSource(videoUrl));
//
////                currentPosition = (exoPlayer.getDuration() * (exoSeekBar.getProgress()/1000L));
////                currentPosition = exoPlayer.getCurrentPosition();
//                    currentPosition = castSession.getRemoteMediaClient().getApproximateStreamPosition();
//
//                    updateLocalVideoPosition(currentPosition);
//                    resumeLocalVideo();
//
////                remoteMediaClient.removeListener(this);
//                    castInfoTextView.setText(String.format("Casting to %s", castSession.getCastDevice().getFriendlyName()));
////                    remoteMediaClient.removeListener(this);
//                } else {
////                 playbackState = PlaybackState.PAUSED;
//                    pauseLocalVideo();
//
////                castInfoTextView.setText(getString(R.string.cast_is_paused));
//                    if(playbackState == PlaybackState.PLAYING){
//                        castInfoTextView.setText(getString(R.string.cast_is_loading));
//                    }
//                }
//                castInfoTextView.setVisibility(View.VISIBLE);
//
////                Intent intent = new Intent(LocalPlayerActivity.this, ExpandedControlsActivity.class);
////                startActivity(intent);
////                remoteMediaClient.removeListener(this);
//            }
//        }
//
//        @Override
//        public void onMetadataUpdated() {
//            Timber.d("VideoDetailsFragment : remoteMediaClientListener : onMetadataUpdated()");
//        }
//
//        @Override
//        public void onQueueStatusUpdated() {
//            Timber.d("VideoDetailsFragment : remoteMediaClientListener : onQueueStatusUpdated()");
//        }
//
//        @Override
//        public void onPreloadStatusUpdated() {
//            Timber.d("VideoDetailsFragment : remoteMediaClientListener : onPreloadStatusUpdated()");
//        }
//
//        @Override
//        public void onSendingRemoteMediaRequest() {
//            Timber.d("VideoDetailsFragment : remoteMediaClientListener : onSendingRemoteMediaRequest()");
//        }
//
//        @Override
//        public void onAdBreakStatusUpdated() {
//            Timber.d("VideoDetailsFragment : remoteMediaClientListener : onAdBreakStatusUpdated()");
//        }
//    };
    // endregion

    // region Callbacks
    private Callback<VideoConfig> getVideoConfigCallback = new Callback<VideoConfig>() {
        @Override
        public void onResponse(Call<VideoConfig> call, Response<VideoConfig> response) {
//            loadingImageView.setVisibility(View.GONE);

            if (!response.isSuccessful()) {

                int responseCode = response.code();
                switch (responseCode){
                    case 504: // 504 Unsatisfiable Request (only-if-cached)
//                        errorTextView.setText("Can't load data.\nCheck your network connection.");
//                        errorLinearLayout.setVisibility(View.VISIBLE);
                        break;
                    case 403: // Forbidden
                        Snackbar.make(getActivity().findViewById(R.id.main_content),
                                TrestleUtility.getFormattedText("Cannot play this video.", font, 16),
                                Snackbar.LENGTH_LONG)
                                .show();
                        break;
                }
                return;
            }

            VideoConfig videoConfig = response.body();
            if (videoConfig != null) {
                videoUrl = videoConfig.getVideoUrl();
                if (!TextUtils.isEmpty(videoUrl)) {
                    playVideo(0);
                }
            }
        }

        @Override
        public void onFailure(Call<VideoConfig> call, Throwable t) {
            NetworkLogUtility.logFailure(call, t);

            if (!call.isCanceled()){
//                loadingImageView.setVisibility(View.GONE);

                if(NetworkUtility.isKnownException(t)){
//                    errorTextView.setText("Can't load data.\nCheck your network connection.");
//                    errorLinearLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    // endregion

    // region Constructors
    public VideoDetailsFragment() {
    }
    // endregion

    // region Factory Methods
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
    // endregion

    // region Lifecycle Methods
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        if (getArguments() != null) {
            video = (Video) getArguments().get(LikedVideosFragment.KEY_VIDEO);
//            mTransitionName = getArguments().getString("TRANSITION_KEY");
        }

        AccessToken token = LoopPrefs.getAccessToken(getActivity());
        vimeoPlayerService = ServiceGenerator.createService(
                VimeoPlayerService.class,
                VimeoPlayerService.BASE_URL,
                new AuthorizedNetworkInterceptor(token));

        setHasOptionsMenu(true);

        font = FontCache.getTypeface("Ubuntu-Medium.ttf", getContext());

        castContext = CastContext.getSharedInstance(getContext());
        castSession = castContext.getSessionManager().getCurrentCastSession();

        playbackState = PlaybackState.PLAYING;

        if (castSession != null && castSession.isConnected()) {
            location = PlaybackLocation.REMOTE;
        } else {
            location = PlaybackLocation.LOCAL;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_video_details, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }

        if (video != null) {
            setUpExoPlayer();
            setUpSimpleExoPlayerView();
            setUpThumbnail();
            setUpCastButton();

            exoSeekBar.setOnSeekBarChangeListener(seekBarOnSeekBarChangeListener);

            setupCastListener();
        }

        int orientation = view.getResources().getConfiguration().orientation;

        switch (orientation){
            case ORIENTATION_PORTRAIT:
                if (viewPager != null) {
                    setupViewPager(viewPager);
                }

                tabLayout.setupWithViewPager(viewPager);
                tabLayout.setTabMode(TabLayout.MODE_FIXED);

                updateTabLayout();
                showPortraitSystemUI();
                break;
            case ORIENTATION_LANDSCAPE:
                showLandscapeSystemUI();
                break;
            default:
                break;
        }

        VideoSavedState videoSavedState = getVideoSavedState();
        if(videoSavedState != null && !TextUtils.isEmpty(videoSavedState.getVideoUrl())){
//            loadingImageView.setVisibility(View.GONE);
            long currentPosition = videoSavedState.getCurrentPosition();
            videoUrl = videoSavedState.getVideoUrl();
            playbackState = videoSavedState.getPlaybackState();
            castInfoTextView.setText(videoSavedState.getCastInfo());
            castInfoTextView.setVisibility(View.VISIBLE);

            boolean autoPlay = false;
            switch (playbackState){
                case PLAYING:
                    autoPlay = true;
                    break;
                case PAUSED:
                    autoPlay = false;
                    break;
                default:
                    break;
            }

            switch (location) {
                case LOCAL:
                    playLocalVideo(currentPosition, autoPlay);
                    updateLocalVideoVolume(videoSavedState.getCurrentVolume());
                    break;
                case REMOTE:
                    playLocalVideo(castSession.getRemoteMediaClient().getApproximateStreamPosition(), autoPlay);
                    updateLocalVideoVolume(videoSavedState.getCurrentVolume());
                    break;
                default:
                    break;
            }
        } else {
            Call getVideoConfigCall = vimeoPlayerService.getVideoConfig(video.getId());
            calls.add(getVideoConfigCall);
            getVideoConfigCall.enqueue(getVideoConfigCallback);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        castContext.getSessionManager().addSessionManagerListener(
                sessionManagerListener, CastSession.class);

        if (castSession != null && castSession.isConnected()) {
            location = PlaybackLocation.REMOTE;
        } else {
            location = PlaybackLocation.LOCAL;
        }

        if(!isLocalVideoPlaying() && playbackState == PlaybackState.PLAYING) {
            switch (location){
                case LOCAL:
                    resumeLocalVideo();
                    break;
                case REMOTE:
                    updateLocalVideoPosition(castSession.getRemoteMediaClient().getApproximateStreamPosition());
                    resumeLocalVideo();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        castContext.getSessionManager().removeSessionManagerListener(
                sessionManagerListener, CastSession.class);

        switch (location) {
            case LOCAL:
                pauseLocalVideo();
                break;
            case REMOTE:
                break;
            default:
                break;
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(!TextUtils.isEmpty(videoUrl)){
            VideoSavedState videoSavedState = new VideoSavedState();
            videoSavedState.setVideoUrl(videoUrl);
            videoSavedState.setCurrentPosition(exoPlayer.getCurrentPosition());
            videoSavedState.setCurrentVolume(exoPlayer.getVolume());
            videoSavedState.setPlaybackState(playbackState);
            videoSavedState.setCastInfo(castInfoTextView.getText().toString());
            setVideoSavedState(videoSavedState);
        }

        removeListeners();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        exoPlayer.release();
    }

    // endregion

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.video_details_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                if (video != null) {
//                    EventLogger.fire(ProductShareEvent.start(mProduct.getId()));

                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra(Intent.EXTRA_TEXT,
                            String.format("I found this on Loop. Check it out.\n\n%s\n\n%s", video.getName(), video.getLink()));

                    String title = getResources().getString(R.string.share_this_video);
                    Intent chooser = Intent.createChooser(sendIntent, title);

                    if (sendIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivityForResult(chooser, VIDEO_SHARE_REQUEST_CODE);
                    }
                }
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case VIDEO_SHARE_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    if (video != null) {
//                        EventLogger.fire(ProductShareEvent.submit(mProduct.getId()));
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                }
                break;
            default:
                break;
        }
    }

    // region Helper Methods
    private void setUpExoPlayer(){
        // Create a default TrackSelector
        TrackSelector trackSelector = createTrackSelector();

        // Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();

        // Create the player
        exoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector, loadControl);
        resumeLocalVideo();

        exoPlayer.addListener(exoPlayerEventListener);
    }

    private void setUpThumbnail() {
        String thumbnailUrl = video.getThumbnailUrl();

        if (!TextUtils.isEmpty(thumbnailUrl)) {
            Glide.with(getActivity())
                    .load(thumbnailUrl)
//                                .placeholder(R.drawable.ic_placeholder)
//                                .error(R.drawable.ic_error)
                    .into(thumbnailImageView);
        }
    }

    private void setUpSimpleExoPlayerView(){
        simpleExoPlayerView.setPlayer(exoPlayer);
        simpleExoPlayerView.setControllerVisibilityListener(playbackControlViewVisibilityListener);
    }

    // This snippet hides the system bars.
    private void hidePortraitSystemUI() {
        final View decorView = getActivity().getWindow().getDecorView();

        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showPortraitSystemUI() {
        final View decorView = getActivity().getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    // This snippet hides the system bars.
    private void hideLandscapeSystemUI() {
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
    private void showLandscapeSystemUI() {
        final View decorView = getActivity().getWindow().getDecorView();

        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

    }

    public void setVideoSavedState(VideoSavedState videoSavedState) {
        this.videoSavedState = videoSavedState;
    }

    public VideoSavedState getVideoSavedState() {
        return videoSavedState;
    }

    private TrackSelector createTrackSelector(){
        // Create a default TrackSelector
        // Measures bandwidth during playback. Can be null if not required.
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        return trackSelector;
    }

    private MediaSource getMediaSource(String videoUrl){
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
//        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "Loop"), bandwidthMeter);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(LoopApplication.getInstance().getApplicationContext(), Util.getUserAgent(LoopApplication.getInstance().getApplicationContext(), "Loop"), bandwidthMeter);
        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        // This is the MediaSource representing the media to be played.
        MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(videoUrl),
                dataSourceFactory, extractorsFactory, null, null);
        // Loops the video indefinitely.
//        LoopingMediaSource loopingSource = new LoopingMediaSource(mediaSource);
        return mediaSource;
    }

    private void removeListeners() {
        exoPlayer.removeListener(exoPlayerEventListener);
        simpleExoPlayerView.setControllerVisibilityListener(null);
        exoSeekBar.setOnSeekBarChangeListener(null);
    }

    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getChildFragmentManager());

        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_VIDEO, video);
        VideoDetailsInfoFragment videoDetailsInfoFragment = VideoDetailsInfoFragment.newInstance(bundle);
        adapter.addFragment(videoDetailsInfoFragment, getString(R.string.info));

        Bundle bundle2 = new Bundle();
        bundle2.putLong(KEY_VIDEO_ID, video.getId());
        RelatedVideosFragment relatedVideosFragment = RelatedVideosFragment.newInstance(bundle2);
        adapter.addFragment(relatedVideosFragment, getString(R.string.related_videos));

        viewPager.setAdapter(adapter);
    }

    private void updateTabLayout(){
        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(font);
                }
            }
        }
    }

    private void setUpCastButton(){
        Drawable remoteIndicatorDrawable = getRemoteIndicatorDrawable();
        DrawableCompat.setTint(remoteIndicatorDrawable, ContextCompat.getColor(getContext(), android.R.color.white));
        mediaRouteButton.setRemoteIndicatorDrawable(remoteIndicatorDrawable);
        CastButtonFactory.setUpMediaRouteButton(getContext().getApplicationContext(), mediaRouteButton);
    }

    private Drawable getRemoteIndicatorDrawable(){
        Context castContext = new ContextThemeWrapper(getContext(), android.support.v7.mediarouter.R.style.Theme_MediaRouter);
        TypedArray a = castContext.obtainStyledAttributes(null,
                android.support.v7.mediarouter.R.styleable.MediaRouteButton, android.support.v7.mediarouter.R.attr.mediaRouteButtonStyle, 0);
        Drawable remoteIndicatorDrawable = a.getDrawable(
                android.support.v7.mediarouter.R.styleable.MediaRouteButton_externalRouteEnabledDrawable);
        a.recycle();
        return remoteIndicatorDrawable;
    }

    private void setupCastListener() {
        sessionManagerListener = new SessionManagerListener<CastSession>() {

            @Override
            public void onSessionEnded(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionResumed(CastSession session, boolean wasSuspended) {
                onApplicationConnected(session);
            }

            @Override
            public void onSessionResumeFailed(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarted(CastSession session, String sessionId) {
                onApplicationConnected(session);
            }

            @Override
            public void onSessionStartFailed(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarting(CastSession session) {
            }

            @Override
            public void onSessionEnding(CastSession session) {
            }

            @Override
            public void onSessionResuming(CastSession session, String sessionId) {
            }

            @Override
            public void onSessionSuspended(CastSession session, int reason) {
            }

            private void onApplicationConnected(CastSession session) {
                castSession = session;
//                castSession.getRemoteMediaClient().addListener(remoteMediaClientListener);

                location = PlaybackLocation.REMOTE;

                pauseLocalVideo();
                thumbnailImageView.animate().alpha(1.0f).setDuration(1000);
                castInfoTextView.setText(getString(R.string.connecting_to_receiver));
                castInfoTextView.setVisibility(View.VISIBLE);

                if(!TextUtils.isEmpty(videoUrl)){
                    if (playbackState == PlaybackState.PLAYING) {
                        loadRemoteMedia(exoPlayer.getCurrentPosition(), true);
                    } else if(playbackState == PlaybackState.PAUSED){
                        loadRemoteMedia(exoPlayer.getCurrentPosition(), false);
                    }
//                else {
//                    playbackState = PlaybackState.IDLE;
//                }
                }

            }

            private void onApplicationDisconnected() {
                castSession = null;

                location = PlaybackLocation.LOCAL;
//                playbackState = PlaybackState.IDLE;
                thumbnailImageView.animate().alpha(0.0f).setDuration(1000);
                castInfoTextView.setVisibility(View.GONE);

                updateLocalVideoVolume(1.0f);
            }
        };
    }

    private void loadRemoteMedia(final long position, boolean autoPlay) {
        currentPosition = position;

        final RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
        remoteMediaClient.addListener(new RemoteMediaClient.Listener() {
            @Override
            public void onStatusUpdated() {
                if(isResumed()){
                    if(isRemoteVideoPlaying()) {
                        playbackState = PlaybackState.PLAYING;
                        updateLocalVideoVolume(0.0f);

                        if(!isLocalVideoPrepared)
                            // Prepare the player with the source.
                            exoPlayer.prepare(getMediaSource(videoUrl));

                        updateLocalVideoPosition(position);
                        resumeLocalVideo();

                        castInfoTextView.setText(String.format("Casting to %s", castSession.getCastDevice().getFriendlyName()));
                        remoteMediaClient.removeListener(this);
                    } else {
                        if(playbackState == PlaybackState.PLAYING){
                            castInfoTextView.setText(getString(R.string.cast_is_loading));
                        }
                    }

                    castInfoTextView.setVisibility(View.VISIBLE);

//                Intent intent = new Intent(LocalPlayerActivity.this, ExpandedControlsActivity.class);
//                startActivity(intent);
//                remoteMediaClient.removeListener(this);
                }
            }

            @Override
            public void onMetadataUpdated() {
            }

            @Override
            public void onQueueStatusUpdated() {
            }

            @Override
            public void onPreloadStatusUpdated() {
            }

            @Override
            public void onSendingRemoteMediaRequest() {
            }

            @Override
            public void onAdBreakStatusUpdated() {
            }
        });

        castSession.getRemoteMediaClient().load(buildMediaInfo(), autoPlay, position);
    }

    private MediaInfo buildMediaInfo() {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, video.getUser().getName());
        movieMetadata.putString(MediaMetadata.KEY_TITLE, video.getName());
        movieMetadata.addImage(new WebImage(Uri.parse(video.getThumbnailUrl())));

        return new MediaInfo.Builder(videoUrl)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("videos/mp4")
                .setMetadata(movieMetadata)
                .setStreamDuration(exoPlayer.getDuration() * 1000)
                .build();
    }

    private void playVideo(long position){
        switch (location) {
            case LOCAL:
                playLocalVideo(position, true);
                break;
            case REMOTE:
                playRemoteVideo(position, true);
                break;
            default:
                break;
        }
    }

    private void playLocalVideo(long position, boolean autoPlay){
        if(location == PlaybackLocation.LOCAL){
            thumbnailImageView.animate().alpha(0.0f).setDuration(1000);
            castInfoTextView.setVisibility(View.GONE);
        }
        updateLocalVideoPosition(position);
        // Prepare the player with the source.
        exoPlayer.prepare(getMediaSource(videoUrl));
        isLocalVideoPrepared = true;
        if(!autoPlay)
            pauseLocalVideo();
    }

    private void playRemoteVideo(long position, boolean autoPlay){
        updateRemoteVideoPosition(position);
        loadRemoteMedia(position, autoPlay);
    }

    private void updateVideoPosition(int position){
        switch (location) {
            case LOCAL:
                updateLocalVideoPosition(position);
                break;
            case REMOTE:
                updateRemoteVideoPosition(position);
                break;
            default:
                break;
        }
    }

    private void updateLocalVideoPosition(long position){
        exoPlayer.seekTo(position);
    }

    private void updateRemoteVideoPosition(long position){
        castSession.getRemoteMediaClient().seek(position, RemoteMediaClient.RESUME_STATE_UNCHANGED);
    }

    private boolean isVideoPlaying(){
        switch (location) {
            case LOCAL:
                return isLocalVideoPlaying();
            case REMOTE:
                return isRemoteVideoPlaying();
            default:
                return false;
        }
    }

    private boolean isLocalVideoPlaying(){
        return exoPlayer.getPlayWhenReady();
    }

    private boolean isRemoteVideoPlaying(){
        if(castSession != null){
            RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
            return remoteMediaClient.isPlaying();
        }
        return false;
    }

    private void resumeVideo(){
        switch (location) {
            case LOCAL:
                resumeLocalVideo();
                break;
            case REMOTE:
                resumeRemoteVideo();
                break;
            default:
                break;
        }
    }

    private void resumeLocalVideo(){
        exoPlayer.setPlayWhenReady(true);
    }

    private void resumeRemoteVideo(){
        if(castSession != null){
            RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
            if(remoteMediaClient != null){
                remoteMediaClient.play();
            }
        }
    }

    private void pauseVideo(){
        switch (location) {
            case LOCAL:
                pauseLocalVideo();
                break;
            case REMOTE:
                pauseRemoteVideo();
                break;
            default:
                break;
        }
    }

    private void pauseLocalVideo(){
        exoPlayer.setPlayWhenReady(false);
    }

    private void pauseRemoteVideo(){
        if(castSession != null){
            RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
            if(remoteMediaClient != null){
                remoteMediaClient.pause();
            }
        }
    }

    private void updateVideoVolume(float volume){
        switch (location) {
            case LOCAL:
                updateLocalVideoVolume(volume);
                break;
            case REMOTE:
                updateRemoteVideoVolume(volume);
                break;
            default:
                break;
        }
    }

    private void updateLocalVideoVolume(float volume){
        exoPlayer.setVolume(volume);
    }

    private void updateRemoteVideoVolume(float volume){

    }

    // endregion

    // region Inner Classes
    public static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> fragments = new ArrayList<>();
        private final List<String> fragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            fragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }
    }

    /**
     * indicates whether we are doing a local or a remote playback
     */
    public enum PlaybackLocation {
        LOCAL,
        REMOTE
    }

    /**
     * List of various states that we can be in
     */
    public enum PlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE
    }
    // endregion

}
