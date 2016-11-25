package com.etiennelawlor.loop.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
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
import android.widget.TextView;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.adapters.CategoriesAdapter;
import com.etiennelawlor.loop.helper.PreferencesHelper;
import com.etiennelawlor.loop.models.AccessToken;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoService;
import com.etiennelawlor.loop.network.models.response.CategoriesCollection;
import com.etiennelawlor.loop.network.models.response.Category;
import com.etiennelawlor.loop.otto.BusProvider;
import com.etiennelawlor.loop.ui.GridSpacesItemDecoration;
import com.etiennelawlor.loop.ui.LoadingImageView;
import com.etiennelawlor.loop.utilities.DisplayUtility;
import com.etiennelawlor.loop.utilities.FontCache;
import com.etiennelawlor.loop.utilities.LogUtility;
import com.etiennelawlor.loop.utilities.TrestleUtility;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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
 * Created by etiennelawlor on 6/13/15.
 */
public class ExploreFragment extends BaseFragment implements CategoriesAdapter.OnItemClickListener {

    // region Constants
    // endregion

    // region Member Variables
    @Bind(R.id.categories_rv)
    RecyclerView categoriesRecyclerView;
    @Bind(android.R.id.empty)
    View emptyView;
    @Bind(R.id.loading_iv)
    LoadingImageView loadingImageView;
    @Bind(R.id.error_ll)
    LinearLayout errorLinearLayout;
    @Bind(R.id.error_tv)
    TextView errorTextView;
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private boolean isLoading = false;
    private CategoriesAdapter categoriesAdapter;
    private VimeoService vimeoService;
    private Typeface font;
    // endregion

    // region Listeners
    @OnClick(R.id.reload_btn)
    public void onReloadButtonClicked() {
        errorLinearLayout.setVisibility(View.GONE);
        loadingImageView.setVisibility(View.VISIBLE);

        Call getCategoriesCall = vimeoService.getCategories();
        calls.add(getCategoriesCall);
        getCategoriesCall.enqueue(getCategoriesCallback);
    }
    // endregion

    // region Callbacks
    private Callback<CategoriesCollection> getCategoriesCallback = new Callback<CategoriesCollection>() {
        @Override
        public void onResponse(Response<CategoriesCollection> response, Retrofit retrofit) {
            Timber.d("onResponse()");

            loadingImageView.setVisibility(View.GONE);
            isLoading = false;

            if (response != null) {
                if(response.isSuccess()){
                    CategoriesCollection categoriesCollection = response.body();
                    if (categoriesCollection != null) {
                        List<Category> categories = categoriesCollection.getCategories();
                        categoriesAdapter.addAll(categories);
                    }
                } else {
                    com.squareup.okhttp.Response rawResponse = response.raw();
                    if (rawResponse != null) {
                        LogUtility.logFailedResponse(rawResponse);

                        int code = rawResponse.code();
                        switch (code) {
                            case 500:
                                errorTextView.setText("Can't load data.\nCheck your network connection.");
                                errorLinearLayout.setVisibility(View.VISIBLE);
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
                    loadingImageView.setVisibility(View.GONE);

                    errorTextView.setText("Can't load data.\nCheck your network connection.");
                    errorLinearLayout.setVisibility(View.VISIBLE);
                } else if(t instanceof IOException){
                    if(message.equals("Canceled")){
                        Timber.e("onFailure() : Canceled");
                    } else {
                        isLoading = false;
                        loadingImageView.setVisibility(View.GONE);
                    }
                }
            }
        }
    };
    // endregion

    // region Constructors
    public ExploreFragment() {
    }
    // endregion

    // region Factory Methods
    public static ExploreFragment newInstance() {
        return new ExploreFragment();
    }

    public static ExploreFragment newInstance(Bundle extras) {
        ExploreFragment fragment = new ExploreFragment();
        fragment.setArguments(extras);
        return fragment;
    }
    // endregion

    // region Lifecycle Methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
//            mQuery = getArguments().getString("query");
        }

        AccessToken token = PreferencesHelper.getAccessToken(getActivity());
        vimeoService = ServiceGenerator.createService(
                VimeoService.class,
                VimeoService.BASE_URL,
                token);

        BusProvider.getInstance().register(this);

        font = FontCache.getTypeface("Ubuntu-Medium.ttf", getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_explore, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(ab != null){
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(TrestleUtility.getFormattedText(getString(R.string.explore), font));
        }

        LinearLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        categoriesRecyclerView.setLayoutManager(layoutManager);
        categoriesRecyclerView.addItemDecoration(new GridSpacesItemDecoration(DisplayUtility.dp2px(getActivity(), 8)));

        categoriesAdapter = new CategoriesAdapter();
        categoriesAdapter.setOnItemClickListener(this);

        categoriesRecyclerView.setItemAnimator(new SlideInUpAnimator());
        categoriesRecyclerView.setAdapter(categoriesAdapter);

        Call getCategoriesCall = vimeoService.getCategories();
        calls.add(getCategoriesCall);
        getCategoriesCall.enqueue(getCategoriesCallback);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister Otto Bus
        BusProvider.getInstance().unregister(this);
    }
    // endregion

    // region CategoriesAdapter.OnItemClickListener Methods
    @Override
    public void onItemClick(int position, View view) {

        Category category = categoriesAdapter.getItem(position);
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
