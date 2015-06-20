package com.etiennelawlor.loop.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.activities.VideoDetailsActivity;
import com.etiennelawlor.loop.adapters.VideosAdapter;
import com.etiennelawlor.loop.animators.SlideInOutBottomItemAnimator;
import com.etiennelawlor.loop.helper.PreferencesHelper;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoService;
import com.etiennelawlor.loop.network.models.AccessToken;
import com.etiennelawlor.loop.network.models.Video;
import com.etiennelawlor.loop.network.models.VideoWrapper;
import com.etiennelawlor.loop.network.models.VideosCollection;
import com.etiennelawlor.loop.otto.BusProvider;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedInput;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class LikedVideosFragment extends BaseFragment implements VideosAdapter.OnItemClickListener {

    // region Constants
    public static final int PAGE_SIZE = 30;
    // endregion

    // region Member Variables
    @InjectView(R.id.videos_rv)
    RecyclerView mVideosRecyclerView;
    @InjectView(android.R.id.empty)
    View mEmptyView;
    @InjectView(R.id.empty_tv)
    TextView mEmptyTextView;
    @InjectView(R.id.pb)
    ProgressBar mProgressBar;
    @InjectView(R.id.error_ll)
    LinearLayout mErrorLinearLayout;
    @InjectView(R.id.error_tv)
    TextView mErrorTextView;
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    private int mCurrentPage = 1;
    private int mSelectedSortByKey = 0;
    private int mSelectedSortOrderKey = 1;
    private boolean mIsLoading = false;
    private String mSortByValue = "date";
    private String mSortOrderValue = "desc";
    private VideosAdapter mVideosAdapter;
    private String mQuery;
    private LinearLayoutManager mLayoutManager;
    private VimeoService mVimeoService;
    // endregion

    // region Listeners
    private RecyclerView.OnScrollListener mRecyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mLayoutManager.getChildCount();
            int totalItemCount = mLayoutManager.getItemCount();
            int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();

            if (!mIsLoading) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
                    loadMoreItems();
                }
            }
        }
    };
    // endregion

    // region Callbacks
    private Callback<VideosCollection> mFindVideosFirstFetchCallback = new Callback<VideosCollection>() {
        @Override
        public void success(VideosCollection videosCollection, Response response) {
            Timber.d("success()");
            if (isAdded() && isResumed()) {
                mProgressBar.setVisibility(View.GONE);
                mIsLoading = false;

                if (videosCollection != null) {
                    List<Video> videos = videosCollection.getVideos();
                    if (videos != null) {
                        mVideosAdapter.addAll(videos);
                    }
                }

                if(mVideosAdapter.isEmpty()){
                    mEmptyTextView.setText(getString(R.string.likes_empty_prompt));
                    Drawable drawable = getResources().getDrawable(R.drawable.ic_likes_large);
                    DrawableCompat.setTint(drawable, getResources().getColor(R.color.grey_500));
                    mEmptyTextView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void failure(RetrofitError error) {
            if (isAdded() && isResumed()) {
                Timber.d("failure()");
                mIsLoading = false;
                mProgressBar.setVisibility(View.GONE);

                if(error != null){
                    Response response = error.getResponse();
                    if(response != null){
                        String reason = response.getReason();
                        Timber.d("failure() : reason -"+reason);

                        TypedInput body = response.getBody();
                        if(body != null){
                            Timber.d("failure() : body.toString() -"+body.toString());
                        }

                        int status = response.getStatus();
                        Timber.d("failure() : status -"+status);
                    }

                    Throwable cause = error.getCause();
                    if(cause != null){
                        Timber.d("failure() : cause.toString() -"+cause.toString());
                    }

                    Object body = error.getBody();
                    if(body != null){
                        Timber.d("failure() : body.toString() -"+body.toString());
                    }
                }
            }

        }
    };

    private Callback<VideosCollection> mFindVideosNextFetchCallback = new Callback<VideosCollection>() {
        @Override
        public void success(VideosCollection videosCollection, Response response) {
            Timber.d("success()");
            if (isAdded() && isResumed()) {
//                mProgressBar.setVisibility(View.GONE);

                mVideosAdapter.removeLoading();

                mIsLoading = false;

                if (videosCollection != null) {
                    List<Video> videos = videosCollection.getVideos();
                    if (videos != null) {
                        mVideosAdapter.addAll(videos);
                    }
                }
            }
        }

        @Override
        public void failure(RetrofitError error) {
            if (isAdded() && isResumed()) {
                Timber.d("failure()");
                mIsLoading = false;
//                mProgressBar.setVisibility(View.GONE);

                mVideosAdapter.removeLoading();
            }
        }
    };
    // endregion

    // region Constructors
    public static LikedVideosFragment newInstance() {
        LikedVideosFragment fragment = new LikedVideosFragment();
        return fragment;
    }

    public static LikedVideosFragment newInstance(Bundle extras) {
        LikedVideosFragment fragment = new LikedVideosFragment();
        fragment.setArguments(extras);
        return fragment;
    }

    public LikedVideosFragment() {
    }
    // endregion

    // region Lifecycle Methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BusProvider.get().register(this);

        if (getArguments() != null) {
            mQuery = getArguments().getString("query");
        }

        AccessToken token = PreferencesHelper.getAccessToken(getActivity());
        mVimeoService = ServiceGenerator.createService(
                VimeoService.class,
                VimeoService.BASE_URL,
                token);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_liked_videos, container, false);
        ButterKnife.inject(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Likes");

        mLayoutManager = new LinearLayoutManager(getActivity());
        mVideosRecyclerView.setLayoutManager(mLayoutManager);
        mVideosAdapter = new VideosAdapter(getActivity());
        mVideosAdapter.setOnItemClickListener(this);

        mVideosRecyclerView.setItemAnimator(new SlideInOutBottomItemAnimator(mVideosRecyclerView));

        mVideosRecyclerView.setAdapter(mVideosAdapter);

        // Pagination
        mVideosRecyclerView.addOnScrollListener(mRecyclerViewOnScrollListener);

        mVimeoService.findLikedVideos(mQuery,
                mSortByValue,
                mSortOrderValue,
                mCurrentPage,
                PAGE_SIZE,
                mFindVideosFirstFetchCallback);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mVideosRecyclerView.removeOnScrollListener(mRecyclerViewOnScrollListener);
        ButterKnife.reset(this);
    }
    // endregion

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_by:
                showSortByDialog();
                break;
            case R.id.sort_order:
                showSortOrderDialog();
                break;
            default:
                // do nothing
                break;
        }
        return super.onOptionsItemSelected(item);
    }

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

