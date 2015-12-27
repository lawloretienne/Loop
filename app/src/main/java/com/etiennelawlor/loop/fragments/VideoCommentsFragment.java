package com.etiennelawlor.loop.fragments;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
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
import com.etiennelawlor.loop.helper.PreferencesHelper;
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
import com.etiennelawlor.loop.ui.LoadingImageView;
import com.etiennelawlor.loop.utilities.DisplayUtility;
import com.etiennelawlor.loop.utilities.LogUtility;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
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

    //region Member Variables
    private VideoCommentsAdapter mVideoCommentsAdapter;
    private VimeoService mVimeoService;
    private Video mVideo;
    private CommentsCollection mCommentsCollection;
    private int mCurrentPage = 1;
    private Long mVideoId = -1L;
    private boolean mCommentChangeMade = false;
    private boolean mIsLoading = false;
    private boolean mIsLastPage = false;
    private String mSortByValue = "date";
    private String mSortOrderValue = "desc";

    @Bind(R.id.comment_et)
    EditText mCommentEditText;
    @Bind(R.id.sumbit_comment_iv)
    ImageView mSubmitCommentImageView;
    @Bind(R.id.sumbit_comment_pb)
    ProgressBar mSubmitCommentProgressBar;
    @Bind(R.id.sumbit_comment_fl)
    FrameLayout mSubmitCommentFrameLayout;
    @Bind(R.id.rv)
    RecyclerView mCommentsRecyclerView;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.loading_iv)
    LoadingImageView mLoadingImageView;
    // endregion

    // region Listeners
    private TextWatcher mCommentEditTextTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > 0) {
//                mSubmitCommentImageView.setImageResource(R.drawable.ic_comment_button_highlighted);
                mSubmitCommentImageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mSubmitCommentImageView.getContext(), android.R.color.white)));
            } else {
//                mSubmitCommentImageView.setImageResource(R.drawable.ic_comment_button);
                mSubmitCommentImageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mSubmitCommentImageView.getContext(), R.color.fifty_percent_transparency_teal_500)));

            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @OnClick(R.id.sumbit_comment_fl)
    public void submitComment() {
        String comment = mCommentEditText.getText().toString();
        if (!TextUtils.isEmpty(comment)) {
            CommentPost commentPost = new CommentPost();
            commentPost.setText(comment);
//
            mSubmitCommentFrameLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey_200));
            mSubmitCommentImageView.setVisibility(View.GONE);
            mSubmitCommentProgressBar.setVisibility(View.VISIBLE);

            Call addCommentCall = mVimeoService.addComment(mVideoId, commentPost);
            mCalls.add(addCommentCall);
            addCommentCall.enqueue(mAddCommentCallback);

//            Api.getService(Api.getEndpointUrl()).addComment(mSale.getId(), commentPost, mAddCommentCallback);
        }
    }
    // endregion

    // region Callbacks
    private Callback<CommentsCollection> mGetCommentsFirstFetchCallback = new Callback<CommentsCollection>() {
        @Override
        public void onResponse(Response<CommentsCollection> response, Retrofit retrofit) {
            Timber.d("onResponse()");
            mLoadingImageView.setVisibility(View.GONE);
            mIsLoading = false;

            if (response != null) {
                if(response.isSuccess()){
                    mCommentsCollection = response.body();
                    if(mCommentsCollection != null){
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
                    mIsLoading = false;
                    mLoadingImageView.setVisibility(View.GONE);
//
//                    mErrorTextView.setText("Can't load data.\nCheck your network connection.");
//                    mErrorLinearLayout.setVisibility(View.VISIBLE);
                } else if(t instanceof IOException){
                    if(message.equals("Canceled")){
                        Timber.e("onFailure() : Canceled");
                    } else {
                        mIsLoading = false;
//                        mLoadingImageView.setVisibility(View.GONE);
                    }
                }
            }
        }
    };


    private Callback<Comment> mAddCommentCallback = new Callback<Comment>() {
        @Override
        public void onResponse(Response<Comment> response, Retrofit retrofit) {
            mCommentChangeMade = true;

            Timber.d("onResponse()");
//            mLoadingImageView.setVisibility(View.GONE);

            mSubmitCommentFrameLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.primary));
            mSubmitCommentProgressBar.setVisibility(View.GONE);
            mSubmitCommentImageView.setVisibility(View.VISIBLE);

            mCommentEditText.setText("");
            DisplayUtility.hideKeyboard(getActivity(), mCommentEditText);

            if (response != null) {
                if(response.isSuccess()){
                    List<Comment> comments = new ArrayList<>();
                    Comment comment = response.body();
                    if(comment != null){
                        comments.add(comment);
                        mVideoCommentsAdapter.addAll(comments);
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
        }

        @Override
        public void onFailure(Throwable t) {
            if (t != null) {
                String message = t.getMessage();
                LogUtility.logFailure(t);

                mSubmitCommentFrameLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.primary));
                mSubmitCommentProgressBar.setVisibility(View.GONE);
                mSubmitCommentImageView.setVisibility(View.VISIBLE);

                mCommentEditText.setText("");
                DisplayUtility.hideKeyboard(getActivity(), mCommentEditText);

                if (t instanceof SocketTimeoutException || t instanceof UnknownHostException) {
                    Timber.e("Timeout occurred");
                    mIsLoading = false;
//                    mLoadingImageView.setVisibility(View.GONE);
//
//                    mErrorTextView.setText("Can't load data.\nCheck your network connection.");
//                    mErrorLinearLayout.setVisibility(View.VISIBLE);
                } else if(t instanceof IOException){
                    if(message.equals("Canceled")){
                        Timber.e("onFailure() : Canceled");
                    } else {
                        mIsLoading = false;
//                        mLoadingImageView.setVisibility(View.GONE);
                    }
                }
            }
        }
    };

