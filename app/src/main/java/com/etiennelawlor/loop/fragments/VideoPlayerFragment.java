package com.etiennelawlor.loop.fragments;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.models.VideoSavedState;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoPlayerService;
import com.etiennelawlor.loop.models.AccessToken;
import com.etiennelawlor.loop.network.models.response.Files;
import com.etiennelawlor.loop.network.models.response.H264;
import com.etiennelawlor.loop.network.models.response.HLS;
import com.etiennelawlor.loop.network.models.response.ProgressiveData;
import com.etiennelawlor.loop.network.models.response.Request;
import com.etiennelawlor.loop.network.models.response.VP6;
import com.etiennelawlor.loop.network.models.response.VideoConfig;
import com.etiennelawlor.loop.network.models.response.VideoFormat;
import com.etiennelawlor.loop.otto.BusProvider;
import com.etiennelawlor.loop.prefs.LoopPrefs;
import com.etiennelawlor.loop.ui.LoadingImageView;
import com.etiennelawlor.loop.utilities.LogUtility;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class VideoPlayerFragment extends BaseFragment {

    // region Constants
    // endregion

    // region Views
    @Bind(R.id.vv)
    VideoView videoView;
    @Bind(R.id.loading_iv)
    LoadingImageView loadingImageView;
    // endregion

    // region Member Variables
    private Long videoId;
    private String videoUrl;
    private MediaController mediaController;
    private VimeoPlayerService vimeoPlayerService;
    private VideoSavedState videoSavedState;
    // endregion

    // region Callbacks
    private Callback<VideoConfig> getVideoConfigCallback = new Callback<VideoConfig>() {
        @Override
        public void onResponse(Response<VideoConfig> response, Retrofit retrofit) {
            if (response != null) {
                if(response.isSuccess()){
                    VideoConfig videoConfig = response.body();
                    if (videoConfig != null) {
                        videoUrl = getVideoUrl(videoConfig);
                        Timber.d("onResponse() : videoUrl - " + videoUrl);

                        if (!TextUtils.isEmpty(videoUrl)) {
                            Timber.d("playVideo()");

                            playVideo(videoUrl, 0);
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
            if (t != null) {
                String message = t.getMessage();
                LogUtility.logFailure(t);

                if (t instanceof SocketTimeoutException || t instanceof UnknownHostException) {
                    Timber.e("Timeout occurred");
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

        BusProvider.getInstance().register(this);

        if(getArguments() != null){
            videoId = getArguments().getLong("video_id");
        }

        AccessToken token = LoopPrefs.getAccessToken(getActivity());
        vimeoPlayerService = ServiceGenerator.createService(
                VimeoPlayerService.class,
                VimeoPlayerService.BASE_URL,
                token);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_video_player, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mediaController = new MediaController(getActivity());
        mediaController.setAnchorView(videoView);
        mediaController.setMediaPlayer(videoView);
        videoView.setMediaController(mediaController);

        setUpSystemUiControls();

        VideoSavedState videoSavedState = getVideoSavedState();
        if(videoSavedState != null && !TextUtils.isEmpty(videoSavedState.getVideoUrl())){
            String videoUrl = videoSavedState.getVideoUrl();
            int currentPosition = videoSavedState.getCurrentPosition();
            playVideo(videoUrl, currentPosition);
        } else {
            Call getVideoConfigCall = vimeoPlayerService.getVideoConfig(videoId);
            calls.add(getVideoConfigCall);
            getVideoConfigCall.enqueue(getVideoConfigCallback);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!videoView.isPlaying())
            videoView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();

        if(!TextUtils.isEmpty(videoUrl)){
            VideoSavedState videoSavedState = new VideoSavedState();
            videoSavedState.setVideoUrl(videoUrl);
            videoSavedState.setCurrentPosition(videoView.getCurrentPosition());
            setVideoSavedState(videoSavedState);
        }

        if (videoView.isPlaying())
            videoView.suspend();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.stopPlayback();
//            mVideoView.setVisibility(View.GONE);
        }
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
    private void setUpSystemUiControls(){
        final View decorView = getActivity().getWindow().getDecorView();
//        final int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN |
//                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
//                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

        final int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

        decorView.setSystemUiVisibility(uiOptions);

        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        // Note that system bars will only be "visible" if none of the
                        // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            // TODO: The system bars are visible. Make any desired
                            // adjustments to your UI, such as showing the action bar or
                            // other navigational controls.
                            Timber.d("onSystemUiVisibilityChange() : system bars VISIBLE");

                            mediaController.show(3000);

                            new Handler().postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    decorView.setSystemUiVisibility(uiOptions);

//                                    // Remember that you should never show the action bar if the
//                                    // status bar is hidden, so hide that too if necessary.
//                                    ActionBar actionBar = getActionBar();
//                                    actionBar.hide();
                                }
                            }, 3000);
                        } else {
                            // TODO: The system bars are NOT visible. Make any desired
                            // adjustments to your UI, such as hiding the action bar or
                            // other navigational controls.
                            Timber.d("onSystemUiVisibilityChange() : system bars NOT VISIBLE");
                            mediaController.hide();
                        }
                    }
                });
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

    private void playVideo(String videoUrl, int currentPosition) {
        videoView.setVideoPath(videoUrl);

        videoView.requestFocus();
        videoView.seekTo(currentPosition);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer arg0) {
                loadingImageView.setVisibility(View.GONE);
                videoView.start();

//                mVideoView.requestFocus();
            }
        });
    }

    public void setVideoSavedState(VideoSavedState videoSavedState) {
        videoSavedState = videoSavedState;
    }

    public VideoSavedState getVideoSavedState() {
        return videoSavedState;
    }
    // endregion
}
