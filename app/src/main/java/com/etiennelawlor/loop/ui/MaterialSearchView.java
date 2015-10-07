package com.etiennelawlor.loop.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.activities.SearchableActivity;
import com.etiennelawlor.loop.adapters.SuggestionsAdapter;
import com.etiennelawlor.loop.otto.BusProvider;
import com.etiennelawlor.loop.otto.events.UpNavigationClickedEvent;
import com.etiennelawlor.loop.utilities.LoopUtility;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 10/6/15.
 */
public class MaterialSearchView extends FrameLayout implements SuggestionsAdapter.OnItemClickListener, SuggestionsAdapter.OnSearchSuggestionCompleteClickListener {

    // region Member Variables
    private boolean mAreSearchSuggestionsVisible;
    private DividerItemDecoration mDividerItemDecoration;
    private Integer mDefaultUpNavIcon;

    @Bind(R.id.search_et)
    EditText mSearchEditText;
    @Bind(R.id.microphone_iv)
    ImageView mMicrophoneImageView;
    @Bind(R.id.clear_iv)
    ImageView mClearImageView;
    @Bind(R.id.cv)
    CardView mCardView;
//    @Bind(R.id.user_avatar_riv)
//    CircleImageView mUserAvatarCircleImageView;
//    @Bind(R.id.back_iv)
//    ImageView mBackImageView;
    @Bind(R.id.up_navigation_iv)
    ImageView mUpNavigationImageView;

    @Bind(R.id.custom_search_view_ll)
    LinearLayout mCustomSearchViewLinearLayout;
    @Bind(R.id.divider_v)
    View mDividerView;
    @Bind(R.id.bg_cover_fl)
    FrameLayout mBackgroundCoverFrameLayout;
    @Bind(R.id.rv)
    RecyclerView mRecyclerView;
    // endregion

    // region Listeners
//    @OnClick(R.id.custom_search_view_ll)
//    public void searchViewLinearLayoutClicked(){
////        if(!mAreSearchSuggestionsVisible){
////            showSearchSuggestions();
////        }
//        mSearchEditText.requestFocus();
//    }

    @OnClick(R.id.bg_cover_fl)
    public void backgroundCoverFrameLayoutClicked(){
        if(mAreSearchSuggestionsVisible){
            hideSearchSuggestions();
        }
    }

    @OnClick(R.id.microphone_iv)
    public void microphoneImageViewClicked(){

    }

    @OnClick(R.id.up_navigation_iv)
    public void upNavigationImageViewClicked(){
        if(mAreSearchSuggestionsVisible){
            hideSearchSuggestions();
        } else {
            Timber.d("Do something else");

            UpNavigationClickedEvent.Type type = null;
            switch (mDefaultUpNavIcon){
                case 0:
                    type = UpNavigationClickedEvent.Type.MENU;
                    break;
                case 1:
                    type = UpNavigationClickedEvent.Type.BACK;
                    break;
                default:
                    break;
            }

            BusProvider.get().post(new UpNavigationClickedEvent(type));
        }
    }

    @OnClick(R.id.clear_iv)
    public void clearImageViewClicked(){
        mSearchEditText.setText("");
    }

    @OnTextChanged(R.id.search_et)
    public void onSearchEditTextTextChanged(CharSequence text){
        if (text.length() > 0) {
            mMicrophoneImageView.setVisibility(View.GONE);
            mClearImageView.setVisibility(View.VISIBLE);
        } else {
            mClearImageView.setVisibility(View.GONE);
            mMicrophoneImageView.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.search_et)
    public void searchEditTextClicked(){
        mSearchEditText.requestFocus();
    }

    @OnFocusChange(R.id.search_et)
    public void onSearchEditTextFocusChanged(boolean focused) {
        Timber.d("onSearchEditTextFocusChanged() : focused - " + focused);

        if(focused){
            if(!mAreSearchSuggestionsVisible){
                showSearchSuggestions();
            }
            LoopUtility.showKeyboard(getContext(), mSearchEditText);
        } else {
            LoopUtility.hideKeyboard(getContext(), mSearchEditText);
        }
    }
    // endregion

    // region Constructors
    public MaterialSearchView(Context context) {
        super(context);
        init(null);
    }

    public MaterialSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MaterialSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }
    // endregion

    // region SuggestionsAdapter.OnSearchSuggestionCompleteClickListener Methods
    @Override
    public void onSearchSuggestionCompleteClickListener(int position, TextView textView) {
        mSearchEditText.setText(textView.getText());
        int textLength = mSearchEditText.getText().length();
        mSearchEditText.setSelection(textLength, textLength);
    }
    // endregion

