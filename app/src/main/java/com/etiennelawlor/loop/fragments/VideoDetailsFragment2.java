package com.etiennelawlor.loop.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.activities.VideoDetailsActivity;
import com.etiennelawlor.loop.adapters.VideosAdapter;
import com.etiennelawlor.loop.helper.PreferencesHelper;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoPlayerService;
import com.etiennelawlor.loop.network.models.AccessToken;
import com.etiennelawlor.loop.network.models.Embed;
import com.etiennelawlor.loop.network.models.Video;
import com.etiennelawlor.loop.otto.BusProvider;
import com.etiennelawlor.loop.utilities.LoopUtility;
//import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class VideoDetailsFragment2 extends BaseFragment implements VideosAdapter.OnItemClickListener {

    // region Member Variables
    @Bind(R.id.wv)
    WebView mWebView;
//    @InjectView(R.id.vv)
//    VideoView mVideoView;
//    @InjectView(R.id.pb)
//    ProgressBar mProgressBar;
//    @InjectView(R.id.video_thumbnail_iv)
//    ImageView mVideoThumbnailImageView;
//    @InjectView(R.id.title_tv)
//    TextView mTitleTextView;
//    @InjectView(R.id.subtitle_tv)
//    TextView mSubtitleTextView;
//    @InjectView(R.id.user_iv)
//    ImageView mUserImageView;
//    //    @InjectView(R.id.uploaded_tv)
////    TextView mUploadedTextView;
//    @InjectView(R.id.view_count_tv)
//    TextView mViewCountTextView;
//    @InjectView(R.id.upload_date_tv)
//    TextView mUploadDateTextView;
//    @InjectView(R.id.tags_tv)
//    TextView mTagsTextView;
//    @InjectView(R.id.description_tv)
//    TextView mDescriptionTextView;
//    @InjectView(R.id.additional_info_ll)
//    LinearLayout mAdditionalInfoLinearLayout;
//    @InjectView(R.id.action_iv)
//    ImageView mActionImageView;

//    @InjectView(R.id.videos_rv)
//    RecyclerView mVideosRecyclerView;

    private boolean isExpanded = false;
    private Video mVideo;
    private VideosAdapter mVideosAdapter;
    private VimeoPlayerService mVimeoPlayerService;
    // endregion

    // region Listeners
//    @OnClick(R.id.action_iv)
//    public void onActionImageViewClicked() {
//        if (!isExpanded) {
//            isExpanded = true;
//            mAdditionalInfoLinearLayout.setVisibility(View.VISIBLE);
//            mActionImageView.setImageResource(R.drawable.ic_collapse);
//        } else {
//            isExpanded = false;
//            mAdditionalInfoLinearLayout.setVisibility(View.GONE);
//            mActionImageView.setImageResource(R.drawable.ic_expand);
//        }
//    }

    // endregion

    // region Callbacks
//    private Callback<VideoConfig> mGetVideoConfigCallback = new Callback<VideoConfig>() {
//        @Override
//        public void success(VideoConfig videoConfig, Response response) {
//            if (isAdded() && isResumed()) {
//                Timber.d("");
//
//                String videoUrl = getVideoUrl(videoConfig);
//
//                if (!TextUtils.isEmpty(videoUrl)) {
//                    mVideoView.setVideoPath(videoUrl);
//
//                    MediaController controller = new MediaController(getActivity());
//                    controller.setAnchorView(mVideoView);
//                    controller.setMediaPlayer(mVideoView);
//                    mVideoView.setMediaController(controller);
//
////                                    mVideoView.start();
//
//                    mVideoView.setVisibility(View.VISIBLE);
//
//                    FrameLayout.LayoutParams videoviewlp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
////                                            videoviewlp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
////                                            videoviewlp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
//                    mVideoView.setLayoutParams(videoviewlp);
//                    mVideoView.invalidate();
//
//                    mProgressBar.setVisibility(View.VISIBLE);
//                    mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                        @Override
//                        public void onPrepared(MediaPlayer arg0) {
//                            mProgressBar.setVisibility(View.GONE);
//                            mVideoView.start();
//                            mVideoThumbnailImageView.setVisibility(View.GONE);
//
//
//                            mVideoView.requestFocus();
//
//                        }
//                    });
//
//                }
//            }
//        }
//
//        @Override
//        public void failure(RetrofitError error) {
//            if (isAdded() && isResumed()) {
//                Timber.d("");
//            }
//        }
//    };

