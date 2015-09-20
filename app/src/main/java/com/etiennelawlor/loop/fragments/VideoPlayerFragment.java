package com.etiennelawlor.loop.fragments;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.helper.PreferencesHelper;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoPlayerService;
import com.etiennelawlor.loop.network.models.AccessToken;
import com.etiennelawlor.loop.network.models.Files;
import com.etiennelawlor.loop.network.models.H264;
import com.etiennelawlor.loop.network.models.HLS;
import com.etiennelawlor.loop.network.models.Request;
import com.etiennelawlor.loop.network.models.VP6;
import com.etiennelawlor.loop.network.models.VideoConfig;
import com.etiennelawlor.loop.network.models.VideoFormat;
import com.etiennelawlor.loop.otto.BusProvider;
import com.etiennelawlor.loop.ui.LoadingImageView;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class VideoPlayerFragment extends BaseFragment {

    // region Constants
    // endregion

    // region Member Variables
    private Long mVideoId;
    private String mVideoUrl;
    private VimeoPlayerService mVimeoPlayerService;

    @Bind(R.id.vv)
    VideoView mVideoView;
//    @Bind(R.id.pb)
//    ProgressBar mProgressBar;
    @Bind(R.id.loading_iv)
    LoadingImageView mLoadingImageView;
    // endregion

    // region Callbacks
    private Callback<VideoConfig> mGetVideoConfigCallback = new Callback<VideoConfig>() {
        @Override
        public void onResponse(Response<VideoConfig> response) {
            Timber.d("onResponse()");

            if (response != null) {
                if(response.isSuccess()){
                    VideoConfig videoConfig = response.body();
                    if (videoConfig != null) {
                        mVideoUrl = getVideoUrl(videoConfig);
                        Timber.d("onResponse() : videoUrl - " + mVideoUrl);

                        if (!TextUtils.isEmpty(mVideoUrl)) {
                            Timber.d("playVideo()");
                            playVideo(mVideoUrl);
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
            Timber.e("onFailure()");

            if (t != null) {
                Throwable cause = t.getCause();
                String message = t.getMessage();

                if (cause != null) {
                    Timber.e("onFailure() : cause.toString() -" + cause.toString());
                }

                if (TextUtils.isEmpty(message)) {
                    Timber.e("onFailure() : message - " + message);
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
    public static VideoPlayerFragment newInstance() {
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        return fragment;
    }

    public static VideoPlayerFragment newInstance(Bundle extras) {
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        fragment.setArguments(extras);
        return fragment;
    }

    public VideoPlayerFragment() {
    }
    // endregion

    // region Lifecycle Methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BusProvider.get().register(this);

        if(getArguments() != null){
            mVideoId = getArguments().getLong("video_id");
        }

        AccessToken token = PreferencesHelper.getAccessToken(getActivity());
        mVimeoPlayerService = ServiceGenerator.createService(
                VimeoPlayerService.class,
                VimeoPlayerService.BASE_URL,
                token);

        setHasOptionsMenu(true);

//        View decorView = getActivity().getWindow().getDecorView();
//// Hide the status bar.
//        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);

// Remember that you should never show the action bar if the
// status bar is hidden, so hide that too if necessary.
//        ActionBar actionBar = getActionBar();
//        actionBar.hide();
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



        Call getVideoConfigCall = mVimeoPlayerService.getVideoConfig(mVideoId);
        mCalls.add(getVideoConfigCall);
        getVideoConfigCall.enqueue(mGetVideoConfigCallback);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mVideoView.isPlaying())
            mVideoView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mVideoView.isPlaying())
            mVideoView.suspend();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.stopPlayback();
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

                    String h264VideoUrl = getH264VideoUrl(h264);
                    String vp6VideoUrl = getVP6VideoUrl(vp6);
                    String hlsVideoUrl = getHLSVideoUrl(hls);

                    if (!TextUtils.isEmpty(h264VideoUrl)) {
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
            String all = hls.getAll();
            videoUrl = all;
        }
        return videoUrl;
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

    private void playVideo(String videoUrl) {
        mVideoView.setVideoPath(videoUrl);

        MediaController controller = new MediaController(getActivity());
        controller.setAnchorView(mVideoView);
        controller.setMediaPlayer(mVideoView);
        mVideoView.setMediaController(controller);

        mVideoView.setVisibility(View.VISIBLE);
        mLoadingImageView.setVisibility(View.VISIBLE);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer arg0) {
                mLoadingImageView.setVisibility(View.GONE);
                mVideoView.start();
//                mVideoThumbnailImageView.setVisibility(View.GONE);
                mVideoView.requestFocus();
            }
        });
    }
    // endregion
}
