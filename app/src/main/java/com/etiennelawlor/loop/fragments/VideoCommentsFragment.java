package com.etiennelawlor.loop.fragments;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.adapters.VideoCommentsAdapter;
import com.etiennelawlor.loop.models.AccessToken;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoService;
import com.etiennelawlor.loop.network.models.request.CommentPost;
import com.etiennelawlor.loop.network.models.response.AuthorizedUser;
import com.etiennelawlor.loop.network.models.response.Comment;
import com.etiennelawlor.loop.network.models.response.CommentsCollection;
import com.etiennelawlor.loop.network.models.response.User;
import com.etiennelawlor.loop.network.models.response.Video;
import com.etiennelawlor.loop.otto.BusProvider;
import com.etiennelawlor.loop.prefs.LoopPrefs;
import com.etiennelawlor.loop.ui.LoadingImageView;
import com.etiennelawlor.loop.utilities.DisplayUtility;
import com.etiennelawlor.loop.utilities.FontCache;
import com.etiennelawlor.loop.utilities.LogUtility;
import com.etiennelawlor.loop.utilities.TrestleUtility;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 12/20/15.
 */
public class VideoCommentsFragment extends BaseFragment implements VideoCommentsAdapter.OnItemLongClickListener {

    // region Constants
    public static final int PAGE_SIZE = 60;
    // endregion