//    private Callback<VideosCollection> mGetRelatedVideosCallback = new Callback<VideosCollection>() {
//        @Override
//        public void success(VideosCollection videosCollection, Response response) {
//            if (isAdded() && isResumed()) {
//                Timber.d("");
//                if (videosCollection != null) {
//                    List<Video> videos = videosCollection.getVideos();
//                    if (videos != null) {
//                        mVideosAdapter.addAll(videos);
//                    }
//                }
//            }
//        }
//
//        @Override
//        public void failure(RetrofitError error) {
//            if (isAdded() && isResumed()) {
//                Timber.d("");
//            }
//        }
//    };
    // endregion

    // region Constructors
    public static VideoDetailsFragment2 newInstance(Bundle extras) {
        VideoDetailsFragment2 fragment = new VideoDetailsFragment2();
        fragment.setArguments(extras);
        return fragment;
    }

    public static VideoDetailsFragment2 newInstance() {
        VideoDetailsFragment2 fragment = new VideoDetailsFragment2();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public VideoDetailsFragment2() {
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
        View rootView = inflater.inflate(R.layout.fragment_video_details2, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int width = LoopUtility.getScreenWidth(getActivity());
        int height = (int) (width * (9D/16D));

        Timber.d("width - %d : height - %d", width, height);

//        mWebView.setLayoutParams(
//                new LinearLayout.LayoutParams(
//                        LoopUtility.dp2px(getActivity(), width),
//                        LoopUtility.dp2px(getActivity(), height)));

        mWebView.setLayoutParams(
                new LinearLayout.LayoutParams(width,
                                            height));

        if (mVideo != null) {
//            setUpTitle();
//            setUpSubtitle();
//            setUpUserImage();
//            setUpDescription();
//            setUpVideoThumbnail();
//            setUpViewCount();
//            setUpUploadedDate();
//            setUpTags();
//
//            String uri = mVideo.getUri();
//            if (!TextUtils.isEmpty(uri)) {
//                String lastPathSegment = Uri.parse(uri).getLastPathSegment();
//                Long videoId = Long.parseLong(lastPathSegment);
//
//                mVimeoPlayerService.getVideoConfig(videoId, mGetVideoConfigCallback);
//
////                final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
////                mVideosRecyclerView.setLayoutManager(layoutManager);
////                mVideosAdapter = new VideosAdapter(getActivity());
////                mVideosAdapter.setOnItemClickListener(this);
////
////                mVideosRecyclerView.setAdapter(mVideosAdapter);
////                Api.getService(Api.getEndpointUrl()).findRelatedVideos(videoId, 1, 10, mGetRelatedVideosCallback);
//            }


            Embed embed = mVideo.getEmbed();
            String html = embed.getHtml();
            Timber.d("html - "+html);

//            String regex = "[a-z]{5,5}=\"d+\"";
//
//            Pattern pattern = Pattern.compile(regex);
//            Matcher matcher = pattern.matcher(html);
//            if (matcher.find()) {
//
//                int start = matcher.start();
//                int end = matcher.end();
//
//                Timber.d(String.format("start - %d : end - %d", start, end));
//            }

//            html = "<iframe src=\"https://player.vimeo.com/video/1793990?title=0&byline=0&portrait=0&badge=0&autopause=0&player_id=0\" width=\"504\" height=\"292\" frameborder=\"0\" title=\"Bodyboarding\" webkitallowfullscreen mozallowfullscreen allowfullscreen></iframe>";
            html = "<iframe src=\"https://player.vimeo.com/video/1793990?title=0&byline=0&portrait=0&badge=0&autopause=0&player_id=0\" style=\"margin: 0 0 0 0\" width=\"360\" height=\"202\" frameborder=\"0\" title=\"Bodyboarding\" webkitallowfullscreen mozallowfullscreen allowfullscreen></iframe>";


//            mWebView.setWebChromeClient(new WebChromeClient());

//            mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
//            mWebView.getSettings().setPluginState(WebSettings.PluginState.ON_DEMAND);

            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView webView, String url) {
                    super.onPageFinished(webView, url);
                    Timber.d(String.format("onViewCreated() : width - %d : height - %d", mWebView.getMeasuredWidth(), mWebView.getMeasuredHeight()));
                    Timber.d(String.format("onViewCreated() :  width - %d : height - %d", mWebView.getWidth(), mWebView.getHeight()));
//                    Timber.d(String.format("onViewCreated() : widthPixes - %d : density - %d", getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().density));

//                    mWebView.setLayoutParams(
//                            new LinearLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels, (int) (mWebView.getHeight() * getResources().getDisplayMetrics().density)));
                }
            });

            mWebView.getSettings().setJavaScriptEnabled(true);

//            mWebView.loadUrl(html);

            mWebView.loadDataWithBaseURL("http://vimeo.com", html, "text/html", "UTF-8", null);

        }

    }

    @Override
    public void onResume() {
        super.onResume();
//        if (!mVideoView.isPlaying())
//            mVideoView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
//        if (mVideoView.isPlaying())
//            mVideoView.suspend();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (mVideoView != null) {
//            mVideoView.stopPlayback();
//            mVideoView.setVisibility(View.GONE);
//        }
    }
    // endregion

    @Override
    public void onItemClick(int position, View view) {
//        VideoWrapper videoWrapper = mVideosAdapter.getItem(position);
//        if (videoWrapper != null) {
//            Video video = videoWrapper.getVideo();
//            if (video != null) {
//                Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
//
//                Bundle bundle = new Bundle();
//                bundle.putParcelable("video", video);
//                intent.putExtras(bundle);
//
//                Pair<View, String> p1 = Pair.create((View) view.findViewById(R.id.video_thumbnail_iv), "videoTransition");
////                Pair<View, String> p2 = Pair.create((View) view.findViewById(R.id.title_tv), "titleTransition");
////                Pair<View, String> p3 = Pair.create((View) view.findViewById(R.id.subtitle_tv), "subtitleTransition");
////        Pair<View, String> p4 = Pair.create((View)view.findViewById(R.id.uploaded_tv), "uploadedTransition");
//
////                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
////                        p1, p2, p3);
//
//                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
//                        p1);
//
//
//                ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
//
////        startActivity(intent);
//            }
//        }

        Video video = mVideosAdapter.getItem(position);
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

    // region Helper Methods
//    private void setUpTitle() {
//        String name = mVideo.getName();
//        if (!TextUtils.isEmpty(name)) {
//            mTitleTextView.setText(name);
//        }
//    }
//
//    private void setUpSubtitle() {
//        User user = mVideo.getUser();
//        if (user != null) {
//            String userName = user.getName();
//            if (!TextUtils.isEmpty(userName)) {
//                mSubtitleTextView.setText(userName);
//            }
//        }
//    }
//
//    private void setUpUserImage() {
//        boolean isPictureAvailable = false;
//
//        User user = mVideo.getUser();
//        if (user != null) {
//
//            Pictures pictures = user.getPictures();
//            if (pictures != null) {
//                List<Size> sizes = pictures.getSizes();
//                if (sizes != null && sizes.size() > 0) {
//                    Size size = sizes.get(sizes.size() - 1);
//                    if (size != null) {
//                        String link = size.getLink();
//                        if (!TextUtils.isEmpty(link)) {
//                            isPictureAvailable = true;
//                            Picasso.with(getActivity())
//                                    .load(link)
////                                .placeholder(R.drawable.ic_placeholder)
////                                .error(R.drawable.ic_error)
//                                    .into(mUserImageView);
//                        }
//                    }
//                }
//            }
//        }
//
//        if (!isPictureAvailable) {
//            mUserImageView.setImageResource(R.drawable.ic_loop);
//        }
//    }
//
//    private void setUpDescription() {
//        String description = mVideo.getDescription();
//        if (!TextUtils.isEmpty(description)) {
//            mDescriptionTextView.setText(description);
//            mDescriptionTextView.setVisibility(View.VISIBLE);
//        } else {
//            mDescriptionTextView.setVisibility(View.GONE);
//        }
//    }
//
//    private void setUpVideoThumbnail() {
//        Pictures pictures = mVideo.getPictures();
//        if (pictures != null) {
//            List<Size> sizes = pictures.getSizes();
//            if (sizes != null && sizes.size() > 0) {
//                Size size = sizes.get(sizes.size() - 1);
//                if (size != null) {
//                    String link = size.getLink();
//                    if (!TextUtils.isEmpty(link)) {
//                        Picasso.with(getActivity())
//                                .load(link)
////                                .placeholder(R.drawable.ic_placeholder)
////                                .error(R.drawable.ic_error)
//                                .into(mVideoThumbnailImageView);
//                    }
//                }
//            }
//        }
//    }
//
//    private void setUpViewCount() {
//        int viewCount = 0;
//        Stats stats = mVideo.getStats();
//        if (stats != null) {
//            viewCount = stats.getPlays();
//        }
//
//        if (viewCount > 0) {
//            String formattedViewCount = NumberFormat.getNumberInstance(Locale.US).format(viewCount);
////                String formattedViewCount = formatViewCount(viewCount);
//            if (viewCount > 1) {
//                mViewCountTextView.setText(String.format("%s views", formattedViewCount));
//            } else {
//                mViewCountTextView.setText(String.format("%s view", formattedViewCount));
//            }
//        }
//    }
//
//    private void setUpUploadedDate() {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ", Locale.ENGLISH);
//        String uploadDate = "";
//
//        String createdTime = mVideo.getCreatedTime();
//
//        try {
//            Date date = sdf.parse(createdTime);
//
//            Calendar futureCalendar = Calendar.getInstance();
//            futureCalendar.setTime(date);
//
//            uploadDate = LoopUtility.getRelativeDate(futureCalendar);
//        } catch (ParseException e) {
//            Timber.e("");
//        }
//
//        mUploadDateTextView.setText(String.format("Uploaded %s", uploadDate));
//    }
//
//    private void setUpTags() {
//        List<Tag> tags = mVideo.getTags();
//        if (tags != null && tags.size() > 0) {
//            String tagString = "";
//            for (Tag tag : tags) {
//                tagString += String.format("#%s ", tag.getCanonical());
//            }
//
//            mTagsTextView.setText(tagString);
//        }
//    }
//
//    private String getVideoUrl(VideoConfig videoConfig){
//        String videoUrl = "";
//
//        if (videoConfig != null) {
//            Request request = videoConfig.getRequest();
//            if (request != null) {
//                Files files = request.getFiles();
//                if (files != null) {
//                    H264 h264 = files.getH264();
//                    if (h264 != null) {
//                        VideoFormat hdVideoFormat = h264.getHd();
//                        VideoFormat sdVideoFormat = h264.getSd();
//                        VideoFormat mobleVideoFormat = h264.getMoble();
//
//                        int width = -1;
//                        int height = -1;
//                        if (hdVideoFormat != null) {
//                            videoUrl = hdVideoFormat.getUrl();
//                            width = hdVideoFormat.getWidth();
//                            height = hdVideoFormat.getHeight();
//                        } else if (sdVideoFormat != null) {
//                            videoUrl = sdVideoFormat.getUrl();
//                            width = sdVideoFormat.getWidth();
//                            height = sdVideoFormat.getHeight();
//                        } else if (mobleVideoFormat != null) {
//                            videoUrl = mobleVideoFormat.getUrl();
//                            width = mobleVideoFormat.getWidth();
//                            height = mobleVideoFormat.getHeight();
//                        }
//
//                        Timber.d(String.format("mGetVideoConfigCallback : url - %s", videoUrl));
//                        Timber.d(String.format("mGetVideoConfigCallback : width - %d : height - %d", width, height));
//                    }
//                }
//            }
//        }
//
//        return videoUrl;
//    }
//
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
