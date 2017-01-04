package com.etiennelawlor.loop.fragments;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.etiennelawlor.loop.EventMapKeys;
import com.etiennelawlor.loop.EventNames;
import com.etiennelawlor.loop.LoopApplication;
import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.activities.SearchableActivity;
import com.etiennelawlor.loop.activities.VideoCommentsActivity;
import com.etiennelawlor.loop.analytics.Event;
import com.etiennelawlor.loop.analytics.EventLogger;
import com.etiennelawlor.loop.bus.RxBus;
import com.etiennelawlor.loop.bus.events.LikeVideoClickedEvent;
import com.etiennelawlor.loop.bus.events.SearchPerformedEvent;
import com.etiennelawlor.loop.bus.events.WatchLaterClickedEvent;
import com.etiennelawlor.loop.models.AccessToken;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoService;
import com.etiennelawlor.loop.network.interceptors.AuthorizedNetworkInterceptor;
import com.etiennelawlor.loop.network.models.response.User;
import com.etiennelawlor.loop.network.models.response.Video;
import com.etiennelawlor.loop.prefs.LoopPrefs;
import com.etiennelawlor.loop.ui.AvatarView;
import com.etiennelawlor.loop.utilities.FontCache;
import com.etiennelawlor.loop.utilities.NetworkLogUtility;
import com.etiennelawlor.loop.utilities.NetworkUtility;
import com.etiennelawlor.loop.utilities.Transformers;
import com.etiennelawlor.loop.utilities.TrestleUtility;
import com.greenfrvr.hashtagview.HashtagView;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by etiennelawlor on 1/3/17.
 */

public class VideoDetailsInfoFragment extends BaseFragment {

    // region Constants
    // endregion

    // region Views
    @BindView(R.id.title_tv)
    TextView titleTextView;
    @BindView(R.id.subtitle_tv)
    TextView subtitleTextView;
    @BindView(R.id.user_iv)
    AvatarView userImageView;
    @BindView(R.id.view_count_tv)
    TextView viewCountTextView;
    @BindView(R.id.upload_date_tv)
    TextView uploadDateTextView;
    @BindView(R.id.like_iv)
    ImageView likeImageView;
    @BindView(R.id.watch_later_iv)
    ImageView watchLaterImageView;
    @BindView(R.id.comments_iv)
    ImageView commentsImageView;
    @BindView(R.id.info_iv)
    ImageView infoImageView;
    @BindView(R.id.htv)
    HashtagView hashtagView;
    @BindView(R.id.description_tv)
    TextView descriptionTextView;
    @BindView(R.id.additional_info_ll)
    LinearLayout additionalInfoLinearLayout;
    // endregion

    // region Member Variables
    private Video video;
    private boolean isLikeOn = false;
    private boolean isWatchLaterOn = false;
    private boolean hasDescription = false;
    private boolean hasTags = false;
    private VimeoService vimeoService;
    private Unbinder unbinder;
    private Typeface font;
    private Typeface boldFont;
    private CompositeSubscription compositeSubscription;
    // endregion

    // region Listeners
    @OnClick(R.id.info_iv)
    public void onInfoImageViewClicked(final View v) {
        int visibility = additionalInfoLinearLayout.getVisibility();
        if(visibility == View.VISIBLE){
            additionalInfoLinearLayout.setVisibility(View.GONE);
            v.animate().rotation(0.0f).setDuration(300).start();
        } else if(visibility == View.GONE){
            additionalInfoLinearLayout.setVisibility(View.VISIBLE);
            v.animate().rotation(180.0f).setDuration(300).start();
        }
    }

    @OnClick(R.id.comments_iv)
    public void onCommentsImageViewClicked(final View v) {
        Intent intent = new Intent(getActivity(), VideoCommentsActivity.class);

        Bundle bundle = new Bundle();
        bundle.putParcelable(LikedVideosFragment.KEY_VIDEO, video);
        intent.putExtras(bundle);

        startActivity(intent);
    }