    // region Views
    @Bind(R.id.comment_et)
    EditText commentEditText;
    @Bind(R.id.sumbit_comment_iv)
    ImageView submitCommentImageView;
    @Bind(R.id.sumbit_comment_pb)
    ProgressBar submitCommentProgressBar;
    @Bind(R.id.sumbit_comment_fl)
    FrameLayout submitCommentFrameLayout;
    @Bind(R.id.rv)
    RecyclerView commentsRecyclerView;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.loading_iv)
    LoadingImageView loadingImageView;
    // endregion

    //region Member Variables
    private VideoCommentsAdapter videoCommentsAdapter;
    private VimeoService vimeoService;
    private Video video;
    private CommentsCollection commentsCollection;
    private int currentPage = 1;
    private Long videoId = -1L;
    private boolean commentChangeMade = false;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private String sortByValue = "date";
    private String sortOrderValue = "desc";
    private Typeface font;
    // endregion

    // region Listeners
    private TextWatcher commentEditTextTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > 0) {
//                mSubmitCommentImageView.setImageResource(R.drawable.ic_comment_button_highlighted);
                submitCommentImageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(submitCommentImageView.getContext(), android.R.color.white)));
            } else {
//                mSubmitCommentImageView.setImageResource(R.drawable.ic_comment_button);
                submitCommentImageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(submitCommentImageView.getContext(), R.color.fifty_percent_transparency_teal_500)));

            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @OnClick(R.id.sumbit_comment_fl)
    public void submitComment() {
        String comment = commentEditText.getText().toString();
        if (!TextUtils.isEmpty(comment)) {
            CommentPost commentPost = new CommentPost();
            commentPost.setText(comment);
//
            submitCommentFrameLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey_200));
            submitCommentImageView.setVisibility(View.GONE);
            submitCommentProgressBar.setVisibility(View.VISIBLE);

            Call addCommentCall = vimeoService.addComment(videoId, commentPost);
            calls.add(addCommentCall);
            addCommentCall.enqueue(addCommentCallback);
        }
    }
    // endregion

    // region Callbacks
    private Callback<CommentsCollection> getCommentsFirstFetchCallback = new Callback<CommentsCollection>() {
        @Override
        public void onResponse(Response<CommentsCollection> response, Retrofit retrofit) {
            Timber.d("onResponse()");
            loadingImageView.setVisibility(View.GONE);
            isLoading = false;

            if (response != null) {
                if (response.isSuccess()) {
                    commentsCollection = response.body();
                    if (commentsCollection != null) {
                        loadComments();
                    }
                } else {
                    com.squareup.okhttp.Response rawResponse = response.raw();
                    if (rawResponse != null) {
                        LogUtility.logFailedResponse(rawResponse);

                        int code = rawResponse.code();
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

//            if (mVideoCommentsAdapter.isEmpty()) {
//                mEmptyTextView.setText(getString(R.string.watch_later_empty_prompt));
//                Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_watch_later_large);
//                DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), R.color.grey_500));
//                mEmptyTextView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
//                mEmptyView.setVisibility(View.VISIBLE);
//            }
        }

        @Override
        public void onFailure(Throwable t) {
            if (t != null) {
                String message = t.getMessage();
                LogUtility.logFailure(t);

                if (t instanceof SocketTimeoutException || t instanceof UnknownHostException) {
                    Timber.e("Timeout occurred");
                    isLoading = false;
                    loadingImageView.setVisibility(View.GONE);
//
//                    mErrorTextView.setText("Can't load data.\nCheck your network connection.");
//                    mErrorLinearLayout.setVisibility(View.VISIBLE);
                } else if (t instanceof IOException) {
                    if (message.equals("Canceled")) {
                        Timber.e("onFailure() : Canceled");
                    } else {
                        isLoading = false;
//                        mLoadingImageView.setVisibility(View.GONE);
                    }
                }
            }
        }
    };


    private Callback<Comment> addCommentCallback = new Callback<Comment>() {
        @Override
        public void onResponse(Response<Comment> response, Retrofit retrofit) {
            commentChangeMade = true;

            Timber.d("onResponse()");
//            mLoadingImageView.setVisibility(View.GONE);

            submitCommentFrameLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.primary));
            submitCommentProgressBar.setVisibility(View.GONE);
            submitCommentImageView.setVisibility(View.VISIBLE);

            commentEditText.setText("");
            DisplayUtility.hideKeyboard(getActivity(), commentEditText);

            if (response != null) {
                if (response.isSuccess()) {
                    Comment comment = response.body();
                    videoCommentsAdapter.add(comment, videoCommentsAdapter.getItemCount()-1);
                } else {
                    com.squareup.okhttp.Response rawResponse = response.raw();
                    if (rawResponse != null) {
                        LogUtility.logFailedResponse(rawResponse);

                        int code = rawResponse.code();
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
            if (t != null) {
                String message = t.getMessage();
                LogUtility.logFailure(t);

                submitCommentFrameLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.primary));
                submitCommentProgressBar.setVisibility(View.GONE);
                submitCommentImageView.setVisibility(View.VISIBLE);

                commentEditText.setText("");
                DisplayUtility.hideKeyboard(getActivity(), commentEditText);

                if (t instanceof SocketTimeoutException || t instanceof UnknownHostException) {
                    Timber.e("Timeout occurred");
                    isLoading = false;
//                    mLoadingImageView.setVisibility(View.GONE);
//
//                    mErrorTextView.setText("Can't load data.\nCheck your network connection.");
//                    mErrorLinearLayout.setVisibility(View.VISIBLE);
                } else if (t instanceof IOException) {
                    if (message.equals("Canceled")) {
                        Timber.e("onFailure() : Canceled");
                    } else {
                        isLoading = false;
//                        mLoadingImageView.setVisibility(View.GONE);
                    }
                }
            }
        }
    };

    private Callback<Object> deleteCommentCallback = new Callback<Object>() {
        @Override
        public void onResponse(Response<Object> response, Retrofit retrofit) {
            commentChangeMade = true;

            if (response != null) {
                if (response.isSuccess()) {
                    Object object = response.body();
                    Timber.d("");
//                    mVideoCommentsAdapter.add(comment, mVideoCommentsAdapter.getItemCount()-1);
                } else {
                    com.squareup.okhttp.Response rawResponse = response.raw();
                    if (rawResponse != null) {
                        LogUtility.logFailedResponse(rawResponse);

                        int code = rawResponse.code();
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
            if (t != null) {
                String message = t.getMessage();
                LogUtility.logFailure(t);

                if (t instanceof SocketTimeoutException || t instanceof UnknownHostException) {
                    Timber.e("Timeout occurred");
                    isLoading = false;
//                    mLoadingImageView.setVisibility(View.GONE);
//
//                    mErrorTextView.setText("Can't load data.\nCheck your network connection.");
//                    mErrorLinearLayout.setVisibility(View.VISIBLE);
                } else if (t instanceof IOException) {
                    if (message.equals("Canceled")) {
                        Timber.e("onFailure() : Canceled");
                    } else {
                        isLoading = false;
//                        mLoadingImageView.setVisibility(View.GONE);
                    }
                }
            }
        }
    };

//    private Callback<Response> mDeleteCommentCallback = new Callback<Response>() {
//        @Override
//        public void success(Response response, Response response2) {
//            if (isAdded() && isResumed()) {
//                if (response != null) {
//                    int status = response.getStatus();
//                    if (status == 200) {
//                        mCommentChangeMade = true;
//
//                        Timber.d("mDeleteCommentCallback : success()");
//                    } else {
//                        Timber.d("mDeleteCommentCallback : success() : status != 200 : status - " + status);
//                    }
//                } else {
//                    Timber.d("mDeleteCommentCallback : success() : response == null");
//                }
//            }
//        }
//
//        @Override
//        public void failure(RetrofitError error) {
//            if (isAdded() && isResumed()) {
//                Timber.d("mDeleteCommentCallback : failure()");
//
//                if (error != null) {
//                    Response response = error.getResponse();
//                    if (response != null) {
//                        Timber.d("mDeleteCommentCallback : failure() : response.getStatus() - " + response.getStatus());
//                        Timber.d("mDeleteCommentCallback : failure() : response.getReason() - " + response.getReason());
//                    }
//                    Timber.d("mDeleteCommentCallback : failure() : error.getMessage() - " + error.getMessage());
//                    Timber.d("mDeleteCommentCallback : failure() : error.getCause() - " + error.getCause());
//                }
//            }
//        }
//    };
    // endregion

    // region Constructors
    public VideoCommentsFragment() {
    }
    // endregion

    // region Factory Methods
    public static VideoCommentsFragment newInstance(Bundle extras) {
        VideoCommentsFragment fragment = new VideoCommentsFragment();
        fragment.setArguments(extras);
        return fragment;
    }

    public static VideoCommentsFragment newInstance() {
        return new VideoCommentsFragment();
    }
    //endregion

    //region Lifecycle Methods
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        BusProvider.getInstance().register(this);
        if (getArguments() != null) {
            video = (Video) getArguments().get("video");
        }

        AccessToken token = LoopPrefs.getAccessToken(getActivity());
        vimeoService = ServiceGenerator.createService(
                VimeoService.class,
                VimeoService.BASE_URL,
                token);

        font = FontCache.getTypeface("Ubuntu-Medium.ttf", getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_comments, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(TrestleUtility.getFormattedText(getString(R.string.comments), font));
        }

        setUpListeners();

        commentsRecyclerView.setItemAnimator(new SlideInUpAnimator());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
//        layoutManager.setReverseLayout(true);
        commentsRecyclerView.setLayoutManager(layoutManager);

        videoCommentsAdapter = new VideoCommentsAdapter(getActivity());
        videoCommentsAdapter.setOnItemLongClickListener(this);

//        List<Comment> comments = mSale.getComments();
//        if (comments != null && comments.size() > 0) {
//            Collections.reverse(comments);
//            mVideoCommentsAdapter.addAll(comments);
//        }

        commentsRecyclerView.setAdapter(videoCommentsAdapter);

        commentsRecyclerView.smoothScrollToPosition(videoCommentsAdapter.getItemCount());

        if (commentsCollection != null) {
            loadComments();
        } else {
            long id = video.getId();
            if (id != -1L) {
                loadingImageView.setVisibility(View.VISIBLE);

                videoId = id;

                Call getCommentsCall = vimeoService.getComments(videoId,
                        sortByValue,
                        sortOrderValue,
                        currentPage,
                        PAGE_SIZE);
                calls.add(getCommentsCall);
                getCommentsCall.enqueue(getCommentsFirstFetchCallback);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        removeListeners();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);

        if (commentChangeMade) {
            List<Comment> comments = new ArrayList<>();
            for (int i = videoCommentsAdapter.getItemCount() - 1; i >= 0; i--) {
                Comment comment = videoCommentsAdapter.getItem(i);
                comments.add(comment);
            }

//            BusProvider.getInstance().post(new CommentChangeEvent(comments));
        }

        super.onDestroy();
    }
    // endregion

    // region VideoCommentsAdapter.OnItemLongClickListener Methods
    @Override
    public void onItemLongClick(final int position) {
        final Comment comment = videoCommentsAdapter.getItem(position);
        if (comment != null) {
            User user = comment.getUser();
            AuthorizedUser authorizedUser = LoopPrefs.getAuthorizedUser(getActivity());

            if (user != null && authorizedUser != null) {
                long commenterId = user.getId();
                long loggedInUserId = authorizedUser.getId();
                if (commenterId == loggedInUserId) {

                    AlertDialog.Builder deleteCommentAlert = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);

                    deleteCommentAlert.setMessage("Are you sure you want to delete this comment?");

                    deleteCommentAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            videoCommentsAdapter.remove(comment);
                            videoCommentsAdapter.notifyDataSetChanged();

                            Call deleteCommentCall = vimeoService.deleteComment(videoId,
                                    comment.getId());
                            calls.add(deleteCommentCall);
                            deleteCommentCall.enqueue(deleteCommentCallback);
                        }
                    });

                    deleteCommentAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Canceled.
                        }
                    });

                    deleteCommentAlert.show();

                }
            }
        }
    }
    // endregion

    // region Helper Methods
    private void setUpListeners() {
        commentEditText.addTextChangedListener(commentEditTextTextWatcher);
    }

    private void removeListeners() {
        commentEditText.removeTextChangedListener(commentEditTextTextWatcher);
    }

    private void loadComments() {
        List<Comment> comments = commentsCollection.getComments();
        if (comments != null && comments.size() > 0) {

            Collections.reverse(comments);
            videoCommentsAdapter.addAll(comments);
            commentsRecyclerView.smoothScrollToPosition(videoCommentsAdapter.getItemCount());
//
//            mVideoCommentsAdapter.addAll(comments);

            if (comments.size() >= PAGE_SIZE) {
//                            mVideoCommentsAdapter.addLoading();
            } else {
                isLastPage = true;
            }
        }
    }
    // endregion
}