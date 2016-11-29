package com.etiennelawlor.loop.fragments;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.etiennelawlor.loop.prefs.LoopPrefs;
import com.etiennelawlor.loop.ui.LoadingImageView;
import com.etiennelawlor.loop.utilities.DisplayUtility;
import com.etiennelawlor.loop.utilities.FontCache;
import com.etiennelawlor.loop.utilities.NetworkLogUtility;
import com.etiennelawlor.loop.utilities.TrestleUtility;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    RecyclerView recyclerView;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.loading_iv)
    LoadingImageView loadingImageView;
    @Bind(R.id.error_ll)
    LinearLayout errorLinearLayout;
    @Bind(R.id.error_tv)
    TextView errorTextView;
    // endregion

    //region Member Variables
    private VideoCommentsAdapter videoCommentsAdapter;
    private VimeoService vimeoService;
    private Video video;
    private int currentPage = 1;
    private Long videoId = -1L;
    private boolean commentChangeMade = false;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private String sortByValue = "date";
    private String sortOrderValue = "desc";
    private Typeface font;
    private Comment deletedComment;
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

//                submitCommentImageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(submitCommentImageView.getContext(), android.R.color.white)));

//                ColorStateList csl = AppCompatResources.getColorStateList(submitCommentImageView.getContext(), android.R.color.white);
//                Drawable drawable = DrawableCompat.wrap(submitCommentImageView.getDrawable());
//                DrawableCompat.setTintList(drawable, csl);
//                submitCommentImageView.setImageDrawable(drawable);
                submitCommentImageView.setImageResource(R.drawable.ic_comment_active);
            } else {
//                mSubmitCommentImageView.setImageResource(R.drawable.ic_comment_button);

//                submitCommentImageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(submitCommentImageView.getContext(), R.color.fifty_percent_transparency_teal_500)));

//                ColorStateList csl = AppCompatResources.getColorStateList(submitCommentImageView.getContext(), R.color.fifty_percent_transparency_teal_500);
//                Drawable drawable = DrawableCompat.wrap(submitCommentImageView.getDrawable());
//                DrawableCompat.setTintList(drawable, csl);
//                submitCommentImageView.setImageDrawable(drawable);

//                mWrappedDrawable = mDrawable.mutate();
//                mWrappedDrawable = DrawableCompat.wrap(mWrappedDrawable);
//                DrawableCompat.setTint(mWrappedDrawable, mColor);
//                DrawableCompat.setTintMode(mWrappedDrawable, PorterDuff.Mode.SRC_IN);

                submitCommentImageView.setImageResource(R.drawable.ic_comment_inactive);
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

    @OnClick(R.id.reload_btn)
    public void onReloadButtonClicked() {
        errorLinearLayout.setVisibility(View.GONE);
        loadingImageView.setVisibility(View.VISIBLE);

        Call getCommentsCall = vimeoService.getComments(videoId,
                sortByValue,
                sortOrderValue,
                currentPage,
                PAGE_SIZE);
        calls.add(getCommentsCall);
        getCommentsCall.enqueue(getCommentsFirstFetchCallback);
    }
    // endregion

    // region Callbacks
    private Callback<CommentsCollection> getCommentsFirstFetchCallback = new Callback<CommentsCollection>() {
        @Override
        public void onResponse(Call<CommentsCollection> call, Response<CommentsCollection> response) {
            loadingImageView.setVisibility(View.GONE);
            isLoading = false;

            if (!response.isSuccessful()) {
                int responseCode = response.code();
                if(responseCode == 504) { // 504 Unsatisfiable Request (only-if-cached)
//                    errorTextView.setText("Can't load data.\nCheck your network connection.");
//                    errorLinearLayout.setVisibility(View.VISIBLE);
                }
                return;
            }

            CommentsCollection commentsCollection = response.body();
            if (commentsCollection != null) {
                List<Comment> comments = commentsCollection.getComments();
                if (comments != null && comments.size() > 0) {

                    Collections.reverse(comments);
                    videoCommentsAdapter.addAll(comments);
                    recyclerView.scrollToPosition(videoCommentsAdapter.getItemCount() - 1);

                    if (comments.size() >= PAGE_SIZE) {
//                            mVideoCommentsAdapter.addLoading();
                    } else {
                        isLastPage = true;
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
        public void onFailure(Call<CommentsCollection> call, Throwable t) {
            NetworkLogUtility.logFailure(call, t);

            loadingImageView.setVisibility(View.GONE);
            isLoading = false;

            if(t instanceof ConnectException || t instanceof UnknownHostException){
                errorTextView.setText("Can't load data.\nCheck your network connection.");
                errorLinearLayout.setVisibility(View.VISIBLE);
            }
        }
    };


    private Callback<Comment> addCommentCallback = new Callback<Comment>() {
        @Override
        public void onResponse(Call<Comment> call, Response<Comment> response) {
            commentChangeMade = true;

            submitCommentFrameLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.primary));
            submitCommentProgressBar.setVisibility(View.GONE);
            submitCommentImageView.setVisibility(View.VISIBLE);

            commentEditText.setText("");
            DisplayUtility.hideKeyboard(getActivity(), commentEditText);

            if (!response.isSuccessful()) {
                int responseCode = response.code();
                if(responseCode == 504) { // 504 Unsatisfiable Request (only-if-cached)
//                    errorTextView.setText("Can't load data.\nCheck your network connection.");
//                    errorLinearLayout.setVisibility(View.VISIBLE);
                }
                return;
            }

            Comment comment = response.body();
            videoCommentsAdapter.add(comment);
            recyclerView.scrollToPosition(videoCommentsAdapter.getItemCount()-1);
        }

        @Override
        public void onFailure(Call<Comment> call, Throwable t) {
            NetworkLogUtility.logFailure(call, t);

            submitCommentFrameLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.primary));
            submitCommentProgressBar.setVisibility(View.GONE);
            submitCommentImageView.setVisibility(View.VISIBLE);

            DisplayUtility.hideKeyboard(getActivity(), commentEditText);

            if(t instanceof ConnectException || t instanceof UnknownHostException){
                Snackbar.make(getActivity().findViewById(R.id.main_content),
                        TrestleUtility.getFormattedText("Network connection is unavailable.", font, 16),
                        Snackbar.LENGTH_LONG)
                        .show();
            }
        }
    };

    private Callback<ResponseBody> deleteCommentCallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            commentChangeMade = true;

            if (!response.isSuccessful()) {
                int responseCode = response.code();
                if(responseCode == 504) { // 504 Unsatisfiable Request (only-if-cached)
//                    errorTextView.setText("Can't load data.\nCheck your network connection.");
//                    errorLinearLayout.setVisibility(View.VISIBLE);
                }
                return;
            }

            // Response 204 No Content

            videoCommentsAdapter.remove(deletedComment);
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            NetworkLogUtility.logFailure(call, t);

            if(t instanceof ConnectException || t instanceof UnknownHostException){
                Snackbar.make(getActivity().findViewById(R.id.main_content),
                        TrestleUtility.getFormattedText("Network connection is unavailable.", font, 16),
                        Snackbar.LENGTH_LONG)
                        .show();
            }
        }
    };
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

        if (getArguments() != null) {
            video = (Video) getArguments().get(LikedVideosFragment.KEY_VIDEO);
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

        recyclerView.setItemAnimator(new SlideInUpAnimator());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
//        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setItemAnimator(new SlideInUpAnimator());

        videoCommentsAdapter = new VideoCommentsAdapter(getActivity());
        videoCommentsAdapter.setOnItemLongClickListener(this);

//        List<Comment> comments = mSale.getComments();
//        if (comments != null && comments.size() > 0) {
//            Collections.reverse(comments);
//            mVideoCommentsAdapter.addAll(comments);
//        }

        recyclerView.setAdapter(videoCommentsAdapter);

        recyclerView.scrollToPosition(videoCommentsAdapter.getItemCount() - 1);

//        if (commentsCollection != null) {
////            loadComments();
//
//            List<Comment> comments = commentsCollection.getComments();
//            if (comments != null && comments.size() > 0) {
//
//                Collections.reverse(comments);
//                videoCommentsAdapter.addAll(comments);
//                recyclerView.scrollToPosition(videoCommentsAdapter.getItemCount() - 1);
////
////            mVideoCommentsAdapter.addAll(comments);
//
//                if (comments.size() >= PAGE_SIZE) {
////                            mVideoCommentsAdapter.addLoading();
//                } else {
//                    isLastPage = true;
//                }
//            }
//        } else {
//
//        }

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
        if (commentChangeMade) {
            List<Comment> comments = new ArrayList<>();
            for (int i = videoCommentsAdapter.getItemCount() - 1; i >= 0; i--) {
                Comment comment = videoCommentsAdapter.getItem(i);
                comments.add(comment);
            }
        }

        super.onDestroy();
    }
    // endregion

    // region VideoCommentsAdapter.OnItemLongClickListener Methods
    @Override
    public void onItemLongClick(final int position) {
        final Comment comment = videoCommentsAdapter.getItem(position);
        if (comment != null) {
            deletedComment = comment;
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
//                            videoCommentsAdapter.remove(comment);
//                            videoCommentsAdapter.notifyDataSetChanged();

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

//    private void loadComments() {
//        List<Comment> comments = commentsCollection.getComments();
//        if (comments != null && comments.size() > 0) {
//
//            Collections.reverse(comments);
//            videoCommentsAdapter.addAll(comments);
//            recyclerView.scrollToPosition(videoCommentsAdapter.getItemCount() - 1);
////
////            mVideoCommentsAdapter.addAll(comments);
//
//            if (comments.size() >= PAGE_SIZE) {
////                            mVideoCommentsAdapter.addLoading();
//            } else {
//                isLastPage = true;
//            }
//        }
//    }
    // endregion
}