//                startActivity(intent);
            }
        }

    }
    // endregion

    // region Helper Methods
    private void loadMoreItems() {
        mIsLoading = true;

        mCurrentPage += 1;

        mVimeoService.findLikedVideos(mQuery,
                mSortByValue,
                mSortOrderValue,
                mCurrentPage,
                PAGE_SIZE,
                mFindVideosNextFetchCallback);

    }

    private void showSortByDialog() {
        AlertDialog.Builder sortByBuilder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
        sortByBuilder.setTitle("Sort by");
        sortByBuilder.setSingleChoiceItems(R.array.likes_sort_by_keys, mSelectedSortByKey, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mSelectedSortByKey = whichButton;

                String[] sortByValues = getResources().getStringArray(R.array.likes_sort_by_values);
                mSortByValue = sortByValues[mSelectedSortByKey];

                mVideosAdapter.clear();

                mEmptyView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);

                mCurrentPage = 1;

                mVimeoService.findLikedVideos(mQuery,
                        mSortByValue,
                        mSortOrderValue,
                        mCurrentPage,
                        PAGE_SIZE,
                        mFindVideosFirstFetchCallback);

                dialog.dismiss();
            }
        });
        sortByBuilder.show();
    }

    private void showSortOrderDialog() {
        AlertDialog.Builder sortOrderBuilder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
        sortOrderBuilder.setTitle("Sort order");
        sortOrderBuilder.setSingleChoiceItems(R.array.likes_sort_order_keys, mSelectedSortOrderKey, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mSelectedSortOrderKey = whichButton;

                String[] sortOrderValues = getResources().getStringArray(R.array.likes_sort_order_values);
                mSortOrderValue = sortOrderValues[mSelectedSortOrderKey];

                mVideosAdapter.clear();

                mEmptyView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);

                mCurrentPage = 1;

                mVimeoService.findLikedVideos(mQuery,
                        mSortByValue,
                        mSortOrderValue,
                        mCurrentPage,
                        PAGE_SIZE,
                        mFindVideosFirstFetchCallback);

                dialog.dismiss();

            }
        });
        sortOrderBuilder.show();
    }
    // endregion
}
