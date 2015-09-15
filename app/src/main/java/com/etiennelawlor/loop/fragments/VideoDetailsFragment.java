package com.etiennelawlor.loop.fragments;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.activities.VideoDetailsActivity;
import com.etiennelawlor.loop.adapters.VideosAdapter;
import com.etiennelawlor.loop.helper.PreferencesHelper;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoPlayerService;
import com.etiennelawlor.loop.network.models.AccessToken;
import com.etiennelawlor.loop.network.models.Files;
import com.etiennelawlor.loop.network.models.H264;
import com.etiennelawlor.loop.network.models.HLS;
import com.etiennelawlor.loop.network.models.Pictures;
import com.etiennelawlor.loop.network.models.Request;
import com.etiennelawlor.loop.network.models.Size;
import com.etiennelawlor.loop.network.models.Stats;
import com.etiennelawlor.loop.network.models.Tag;
import com.etiennelawlor.loop.network.models.User;
import com.etiennelawlor.loop.network.models.VP6;
import com.etiennelawlor.loop.network.models.Video;
import com.etiennelawlor.loop.network.models.VideoConfig;
import com.etiennelawlor.loop.network.models.VideoFormat;
import com.etiennelawlor.loop.network.models.VideoWrapper;
import com.etiennelawlor.loop.network.models.VideosCollection;
import com.etiennelawlor.loop.otto.BusProvider;
import com.etiennelawlor.loop.ui.CustomMediaController;
import com.etiennelawlor.loop.utilities.LoopUtility;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class VideoDetailsFragment extends BaseFragment implements VideosAdapter.OnItemClickListener {

    // region Member Variables
    @Bind(R.id.vv)
    VideoView mVideoView;
    @Bind(R.id.pb)
    ProgressBar mProgressBar;
    @Bind(R.id.video_thumbnail_iv)
    ImageView mVideoThumbnailImageView;
    @Bind(R.id.title_tv)
    TextView mTitleTextView;
    @Bind(R.id.subtitle_tv)
    TextView mSubtitleTextView;
    @Bind(R.id.user_iv)
    CircleImageView mUserImageView;
    //    @InjectView(R.id.uploaded_tv)
//    TextView mUploadedTextView;
    @Bind(R.id.view_count_tv)
    TextView mViewCountTextView;
    @Bind(R.id.upload_date_tv)
    TextView mUploadDateTextView;
    @Bind(R.id.tags_tv)
    TextView mTagsTextView;
    @Bind(R.id.description_tv)
    TextView mDescriptionTextView;
    @Bind(R.id.additional_info_ll)
    LinearLayout mAdditionalInfoLinearLayout;
    @Bind(R.id.action_iv)
    ImageView mActionImageView;

//    @InjectView(R.id.videos_rv)
//    RecyclerView mVideosRecyclerView;

    private boolean isExpanded = false;
    private Video mVideo;
    private VideosAdapter mVideosAdapter;
    private String mVideoUrl;
    private VimeoPlayerService mVimeoPlayerService;
    // endregion

    // region Listeners
    @OnClick(R.id.action_iv)
    public void onActionImageViewClicked() {
        if (!isExpanded) {
            isExpanded = true;
            mAdditionalInfoLinearLayout.setVisibility(View.VISIBLE);
            mActionImageView.setImageResource(R.drawable.ic_collapse);
        } else {
            isExpanded = false;
            mAdditionalInfoLinearLayout.setVisibility(View.GONE);
            mActionImageView.setImageResource(R.drawable.ic_expand);
        }
    }

    // endregion

    // region Callbacks
    private Callback<VideoConfig> mGetVideoConfigCallback = new Callback<VideoConfig>() {
        @Override
        public void onResponse(Response<VideoConfig> response) {
            Timber.d("onResponse()");

            if (response != null) {
                VideoConfig videoConfig = response.body();
                com.squareup.okhttp.Response rawResponse = response.raw();

                if (videoConfig != null) {
                    mVideoUrl = getVideoUrl(videoConfig);
                    Timber.d("onResponse() : videoUrl - " + mVideoUrl);

                    if (!TextUtils.isEmpty(mVideoUrl)) {
                        Timber.d("playVideo()");
                        playVideo(mVideoUrl);
                    }
                } else if (rawResponse != null) {
                    String message = rawResponse.message();
                    int code = rawResponse.code();
                    Timber.d("onResponse() : message - " + message);
                    Timber.d("onResponse() : code - " + code);

                    switch (code) {
                        case 500:
//                            mErrorTextView.setText("Can't load data.\nCheck your network connection.");
//                            mErrorLinearLayout.setVisibility(View.VISIBLE);
                            break;
                        default:
                            break;
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
            }

        }
    };

    private Callback<VideosCollection> mGetRelatedVideosCallback = new Callback<VideosCollection>() {
        @Override
        public void onResponse(Response<VideosCollection> response) {
            Timber.d("onResponse()");

            VideosCollection videosCollection = response.body();
            com.squareup.okhttp.Response rawResponse = response.raw();

            if (videosCollection != null) {
                List<Video> videos = videosCollection.getVideos();
                if (videos != null) {
                    mVideosAdapter.addAll(videos);
                }
            } else if (rawResponse != null) {
                String message = rawResponse.message();
                int code = rawResponse.code();
                Timber.d("onResponse() : message - " + message);
                Timber.d("onResponse() : code - " + code);

                switch (code) {
                    case 500:
//                        mErrorTextView.setText("Can't load data.\nCheck your network connection.");
//                        mErrorLinearLayout.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
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
        mVimeoPlayerService = ServiceGenerator.createService(
                VimeoPlayerService.class,
                VimeoPlayerService.BASE_URL,
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

//        int width = LoopUtility.getScreenWidth(getActivity());
//        mVideoView.getHolder().setFixedSize(width, LoopUtility.dp2px(getActivity(), 202));

        if (mVideo != null) {
            setUpTitle();
            setUpSubtitle();
            setUpUserImage();
            setUpDescription();
            setUpVideoThumbnail();
            setUpViewCount();
            setUpUploadedDate();
            setUpTags();

            String uri = mVideo.getUri();
            if (!TextUtils.isEmpty(uri)) {
                String lastPathSegment = Uri.parse(uri).getLastPathSegment();
                Long videoId = Long.parseLong(lastPathSegment);

                Call getVideoConfigCall = mVimeoPlayerService.getVideoConfig(videoId);
                mCalls.add(getVideoConfigCall);
                getVideoConfigCall.enqueue(mGetVideoConfigCallback);

//                final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
//                mVideosRecyclerView.setLayoutManager(layoutManager);
//                mVideosAdapter = new VideosAdapter(getActivity());
//                mVideosAdapter.setOnItemClickListener(this);
//
//                mVideosRecyclerView.setAdapter(mVideosAdapter);
//                Api.getService(Api.getEndpointUrl()).findRelatedVideos(videoId, 1, 10, mGetRelatedVideosCallback);
            }
        }

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
            mVideoView.setVisibility(View.GONE);
        }
    }
    // endregion

    // region VideosAdapter.OnItemClickListener Methods
    @Override
    public void onItemClick(int position, View view) {
        VideoWrapper videoWrapper = mVideosAdapter.getItem(position);
        if (videoWrapper != null) {
            Video video = videoWrapper.getVideo();
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
    }
    // endregion

    // region Helper Methods
    private void setUpTitle() {
        String name = mVideo.getName();
        if (!TextUtils.isEmpty(name)) {
            mTitleTextView.setText(name);
        }
    }

    private void setUpSubtitle() {
        User user = mVideo.getUser();
        if (user != null) {
            String userName = user.getName();
            if (!TextUtils.isEmpty(userName)) {
                mSubtitleTextView.setText(userName);
            }
        }
    }

    private void setUpUserImage() {
        boolean isPictureAvailable = false;

        User user = mVideo.getUser();
        if (user != null) {

            Pictures pictures = user.getPictures();
            if (pictures != null) {
                List<Size> sizes = pictures.getSizes();
                if (sizes != null && sizes.size() > 0) {
                    Size size = sizes.get(sizes.size() - 1);
                    if (size != null) {
                        String link = size.getLink();
                        if (!TextUtils.isEmpty(link)) {
                            isPictureAvailable = true;
                            Glide.with(getActivity())
                                    .load(link)
//                                .placeholder(R.drawable.ic_placeholder)
//                                .error(R.drawable.ic_error)
                                    .into(mUserImageView);
                        }
                    }
                }
            }
        }

        if (!isPictureAvailable) {
            mUserImageView.setImageResource(R.drawable.ic_loop);
        }
    }

    private void setUpDescription() {
        String description = mVideo.getDescription();
        if (!TextUtils.isEmpty(description)) {
            mDescriptionTextView.setText(description.trim());
            mDescriptionTextView.setVisibility(View.VISIBLE);
        } else {
            mDescriptionTextView.setVisibility(View.GONE);
        }
    }

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

    private void setUpViewCount() {
        int viewCount = 0;
        Stats stats = mVideo.getStats();
        if (stats != null) {
            viewCount = stats.getPlays();
        }

        if (viewCount > 0) {
            String formattedViewCount = NumberFormat.getNumberInstance(Locale.US).format(viewCount);
//                String formattedViewCount = formatViewCount(viewCount);
            if (viewCount > 1) {
                mViewCountTextView.setText(String.format("%s views", formattedViewCount));
            } else {
                mViewCountTextView.setText(String.format("%s view", formattedViewCount));
            }
        }
    }

    private void setUpUploadedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ", Locale.ENGLISH);
        String uploadDate = "";

        String createdTime = mVideo.getCreatedTime();

        try {
            Date date = sdf.parse(createdTime);

            Calendar futureCalendar = Calendar.getInstance();
            futureCalendar.setTime(date);

            uploadDate = LoopUtility.getRelativeDate(futureCalendar);
        } catch (ParseException e) {
            Timber.e("");
        }

        if (!TextUtils.isEmpty(uploadDate)) {
            mUploadDateTextView.setText(String.format("Uploaded %s", uploadDate));
            mUploadDateTextView.setVisibility(View.VISIBLE);
        } else {
            mUploadDateTextView.setVisibility(View.GONE);
        }
    }

    private void setUpTags() {
        List<Tag> tags = mVideo.getTags();
        if (tags != null && tags.size() > 0) {
            String tagString = "";
            for (Tag tag : tags) {
                tagString += String.format("#%s ", tag.getCanonical());
            }

            if (!TextUtils.isEmpty(tagString)) {
                mTagsTextView.setText(tagString);
                mTagsTextView.setVisibility(View.VISIBLE);
            } else {
                mTagsTextView.setVisibility(View.GONE);
            }
        }
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

    private String formatViewCount(int viewCount) {
        String formattedViewCount = "";

        if (viewCount < 1000000000 && viewCount >= 1000000) {
            formattedViewCount = String.format("%dM views", viewCount / 1000000);
        } else if (viewCount < 1000000 && viewCount >= 1000) {
            formattedViewCount = String.format("%dK views", viewCount / 1000);
        } else if (viewCount < 1000 && viewCount > 1) {
            formattedViewCount = String.format("%d views", viewCount);
        } else if (viewCount == 1) {
            formattedViewCount = String.format("%d view", viewCount);
        }

        return formattedViewCount;
    }

    private void playVideo(String videoUrl) {
        mVideoView.setVideoPath(videoUrl);

        MediaController controller = new MediaController(getActivity());
        controller.setAnchorView(mVideoView);
        controller.setMediaPlayer(mVideoView);
        mVideoView.setMediaController(controller);

        mVideoView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer arg0) {
                mProgressBar.setVisibility(View.GONE);
                mVideoView.start();
                mVideoThumbnailImageView.setVisibility(View.GONE);
                mVideoView.requestFocus();
            }
        });
    }
    // endregion
}
