package com.etiennelawlor.loop.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.activities.SearchableActivity;
import com.etiennelawlor.loop.activities.VideoDetailsActivity;
import com.etiennelawlor.loop.adapters.VideosAdapter;
import com.etiennelawlor.loop.bus.RxBus;
import com.etiennelawlor.loop.bus.events.FilterClickedEvent;
import com.etiennelawlor.loop.bus.events.SearchPerformedEvent;
import com.etiennelawlor.loop.bus.events.ShowSearchSuggestionsEvent;
import com.etiennelawlor.loop.models.AccessToken;
import com.etiennelawlor.loop.network.ServiceGenerator;
import com.etiennelawlor.loop.network.VimeoService;
import com.etiennelawlor.loop.network.interceptors.AuthorizedNetworkInterceptor;
import com.etiennelawlor.loop.network.models.response.Video;
import com.etiennelawlor.loop.network.models.response.VideosEnvelope;
import com.etiennelawlor.loop.prefs.LoopPrefs;
import com.etiennelawlor.loop.realm.RealmUtility;
import com.etiennelawlor.loop.ui.LoadingImageView;
import com.etiennelawlor.loop.ui.MaterialSearchView;
import com.etiennelawlor.loop.utilities.NetworkLogUtility;
import com.etiennelawlor.loop.utilities.NetworkUtility;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class SearchableFragment extends BaseFragment implements VideosAdapter.OnItemClickListener, VideosAdapter.OnReloadClickListener {

    // region Constants
    public static final int PAGE_SIZE = 30;
    // endregion

    // region Views
    @Bind(R.id.rv)
    RecyclerView recyclerView;
    @Bind(android.R.id.empty)
    View emptyView;
    @Bind(R.id.empty_tv)
    TextView emptyTextView;
    @Bind(R.id.iv)
    ImageView imageView;
    @Bind(R.id.loading_iv)
    LoadingImageView loadingImageView;
    @Bind(R.id.error_ll)
    LinearLayout errorLinearLayout;
    @Bind(R.id.error_tv)
    TextView errorTextView;
    @Bind(R.id.material_sv)
    MaterialSearchView materialSearchView;
//    @Bind(R.id.toolbar)
//    Toolbar toolbar;
    // endregion

    // region Member Variables
    private boolean isLastPage = false;
    private int currentPage = 1;
    private int selectedSortByKey = 0;
    private int selectedSortOrderKey = 1;
    private boolean isLoading = false;
    private String sortByValue = "relevant";
    private String sortOrderValue = "desc";
    private String filter = "CC";
    private VideosAdapter videosAdapter;
    private String query;
    private LinearLayoutManager layoutManager;
    private VimeoService vimeoService;
    private CompositeSubscription compositeSubscription;
    // endregion

    // region Listeners
    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = layoutManager.getChildCount();
            int totalItemCount = layoutManager.getItemCount();
            int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

            if (!isLoading && !isLastPage) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
                    loadMoreItems();
                }
            }
        }
    };

    @OnClick(R.id.reload_btn)
    public void onReloadButtonClicked() {
        errorLinearLayout.setVisibility(View.GONE);
        loadingImageView.setVisibility(View.VISIBLE);

        materialSearchView.disableFilter();

        Call findVideosCall = vimeoService.findVideos(query,
                sortByValue,
                sortOrderValue,
                currentPage,
                PAGE_SIZE,
                filter);
        calls.add(findVideosCall);
        findVideosCall.enqueue(findVideosFirstFetchCallback);
    }
    // endregion

    // region Callbacks
    private Callback<VideosEnvelope> findVideosFirstFetchCallback = new Callback<VideosEnvelope>() {
        @Override
        public void onResponse(Call<VideosEnvelope> call, Response<VideosEnvelope> response) {
            loadingImageView.setVisibility(View.GONE);
            isLoading = false;

            if (!response.isSuccessful()) {
                int responseCode = response.code();
                if(responseCode == 504) { // 504 Unsatisfiable Request (only-if-cached)
                    errorTextView.setText("Can't load data.\nCheck your network connection.");
                    errorLinearLayout.setVisibility(View.VISIBLE);
                }
                return;
            }

            VideosEnvelope videosEnvelope = response.body();
            if (videosEnvelope != null) {
                List<Video> videos = videosEnvelope.getVideos();
                if (videos != null) {
                    if(videos.size()>0)
                        videosAdapter.addAll(videos);

                    if(videos.size() >= PAGE_SIZE){
                        videosAdapter.addFooter();
                    } else {
                        isLastPage = true;
                    }
                }
            }

            materialSearchView.enableFilter();

            if (videosAdapter.isEmpty()) {
                emptyTextView.setText("There are no videos matching your search");
//                Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_watch_later_large);
//                DrawableCompat.setTint(drawable, ContextCompat.getColor(getActivity(), R.color.grey_500));
//                emptyTextView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
                imageView.setImageResource(R.drawable.ic_watch_now_large);
                emptyView.setVisibility(View.VISIBLE);

            }
        }

        @Override
        public void onFailure(Call<VideosEnvelope> call, Throwable t) {
            NetworkLogUtility.logFailure(call, t);

            if (!call.isCanceled()){
                isLoading = false;
                loadingImageView.setVisibility(View.GONE);

                if(NetworkUtility.isKnownException(t)){
                    errorTextView.setText("Can't load data.\nCheck your network connection.");
                    errorLinearLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    private Callback<VideosEnvelope> findVideosNextFetchCallback = new Callback<VideosEnvelope>() {
        @Override
        public void onResponse(Call<VideosEnvelope> call, Response<VideosEnvelope> response) {
            videosAdapter.removeFooter();
            isLoading = false;

            if (!response.isSuccessful()) {
                int responseCode = response.code();
                switch (responseCode){
                    case 504: // 504 Unsatisfiable Request (only-if-cached)
                        break;
                    case 400:
                        isLastPage = true;
                        break;
                }
                return;
            }

            VideosEnvelope videosEnvelope = response.body();
            if (videosEnvelope != null) {
                List<Video> videos = videosEnvelope.getVideos();
                if (videos != null) {
                    if(videos.size()>0)
                        videosAdapter.addAll(videos);

                    if(videos.size() >= PAGE_SIZE){
                        videosAdapter.addFooter();
                    } else {
                        isLastPage = true;
                    }
                }
            }
        }

        @Override
        public void onFailure(Call<VideosEnvelope> call, Throwable t) {
            NetworkLogUtility.logFailure(call, t);

            if (!call.isCanceled()){
                if(NetworkUtility.isKnownException(t)){
                    videosAdapter.updateFooter(VideosAdapter.FooterType.ERROR);
                }
            }
        }
    };
    // endregion

    // region Constructors
    public SearchableFragment() {
    }
    // endregion

    // region Factory Methods
    public static SearchableFragment newInstance() {
        return new SearchableFragment();
    }

    public static SearchableFragment newInstance(Bundle extras) {
        SearchableFragment fragment = new SearchableFragment();
        fragment.setArguments(extras);
        return fragment;
    }
    // endregion

    // region Lifecycle Methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        if(getArguments() != null){
            query = getArguments().getString(SearchManager.QUERY);

            RealmUtility.saveQuery(query);

//            performSearch(query);
        }

        AccessToken token = LoopPrefs.getAccessToken(getActivity());
        vimeoService = ServiceGenerator.createService(
                VimeoService.class,
                VimeoService.BASE_URL,
                new AuthorizedNetworkInterceptor(token));

        setHasOptionsMenu(true);

        compositeSubscription = new CompositeSubscription();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_searchable, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
//
//        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
//        ab.setDisplayHomeAsUpEnabled(true);
//        ab.setTitle("");

        setupSearchView();

        setUpRxBusSubscription();


//        mSearchViewWidget.setQuery(mQuery, false);
//
//        mSearchViewWidget.setOnQueryTextListener(new SearchViewWidget2.OnQueryTextListener() {
//
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                Intent intent = new Intent(mSearchViewWidget.getContext(), SearchableActivity.class);
//                intent.setAction(Intent.ACTION_SEARCH);
//                intent.putExtra(SearchManager.QUERY, query);
//                mSearchViewWidget.getContext().startActivity(intent);
//
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                return false;
//            }
//        });
//
//        mSearchViewWidget.setOnSearchViewListener(new SearchViewWidget2.SearchViewListener() {
//
//            @Override
//            public void onSearchViewShown() {
//            }
//
//            @Override
//            public void onSearchViewClosed() {
//            }
//        });
//
//        List<SearchViewItem> mSuggestionsList = new ArrayList<>();
//        mSuggestionsList.add(new SearchViewItem(R.drawable.ic_search_black_24dp, "Wi-Fi"));
//        mSuggestionsList.add(new SearchViewItem(R.drawable.ic_search_black_24dp, "Bluetooth"));
//        mSuggestionsList.add(new SearchViewItem(R.drawable.ic_search_black_24dp, "GPS"));
//        mSuggestionsList.add(new SearchViewItem(R.drawable.ic_search_black_24dp, "Ad-Hoc"));
//        mSuggestionsList.add(new SearchViewItem(R.drawable.ic_search_black_24dp, "Google"));
//        mSuggestionsList.add(new SearchViewItem(R.drawable.ic_search_black_24dp, "Android"));
//        mSuggestionsList.add(new SearchViewItem(R.drawable.ic_search_black_24dp, "Piconet"));
//        mSuggestionsList.add(new SearchViewItem(R.drawable.ic_search_black_24dp, "Scatternet"));
//
//        List<SearchViewItem> mResultsList = new ArrayList<>();
//        // choose true for Light Theme, false for Dark Theme.
//        SearchViewAdapter mSearchViewAdapter = new SearchViewAdapter(getActivity(), mResultsList, mSuggestionsList, true);
//        mSearchViewAdapter.setOnItemClickListener(new SearchViewAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//                TextView mText = (TextView) view.findViewById(R.id.textView_result);
//                CharSequence text = "Hello toast!";
////                int duration = Toast.LENGTH_SHORT;
////                Toast toast = Toast.makeText(getApplicationContext(), text, duration);
////                toast.show();
//
//            }
//        });
//        mSearchViewWidget.setAdapter(mSearchViewAdapter);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        videosAdapter = new VideosAdapter();
        videosAdapter.setOnItemClickListener(this);
        videosAdapter.setOnReloadClickListener(this);

        recyclerView.setItemAnimator(new SlideInUpAnimator());
        recyclerView.setAdapter(videosAdapter);

        // Pagination
        recyclerView.addOnScrollListener(recyclerViewOnScrollListener);

        Call findVideosCall = vimeoService.findVideos(query,
                sortByValue,
                sortOrderValue,
                currentPage,
                PAGE_SIZE,
                filter);
        calls.add(findVideosCall);
        findVideosCall.enqueue(findVideosFirstFetchCallback);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupSearchView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeListeners();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeSubscription.unsubscribe();
    }
    // endregion

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

//        inflater.inflate(R.menu.searchable_menu, menu);

        // Get the SearchView and set the searchable configuration
//        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        // Assumes current activity is the searchable activity
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
//        searchView.setQueryRefinementEnabled(true);

//        searchView.onActionViewExpanded();
//        searchView.setIconified(false);
//        searchView.setQuery(mQuery, false);
//        searchView.clearFocus();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                // do nothing
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == Activity.RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
//                    mMaterialSearchView.setQuery(searchWrd);
                    launchSearchActivity(searchWrd);
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // region VideosAdapter.OnItemClickListener Methods
    @Override
    public void onItemClick(int position, View view) {
        Video video = videosAdapter.getItem(position);
        if (video != null) {
            Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);

            Bundle bundle = new Bundle();
            bundle.putParcelable(LikedVideosFragment.KEY_VIDEO, video);
            intent.putExtras(bundle);

            Pair<View, String> p1 = Pair.create(view.findViewById(R.id.video_thumbnail_iv), "videoTransition");
//                Pair<View, String> p2 = Pair.create((View) view.findViewById(R.id.title_tv), "titleTransition");
//                Pair<View, String> p3 = Pair.create((View) view.findViewById(R.id.subtitle_tv), "subtitleTransition");
//        Pair<View, String> p4 = Pair.create((View)view.findViewById(R.id.uploaded_tv), "uploadedTransition");

//                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
//                        p1, p2, p3);

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                    p1);

//            ActivityCompat.startActivity(getActivity(), intent, options.toBundle());

                startActivity(intent);
        }

    }
    // endregion

    // region VideosAdapter.OnReloadClickListener Methods

    @Override
    public void onReloadClick() {
        videosAdapter.updateFooter(VideosAdapter.FooterType.LOAD_MORE);

        Call findVideosCall = vimeoService.findVideos(query,
                sortByValue,
                sortOrderValue,
                currentPage,
                PAGE_SIZE,
                filter);
        calls.add(findVideosCall);
        findVideosCall.enqueue(findVideosNextFetchCallback);
    }

    // endregion

    // region Helper Methods
    private void loadMoreItems() {
        isLoading = true;

        currentPage += 1;

        Call findVideosCall = vimeoService.findVideos(query,
                sortByValue,
                sortOrderValue,
                currentPage,
                PAGE_SIZE,
                filter);
        calls.add(findVideosCall);
        findVideosCall.enqueue(findVideosNextFetchCallback);
    }

    private void showSortDialog() {
        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptsView = li.inflate(R.layout.sort_dialog, null);
        final Spinner sortBySpinner = (Spinner) promptsView.findViewById(R.id.sort_by_s);
        final Spinner sortOrderSpinner = (Spinner) promptsView.findViewById(R.id.sort_order_s);

        String[] mSortByKeysArray = getResources().getStringArray(R.array.videos_sort_by_keys);
        ArrayAdapter<String> sortByAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mSortByKeysArray);
        sortBySpinner.setAdapter(sortByAdapter);

        String[] mSortOrderKeysArray = getResources().getStringArray(R.array.videos_sort_order_keys);
        ArrayAdapter<String> sortOrderAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mSortOrderKeysArray);
        sortOrderSpinner.setAdapter(sortOrderAdapter);

        sortBySpinner.setSelection(selectedSortByKey);
        sortOrderSpinner.setSelection(selectedSortOrderKey);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
        alertDialogBuilder.setView(promptsView);
        alertDialogBuilder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedSortByKey = sortBySpinner.getSelectedItemPosition();
                selectedSortOrderKey = sortOrderSpinner.getSelectedItemPosition();

                String[] sortByValues = getResources().getStringArray(R.array.videos_sort_by_values);
                sortByValue = sortByValues[selectedSortByKey];

                String[] sortOrderValues = getResources().getStringArray(R.array.videos_sort_order_values);
                sortOrderValue = sortOrderValues[selectedSortOrderKey];

                videosAdapter.clear();

                loadingImageView.setVisibility(View.VISIBLE);

                currentPage = 1;

                materialSearchView.disableFilter();

                Call findVideosCall = vimeoService.findVideos(query,
                        sortByValue,
                        sortOrderValue,
                        currentPage,
                        PAGE_SIZE,
                        filter);
                calls.add(findVideosCall);
                findVideosCall.enqueue(findVideosFirstFetchCallback);

                dialog.dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialogBuilder.show();
    }

    private void launchSearchActivity(String query){
        Intent intent = new Intent(getContext(), SearchableActivity.class);
        intent.setAction(Intent.ACTION_SEARCH);
        Bundle bundle = new Bundle();
        bundle.putString(SearchManager.QUERY, query);
        intent.putExtras(bundle);
        getContext().startActivity(intent);
        getActivity().overridePendingTransition(0, 0);
    }

    private void setupSearchView(){
        materialSearchView.setQuery(query);
    }

    private void removeListeners(){
        recyclerView.removeOnScrollListener(recyclerViewOnScrollListener);
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

                        if(event instanceof FilterClickedEvent) {
                            showSortDialog();
                        } else if(event instanceof SearchPerformedEvent) {
                            String query = ((SearchPerformedEvent)event).getQuery();
                            if (!TextUtils.isEmpty(query)) {
                                launchSearchActivity(query);
                            }
                        } else if(event instanceof ShowSearchSuggestionsEvent) {
                            String query = ((ShowSearchSuggestionsEvent)event).getQuery();

                            materialSearchView.addSuggestions(RealmUtility.getSuggestions(query));
                        }
                    }
                });

        compositeSubscription.add(rxBusSubscription);
    }
    // endregion
}