//    private Callback<List<Comment>> mGetCommentsCallback = new Callback<List<Comment>>() {
//        @Override
//        public void success(List<Comment> comments, Response response) {
//            if (isAdded() && isResumed()) {
//                Timber.d("mGetCommentsCallback : success()");
//                Collections.reverse(comments);
//                mVideoCommentsAdapter.addAll(Lists.reverse(comments));
//                mCommentsRecyclerView.smoothScrollToPosition(mVideoCommentsAdapter.getItemCount());
//
//            }
//        }
//
//        @Override
//        public void failure(RetrofitError error) {
//            if (isAdded() && isResumed()) {
//                Timber.d("mGetCommentsCallback : failure()");
//
//                if (error != null) {
//                    Response response = error.getResponse();
//                    if (response != null) {
//                        Timber.d("mGetCommentsCallback : failure() : response.getStatus() - " + response.getStatus());
//                        Timber.d("mGetCommentsCallback : failure() : response.getReason() - " + response.getReason());
//                    }
//                    Timber.d("mGetCommentsCallback : failure() : error.getMessage() - " + error.getMessage());
//                    Timber.d("mGetCommentsCallback : failure() : error.getCause() - " + error.getCause());
//                }
//            }
//        }
//    };

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
            mVideo = (Video) getArguments().get("video");
        }

        AccessToken token = PreferencesHelper.getAccessToken(getActivity());
        mVimeoService = ServiceGenerator.createService(
                VimeoService.class,
                VimeoService.BASE_URL,
                token);
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

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setUpListeners();

        mCommentsRecyclerView.setItemAnimator(new SlideInUpAnimator());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        mCommentsRecyclerView.setLayoutManager(layoutManager);

        mVideoCommentsAdapter = new VideoCommentsAdapter(getActivity());
        mVideoCommentsAdapter.setOnItemLongClickListener(this);

//        List<Comment> comments = mSale.getComments();
//        if (comments != null && comments.size() > 0) {
//            Collections.reverse(comments);
//            mVideoCommentsAdapter.addAll(comments);
//        }

        mCommentsRecyclerView.setAdapter(mVideoCommentsAdapter);

        mCommentsRecyclerView.smoothScrollToPosition(mVideoCommentsAdapter.getItemCount());

        if(mCommentsCollection != null){
            loadComments();
        } else {
            String uri = mVideo.getUri();
            if (!TextUtils.isEmpty(uri)) {
                mLoadingImageView.setVisibility(View.VISIBLE);

                String lastPathSegment = Uri.parse(uri).getLastPathSegment();
                mVideoId = Long.parseLong(lastPathSegment);

                Call getCommentsCall = mVimeoService.getComments(mVideoId,
                        mSortByValue,
                        mSortOrderValue,
                        mCurrentPage,
                        PAGE_SIZE);
                mCalls.add(getCommentsCall);
                getCommentsCall.enqueue(mGetCommentsFirstFetchCallback);
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

        if (mCommentChangeMade) {
            List<Comment> comments = new ArrayList<>();
            for (int i = mVideoCommentsAdapter.getItemCount() - 1; i >= 0; i--) {
                Comment comment = mVideoCommentsAdapter.getItem(i);
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
        final Comment comment = mVideoCommentsAdapter.getItem(position);
        if (comment != null) {
            User user = comment.getUser();
            AuthorizedUser authorizedUser = PreferencesHelper.getAuthorizedUser(getActivity());

            if (user != null && authorizedUser != null) {
                String commenterDisplayName = user.getName();
                String loggedInUserDisplayName = authorizedUser.getName();
                if (!TextUtils.isEmpty(commenterDisplayName)
                        && !TextUtils.isEmpty(loggedInUserDisplayName)
                        && commenterDisplayName.equals(loggedInUserDisplayName)) {

                    AlertDialog.Builder deleteCommentAlert = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);

                    deleteCommentAlert.setMessage("Are you sure you want to delete this comment?");

                    deleteCommentAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

//                            mVideoCommentsAdapter.remove(position);
//                            mVideoCommentsAdapter.notifyDataSetChanged();

//                            Api.getService(Api.getEndpointUrl()).deleteComment(mSale.getId(), comment.getId(), mDeleteCommentCallback);
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
        mCommentEditText.addTextChangedListener(mCommentEditTextTextWatcher);
    }

    private void removeListeners() {
        mCommentEditText.removeTextChangedListener(mCommentEditTextTextWatcher);
    }

    private void loadComments(){
        List<Comment> comments = mCommentsCollection.getComments();
        if (comments != null && comments.size()>0) {
            mVideoCommentsAdapter.addAll(comments);

            if(comments.size() >= PAGE_SIZE){
//                            mVideoCommentsAdapter.addLoading();
            } else {
                mIsLastPage = true;
            }
        }
    }
    // endregion
}