    // region SuggestionsAdapter.OnItemClickListener Methods
    @Override
    public void onItemClick(int position, View view) {
//        Timber.d("");
        Timber.d("SuggestionsAdapter : onItemClick()");

        TextView suggestionTextView = (TextView) view.findViewById(R.id.suggestion_tv);
        String suggestion = suggestionTextView.getText().toString();
//        Timber.d("SuggestionsAdapter : onItemClick() : suggestionTextView.getText() - "+suggestionTextView.getText());

        hideSearchSuggestions();

        Intent intent = new Intent(getContext(), SearchableActivity.class);
        intent.setAction(Intent.ACTION_SEARCH);
        intent.putExtra(SearchManager.QUERY, suggestion);
        getContext().startActivity(intent);

//        setQuery("");
    }
    // endregion

    // region Helper Methods
    private void init(AttributeSet attrs){
//        if (isInEditMode()) {
//            return;
//        }

        LayoutInflater.from(getContext()).inflate((R.layout.material_search_view), this, true);
        ButterKnife.bind(this);

        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.MaterialSearchView, 0, 0);
            try {
                mDefaultUpNavIcon = a.getInteger(R.styleable.MaterialSearchView_default_up_nav_icon, 1);
            } finally {
                a.recycle();
            }
        }

        setUpDefaultUpNavIcon();

        setUpListeners();
    }

    private void setUpListeners(){
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    hideSearchSuggestions();

                    Intent intent = new Intent(getContext(), SearchableActivity.class);
                    intent.setAction(Intent.ACTION_SEARCH);
                    intent.putExtra(SearchManager.QUERY, getQuery());
                    getContext().startActivity(intent);

//                    setQuery("");
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void setUpDefaultUpNavIcon(){
        switch (mDefaultUpNavIcon){
            case 0:
                mUpNavigationImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu_black_24dp));
                break;
            case 1:
                mUpNavigationImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_arrow_back_black_24dp));
                break;
            default:
                break;
        }
    }

    private void showSearchSuggestions(){
        Timber.d("mCardView : onClick()");

        mBackgroundCoverFrameLayout.setVisibility(View.VISIBLE);

        SuggestionsAdapter suggestionsAdapter = new SuggestionsAdapter();

        List<String> suggestions = new ArrayList<String>();
        suggestions.add("Bodyboarding");
        suggestions.add("Surfing");
        suggestions.add("Wind");
        suggestions.add("Snowboarding");
        suggestions.add("Skiing");
        suggestions.add("Skateboarding");
//                suggestions.add("BMX");
//                suggestions.add("Motocross");

        suggestionsAdapter.addAll(suggestions);

        suggestionsAdapter.setOnItemClickListener(this);
        suggestionsAdapter.setOnSearchSuggestionCompleteClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);

        mDividerItemDecoration = new DividerItemDecoration(getResources().getDrawable(R.drawable.divider));
        mRecyclerView.addItemDecoration(mDividerItemDecoration);
        mRecyclerView.setAdapter(suggestionsAdapter);

        if(suggestionsAdapter.getItemCount() > 0){
            mRecyclerView.setVisibility(View.VISIBLE);
            mDividerView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mDividerView.setVisibility(View.GONE);
        }

//        mUserAvatarCircleImageView.setVisibility(View.GONE);
//        mBackImageView.setVisibility(View.VISIBLE);

        mUpNavigationImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_arrow_back_black_24dp));

        mAreSearchSuggestionsVisible = true;
    }

    private void hideSearchSuggestions(){
        mDividerView.setVisibility(View.GONE);
        mBackgroundCoverFrameLayout.setVisibility(View.GONE);

//        mBackImageView.setVisibility(View.GONE);
//        mUserAvatarCircleImageView.setVisibility(View.VISIBLE);

//        mUpNavigationImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu_black_24dp));

        setUpDefaultUpNavIcon();

        mRecyclerView.setVisibility(View.GONE);
        mRecyclerView.removeItemDecoration(mDividerItemDecoration);

        mAreSearchSuggestionsVisible = false;
        mSearchEditText.clearFocus();
    }

    public void setQuery(String query){
        mSearchEditText.setText(query);
    }

    public String getQuery(){
        return mSearchEditText.getText().toString();
    }
    // endregion
}
