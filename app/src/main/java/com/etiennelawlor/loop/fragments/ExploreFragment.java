package com.etiennelawlor.loop.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.adapters.CategoriesAdapter;
import com.etiennelawlor.loop.animators.SlideInOutBottomItemAnimator;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoService;
import com.etiennelawlor.loop.network.models.AccessToken;
import com.etiennelawlor.loop.network.models.CategoriesCollection;
import com.etiennelawlor.loop.network.models.Category;
import com.etiennelawlor.loop.otto.BusProvider;
import com.etiennelawlor.loop.ui.GridSpacesItemDecoration;
import com.etiennelawlor.loop.utilities.LoopUtility;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedInput;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 6/13/15.
 */
public class ExploreFragment extends BaseFragment implements CategoriesAdapter.OnItemClickListener {

    // region Constants
    // endregion

    // region Member Variables
    @InjectView(R.id.categories_rv)
    RecyclerView mCategoriesRecyclerView;
    @InjectView(android.R.id.empty)
    View mEmptyView;
    @InjectView(R.id.pb)
    ProgressBar mProgressBar;
    @InjectView(R.id.error_ll)
    LinearLayout mErrorLinearLayout;
    @InjectView(R.id.error_tv)
    TextView mErrorTextView;
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;

    private boolean mIsLoading = false;
    private CategoriesAdapter mCategoriesAdapter;
    private LinearLayoutManager mLayoutManager;
    private VimeoService mVimeoService;
    // endregion

    // region Listeners
    // endregion

    // region Callbacks
    private Callback<CategoriesCollection> mGetCategoriesCallback = new Callback<CategoriesCollection>() {
        @Override
        public void success(CategoriesCollection categoriesCollection, Response response) {
            if(isAdded() && isResumed()){
                mProgressBar.setVisibility(View.GONE);
                mIsLoading = false;

                Timber.d("");
                if(categoriesCollection != null){
                    List<Category> categories = categoriesCollection.getCategories();

                    Timber.d("");
                    mCategoriesAdapter.addAll(categories);
                }
            }
        }

        @Override
        public void failure(RetrofitError error) {
            if(isAdded() && isResumed()){
                mProgressBar.setVisibility(View.GONE);
                mIsLoading = false;

                Timber.d("");

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
    // endregion

    // region Constructors
    public static ExploreFragment newInstance() {
        ExploreFragment fragment = new ExploreFragment();
        return fragment;
    }

    public static ExploreFragment newInstance(Bundle extras) {
        ExploreFragment fragment = new ExploreFragment();
        fragment.setArguments(extras);
        return fragment;
    }

    public ExploreFragment() {
    }
    // endregion

    // region Lifecycle Methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BusProvider.get().register(this);

        if (getArguments() != null) {
//            mQuery = getArguments().getString("query");
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String tokenType = sharedPreferences.getString(getString(R.string.token_type), "");
        String accessToken = sharedPreferences.getString(getString(R.string.access_token), "");
        AccessToken token = new AccessToken(tokenType, accessToken);

        mVimeoService = ServiceGenerator.createService(
                VimeoService.class,
                VimeoService.BASE_URL,
                token);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_explore, container, false);
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
        ab.setTitle("Explore");

//        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(mQuery);

        mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mCategoriesRecyclerView.setLayoutManager(mLayoutManager);
        mCategoriesRecyclerView.addItemDecoration(new GridSpacesItemDecoration(LoopUtility.dp2px(getActivity(), 8)));

        mCategoriesAdapter = new CategoriesAdapter(getActivity());
        mCategoriesAdapter.setOnItemClickListener(this);

        mCategoriesRecyclerView.setItemAnimator(new SlideInOutBottomItemAnimator(mCategoriesRecyclerView));

        mCategoriesRecyclerView.setAdapter(mCategoriesAdapter);

        mVimeoService.getCategories(mGetCategoriesCallback);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

//        mVideosRecyclerView.removeOnScrollListener(mRecyclerViewOnScrollListener);
        ButterKnife.reset(this);
    }
    // endregion

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//
//        inflater.inflate(R.menu.main_menu, menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
////            case R.id.sort_by:
////                showSortByDialog();
////                break;
////            case R.id.sort_order:
////                showSortOrderDialog();
////                break;
//            default:
//                // do nothing
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    // region CategoriesAdapter.OnItemClickListener Methods
    @Override
    public void onItemClick(int position, View view) {

        Category category = mCategoriesAdapter.getItem(position);
        Timber.d("");

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
//                Pair<View, String> p2 = Pair.create((View) view.findViewById(R.id.title_tv), "titleTransition");
//                Pair<View, String> p3 = Pair.create((View) view.findViewById(R.id.subtitle_tv), "subtitleTransition");
////        Pair<View, String> p4 = Pair.create((View)view.findViewById(R.id.uploaded_tv), "uploadedTransition");
//
//                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
//                        p1, p2, p3);
//
//
//                ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
//
////                startActivity(intent);
//            }
//        }

    }
    // endregion

    // region Helper Methods
    // endregion
}