    @OnClick(R.id.watch_later_iv)
    public void onWatchLaterImageViewClicked(final View v) {
        if (isWatchLaterOn) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
            alertDialogBuilder.setMessage("Are you sure you want to remove this video from your Watch Later collection?");
            alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Call removeVideoFromWatchLaterCall = vimeoService.removeVideoFromWatchLater(String.valueOf(video.getId()));
                    calls.add(removeVideoFromWatchLaterCall);
                    removeVideoFromWatchLaterCall.enqueue(removeVideoFromWatchLaterCallback);
                }
            });
            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });
            alertDialogBuilder.show();
        } else {
            Call addVideoToWatchLaterCall = vimeoService.addVideoToWatchLater(String.valueOf(video.getId()));
            calls.add(addVideoToWatchLaterCall);
            addVideoToWatchLaterCall.enqueue(addVideoToWatchLaterCallback);
        }
    }

    @OnClick(R.id.like_iv)
    public void onLikeImageViewClicked(final View v) {
        if (isLikeOn) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
            alertDialogBuilder.setMessage("Are you sure you want to unlike this video?");
            alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Call unlikeVideoCall = vimeoService.unlikeVideo(String.valueOf(video.getId()));
                    calls.add(unlikeVideoCall);
                    unlikeVideoCall.enqueue(unlikeVideoCallback);
                }
            });
            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });
            alertDialogBuilder.show();
        } else {
            Call likeVideoCall = vimeoService.likeVideo(String.valueOf(video.getId()));
            calls.add(likeVideoCall);
            likeVideoCall.enqueue(likeVideoCallback);
        }
    }
    // endregion

    // region Callbacks
    private Callback<ResponseBody> addVideoToWatchLaterCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (!response.isSuccessful()) {
                int responseCode = response.code();
                if(responseCode == 504) { // 504 Unsatisfiable Request (only-if-cached)
//                    errorTextView.setText("Can't load data.\nCheck your network connection.");
//                    errorLinearLayout.setVisibility(View.VISIBLE);
                }
                return;
            }

            okhttp3.Response rawResponse = response.raw();
            if (rawResponse != null) {
                int code = rawResponse.code();
                switch (code) {
                    case 204:
                        // No Content
                        RxBus.getInstance().send(new WatchLaterClickedEvent());

                        isWatchLaterOn = true;
                        watchLaterImageView.setImageResource(R.drawable.ic_watch_later_on);

                        break;
//                            case 400:
//                                // If the video is owned by the authenticated user
//                                break;
//                            case 403:
//                                // If the authenticated user is not allowed to like videos
//                                break;
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            NetworkLogUtility.logFailure(call, t);

            if (!call.isCanceled()){
                if(NetworkUtility.isKnownException(t)){
                    Snackbar.make(getActivity().findViewById(R.id.main_content),
                            TrestleUtility.getFormattedText("Network connection is unavailable.", font, 16),
                            Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        }
    };

    private Callback<ResponseBody> removeVideoFromWatchLaterCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (!response.isSuccessful()) {
                int responseCode = response.code();
                if(responseCode == 504) { // 504 Unsatisfiable Request (only-if-cached)
//                    errorTextView.setText("Can't load data.\nCheck your network connection.");
//                    errorLinearLayout.setVisibility(View.VISIBLE);
                }
                return;
            }

            okhttp3.Response rawResponse = response.raw();
            if (rawResponse != null) {
                int code = rawResponse.code();
                switch (code) {
                    case 204:
                        // No Content
                        RxBus.getInstance().send(new WatchLaterClickedEvent());

                        isWatchLaterOn = false;
                        watchLaterImageView.setImageResource(R.drawable.ic_watch_later_off);

                        break;
//                            case 403:
//                                // If the authenticated user is not allowed to like videos
//                                break;
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            NetworkLogUtility.logFailure(call, t);

            if (!call.isCanceled()){
                if(NetworkUtility.isKnownException(t)){
                    Snackbar.make(getActivity().findViewById(R.id.main_content),
                            TrestleUtility.getFormattedText("Network connection is unavailable.", font, 16),
                            Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        }
    };


    private Callback<ResponseBody> likeVideoCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            if (!response.isSuccessful()) {
                int responseCode = response.code();
                if(responseCode == 504) { // 504 Unsatisfiable Request (only-if-cached)
//                    errorTextView.setText("Can't load data.\nCheck your network connection.");
//                    errorLinearLayout.setVisibility(View.VISIBLE);
                }
                return;
            }

            okhttp3.Response rawResponse = response.raw();
            if (rawResponse != null) {
                int code = rawResponse.code();
                switch (code) {
                    case 204:
                        // No Content
                        RxBus.getInstance().send(new LikeVideoClickedEvent());

                        HashMap<String, Object> map = new HashMap<>();
                        map.put(EventMapKeys.NAME, video.getName());
                        map.put(EventMapKeys.DURATION, video.getDuration());
                        map.put(EventMapKeys.VIDEO_ID, video.getId());

                        Event event = new Event(EventNames.VIDEO_LIKED, map);
                        EventLogger.logEvent(event);

                        isLikeOn = true;
                        likeImageView.setImageResource(R.drawable.ic_likes_on);

                        break;
                    case 400:
                        // If the video is owned by the authenticated user
                        break;
                    case 403:
                        // If the authenticated user is not allowed to like videos
                        break;
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            NetworkLogUtility.logFailure(call, t);

            if (!call.isCanceled()){
                if(NetworkUtility.isKnownException(t)){
                    Snackbar.make(getActivity().findViewById(R.id.main_content),
                            TrestleUtility.getFormattedText("Network connection is unavailable.", font, 16),
                            Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        }
    };

    private Callback<ResponseBody> unlikeVideoCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            if (!response.isSuccessful()) {
                int responseCode = response.code();
                if(responseCode == 504) { // 504 Unsatisfiable Request (only-if-cached)
//                    errorTextView.setText("Can't load data.\nCheck your network connection.");
//                    errorLinearLayout.setVisibility(View.VISIBLE);
                }
                return;
            }

            okhttp3.Response rawResponse = response.raw();
            if (rawResponse != null) {
                int code = rawResponse.code();
                switch (code) {
                    case 204:
                        // No Content
                        RxBus.getInstance().send(new LikeVideoClickedEvent());

                        HashMap<String, Object> map = new HashMap<>();
                        map.put(EventMapKeys.NAME, video.getName());
                        map.put(EventMapKeys.DURATION, video.getDuration());
                        map.put(EventMapKeys.VIDEO_ID, video.getId());

                        Event event = new Event(EventNames.VIDEO_DISLIKED, map);
                        EventLogger.logEvent(event);

                        isLikeOn = false;
                        likeImageView.setImageResource(R.drawable.ic_likes_off);
                        break;
                    case 403:
                        // If the authenticated user is not allowed to like videos
                        break;
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            NetworkLogUtility.logFailure(call, t);

            if (!call.isCanceled()){
                if(NetworkUtility.isKnownException(t)){
                    Snackbar.make(getActivity().findViewById(R.id.main_content),
                            TrestleUtility.getFormattedText("Network connection is unavailable.", font, 16),
                            Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        }
    };
    // endregion

    // region Constructors
    public VideoDetailsInfoFragment() {
    }
    // endregion

    // region Factory Methods
    public static VideoDetailsInfoFragment newInstance(Bundle extras) {
        VideoDetailsInfoFragment fragment = new VideoDetailsInfoFragment();
        fragment.setArguments(extras);
        return fragment;
    }

    public static VideoDetailsInfoFragment newInstance() {
        VideoDetailsInfoFragment fragment = new VideoDetailsInfoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    // endregion

    // region Lifecycle Methods
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            video = (Video) getArguments().get(LikedVideosFragment.KEY_VIDEO);
        }

        AccessToken token = LoopPrefs.getAccessToken(getActivity());
        vimeoService = ServiceGenerator.createService(
                VimeoService.class,
                VimeoService.BASE_URL,
                new AuthorizedNetworkInterceptor(token));

        boldFont = FontCache.getTypeface("Ubuntu-Bold.ttf", LoopApplication.getInstance().getApplicationContext());
        font = FontCache.getTypeface("Ubuntu-Medium.ttf", getContext());
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_video_details_info, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpRxBusSubscription();

        if(video != null){
            setUpTitle(titleTextView, video);
            setUpSubtitle(subtitleTextView, video);
            setUpViewCount(viewCountTextView, video);
            setUpLike(likeImageView, video);
            setUpWatchLater(watchLaterImageView, video);
            setUpUserImage(userImageView, video);
            setUpUploadedDate(uploadDateTextView, video);
            setUpDescription(descriptionTextView, video);
            setUpTags(hashtagView, video);
            setUpInfoImage(infoImageView);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeListeners();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeSubscription.unsubscribe();
    }

    // endregion

    // region Helper Methods

    private void setUpTitle(TextView tv, Video video) {
        String name = video.getName();
        if (!TextUtils.isEmpty(name)) {
            tv.setText(name);
        }
    }

    private void setUpSubtitle(TextView tv, Video video) {
        User user = video.getUser();
        if (user != null) {
            String userName = user.getName();
            if (!TextUtils.isEmpty(userName)) {
                tv.setText(userName);
            }
        }
    }

    private void setUpLike(ImageView iv, Video video){
        boolean isLiked = video.isLiked();
        if (isLiked) {
            isLikeOn = true;
            iv.setImageResource(R.drawable.ic_likes_on);
        }
    }

    private void setUpWatchLater(ImageView iv, Video video){
        boolean isAddedToWatchLater = video.isAddedToWatchLater();
        if (isAddedToWatchLater) {
            isWatchLaterOn = true;
            iv.setImageResource(R.drawable.ic_watch_later_on);
        }
    }

    private void setUpUserImage(AvatarView av, Video video) {
        User user = video.getUser();
        if(user != null){
            av.bind(user);
        } else {
            av.nullify();
        }
    }

    private void setUpViewCount(TextView tv, Video video) {
        String formattedViewCount = video.getFormattedViewCount();
        if(!TextUtils.isEmpty(formattedViewCount)){
            tv.setText(formattedViewCount);
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    private void setUpUploadedDate(TextView tv, Video video) {
        String formattedCreatedTime = video.getFormattedCreatedTime();
        if (!TextUtils.isEmpty(formattedCreatedTime)) {
            tv.setText(formattedCreatedTime);
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    private void setUpTags(final HashtagView htv, Video video) {
        List<String> canonicalTags = video.getCanonicalTags();
        if(canonicalTags.size() > 0){
            hasTags = true;
            htv.setData(canonicalTags, Transformers.HASH);
            htv.setTypeface(boldFont);
            htv.addOnTagClickListener(new HashtagView.TagsClickListener() {
                @Override
                public void onItemClicked(Object item) {
                    String tag = (String) item;

                    // TODO this triggers two events somehow
                    RxBus.getInstance().send(new SearchPerformedEvent(tag));
                }
            });
            htv.setVisibility(View.VISIBLE);
        } else {
            htv.setVisibility(View.GONE);
        }
    }

    private void setUpDescription(TextView tv, Video video) {
        String formattedDescription = video.getFormattedDescription();
        if (!TextUtils.isEmpty(formattedDescription)) {
            hasDescription = true;
//            formattedDescription = formattedDescription.replaceAll("[\\t\\n\\r]+", "\n");
            tv.setText(formattedDescription);
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    private void setUpInfoImage(ImageView iv){
        if(hasDescription || hasTags){
            iv.setVisibility(View.VISIBLE);
        }
    }

    private void launchSearchActivity(String query) {
        Intent intent = new Intent(getContext(), SearchableActivity.class);
        intent.setAction(Intent.ACTION_SEARCH);
        Bundle bundle = new Bundle();
        bundle.putString(SearchManager.QUERY, query);
        intent.putExtras(bundle);
        getContext().startActivity(intent);
    }

    private void removeListeners() {
//        videosRecyclerView.removeOnScrollListener(recyclerViewOnScrollListener);
    }

    private void setUpRxBusSubscription(){
        Subscription rxBusSubscription = RxBus.getInstance().toObserverable()
                .observeOn(AndroidSchedulers.mainThread()) // UI Thread
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
//                        if (event == null || !isResumed()) {
//                            return;
//                        }

                        if (event == null) {
                            return;
                        }

                        if(event instanceof SearchPerformedEvent) {
                            String query = ((SearchPerformedEvent)event).getQuery();
                            if (!TextUtils.isEmpty(query)) {
                                launchSearchActivity(query);
                            }
                        }
                    }
                });

        compositeSubscription.add(rxBusSubscription);
    }

    // endregion
}
