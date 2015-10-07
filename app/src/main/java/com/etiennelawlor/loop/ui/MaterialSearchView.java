package com.etiennelawlor.loop.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.adapters.SuggestionsAdapter;
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

    @Bind(R.id.search_et)
    EditText mSearchEditText;
    @Bind(R.id.microphone_iv)
    ImageView mMicrophoneImageView;
    @Bind(R.id.clear_iv)
    ImageView mClearImageView;
    @Bind(R.id.cv)
    CardView mCardView;
    @Bind(R.id.user_avatar_riv)
    CircleImageView mUserAvatarCircleImageView;
    @Bind(R.id.back_iv)
    ImageView mBackImageView;
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
    @OnClick(R.id.custom_search_view_ll)
    public void searchViewLinearLayoutClicked(){
//        if(!mAreSearchSuggestionsVisible){
//            showSearchSuggestions();
//        }
        mSearchEditText.requestFocus();
    }

    @OnClick(R.id.bg_cover_fl)
    public void backgroundCoverFrameLayoutClicked(){
        if(mAreSearchSuggestionsVisible){
            hideSearchSuggestions();
        }
    }

    @OnClick(R.id.microphone_iv)
    public void microphoneImageViewClicked(){

    }

    @OnClick(R.id.back_iv)
    public void backImageViewClicked(){
        if(mAreSearchSuggestionsVisible){
            hideSearchSuggestions();
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
        if(!mAreSearchSuggestionsVisible){
            showSearchSuggestions();
        }
    }

    @OnFocusChange(R.id.search_et)
    public void onSearchEditTextFocusChanged(boolean focused) {
        Timber.d("onSearchEditTextFocusChanged() : focused - "+focused);

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
            TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CustomFontTextView, 0, 0);
            try {
//                Integer position = a.getInteger(R.styleable.CustomFontTextView_textFont, 10);
//                setTypeface(TypefaceUtil.getTypeface(Typefaces.from(position)));
            } finally {
                a.recycle();
            }
        } else {
//            setTypeface(TypefaceUtil.getTypeface(Typefaces.ROBOTO_REGULAR));
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
//                suggestions.add("Snowboarding");
//                suggestions.add("Skiing");
//                suggestions.add("Skateboarding");
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

        mRecyclerView.setVisibility(View.VISIBLE);
        mDividerView.setVisibility(View.VISIBLE);

        mUserAvatarCircleImageView.setVisibility(View.GONE);
        mBackImageView.setVisibility(View.VISIBLE);

        mAreSearchSuggestionsVisible = true;
    }

    private void hideSearchSuggestions(){
        mDividerView.setVisibility(View.GONE);
        mBackgroundCoverFrameLayout.setVisibility(View.GONE);

        mBackImageView.setVisibility(View.GONE);
        mUserAvatarCircleImageView.setVisibility(View.VISIBLE);

        mRecyclerView.setVisibility(View.GONE);
        mRecyclerView.removeItemDecoration(mDividerItemDecoration);

        mAreSearchSuggestionsVisible = false;
        mSearchEditText.clearFocus();
    }
    // endregion
}
