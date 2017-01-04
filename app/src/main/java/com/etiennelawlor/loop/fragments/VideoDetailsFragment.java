package com.etiennelawlor.loop.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

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
    @BindView(R.id.exo_artwork)
    ImageView artworkImageView;
    @BindView(R.id.exo_replay)
    ImageButton replayImageButton;
    @BindView(R.id.exo_btns_fl)
    FrameLayout exoButtonsFrameLayout;
    @BindView(R.id.control_view_ll)
    LinearLayout controlViewLinearLayout;
    @BindView(R.id.mrb)
    MediaRouteButton mediaRouteButton;
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
    // endregion

    // region Listeners
    @OnClick(R.id.exo_replay)
    public void onReplayButtonClicked() {
        replayImageButton.setVisibility(View.GONE);
        exoButtonsFrameLayout.setVisibility(View.VISIBLE);
        exoPlayer.seekTo(0);
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
                videoUrl = videoConfig.getVideoUrl();
                if (!TextUtils.isEmpty(videoUrl)) {

                    // Prepare the player with the source.
                    exoPlayer.prepare(getMediaSource(videoUrl));
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
            setUpArtwork();
            setUpExoPlayer();
            setUpSimpleExoPlayerView();
            setUpCastButton();
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
            String videoUrl = videoSavedState.getVideoUrl();
            long currentPosition = videoSavedState.getCurrentPosition();
            exoPlayer.seekTo(currentPosition);
            exoPlayer.prepare(getMediaSource(videoUrl));
        } else {
            Call getVideoConfigCall = vimeoPlayerService.getVideoConfig(video.getId());
            calls.add(getVideoConfigCall);
            getVideoConfigCall.enqueue(getVideoConfigCallback);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(!exoPlayer.getPlayWhenReady())
            exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(!TextUtils.isEmpty(videoUrl)){
            VideoSavedState videoSavedState = new VideoSavedState();
            videoSavedState.setVideoUrl(videoUrl);
            videoSavedState.setCurrentPosition(exoPlayer.getCurrentPosition());
            setVideoSavedState(videoSavedState);
        }

        removeListeners();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.d("");
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
        exoPlayer.setPlayWhenReady(true);

        exoPlayer.addListener(exoPlayerEventListener);
    }

    private void setUpArtwork() {
        String thumbnailUrl = video.getThumbnailUrl();

        if (!TextUtils.isEmpty(thumbnailUrl)) {
            Glide.with(getActivity())
                    .load(thumbnailUrl)
//                                .placeholder(R.drawable.ic_placeholder)
//                                .error(R.drawable.ic_error)
                    .into(artworkImageView);
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

    private void removeListeners() {
        exoPlayer.removeListener(exoPlayerEventListener);
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
    // endregion

}
