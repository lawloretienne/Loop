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
import com.etiennelawlor.loop.models.AccessToken;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoService;
import com.etiennelawlor.loop.network.models.response.CategoriesEnvelope;
import com.etiennelawlor.loop.network.models.response.Category;
import com.etiennelawlor.loop.prefs.LoopPrefs;
import com.etiennelawlor.loop.ui.GridSpacesItemDecoration;
import com.etiennelawlor.loop.ui.LoadingImageView;
import com.etiennelawlor.loop.utilities.DisplayUtility;
import com.etiennelawlor.loop.utilities.FontCache;
import com.etiennelawlor.loop.utilities.NetworkLogUtility;
import com.etiennelawlor.loop.utilities.NetworkUtility;
import com.etiennelawlor.loop.utilities.TrestleUtility;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 6/13/15.
 */
public class ExploreFragment extends BaseFragment implements CategoriesAdapter.OnItemClickListener {

    // region Views
    @Bind(R.id.rv)
    RecyclerView recyclerView;
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
    // endregion

    // region Member Variables
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
    private Callback<CategoriesEnvelope> getCategoriesCallback = new Callback<CategoriesEnvelope>() {
        @Override
        public void onResponse(Call<CategoriesEnvelope> call, Response<CategoriesEnvelope> response) {
            loadingImageView.setVisibility(View.GONE);

            if (!response.isSuccessful()) {
                int responseCode = response.code();
                if(responseCode == 504) { // 504 Unsatisfiable Request (only-if-cached)
                    errorTextView.setText("Can't load data.\nCheck your network connection.");
                    errorLinearLayout.setVisibility(View.VISIBLE);
                }
                return;
            }

            CategoriesEnvelope categoriesEnvelope = response.body();
            if (categoriesEnvelope != null) {
                List<Category> categories = categoriesEnvelope.getCategories();
                categoriesAdapter.addAll(categories);
            }
        }

        @Override
        public void onFailure(Call<CategoriesEnvelope> call, Throwable t) {
            NetworkLogUtility.logFailure(call, t);

            if (!call.isCanceled()){
                loadingImageView.setVisibility(View.GONE);

                if(NetworkUtility.isKnownException(t)){
                    errorTextView.setText("Can't load data.\nCheck your network connection.");
                    errorLinearLayout.setVisibility(View.VISIBLE);
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

        AccessToken token = LoopPrefs.getAccessToken(getActivity());
        vimeoService = ServiceGenerator.createService(
                VimeoService.class,
                VimeoService.BASE_URL,
                token);

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
            ab.setHomeAsUpIndicator(R.drawable.ic_menu_light);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(TrestleUtility.getFormattedText(getString(R.string.explore), font));
        }

        LinearLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new GridSpacesItemDecoration(DisplayUtility.dp2px(getActivity(), 8)));

        categoriesAdapter = new CategoriesAdapter();
        categoriesAdapter.setOnItemClickListener(this);

        recyclerView.setItemAnimator(new SlideInUpAnimator());
        recyclerView.setAdapter(categoriesAdapter);

        Call getCategoriesCall = vimeoService.getCategories();
        calls.add(getCategoriesCall);
        getCategoriesCall.enqueue(getCategoriesCallback);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
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
