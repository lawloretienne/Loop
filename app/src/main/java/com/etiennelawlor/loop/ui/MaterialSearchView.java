package com.etiennelawlor.loop.ui;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.speech.RecognizerIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.adapters.SuggestionsAdapter;
import com.etiennelawlor.loop.otto.BusProvider;
import com.etiennelawlor.loop.otto.events.FilterClickedEvent;
import com.etiennelawlor.loop.otto.events.SearchPerformedEvent;
import com.etiennelawlor.loop.otto.events.ShowSearchSuggestionsEvent;
import com.etiennelawlor.loop.otto.events.LeftDrawableClickedEvent;
import com.etiennelawlor.loop.realm.RealmUtility;
import com.etiennelawlor.loop.utilities.LoopUtility;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by etiennelawlor on 10/6/15.
 */
public class MaterialSearchView extends FrameLayout implements
        SuggestionsAdapter.OnItemClickListener,
        SuggestionsAdapter.OnItemLongClickListener,
        SuggestionsAdapter.OnSearchSuggestionCompleteClickListener {

    // region Constants
    public static final int REQUEST_VOICE = 9999;
    // endregion

    // region Member Variables
    private boolean mAreSearchSuggestionsVisible;
    private DividerItemDecoration mDividerItemDecoration;
    private Integer mLeftDrawableType;
    private String mHintText;
    private int mMarginTop;
    private int mMarginBottom;
    private int mMarginLeft;
    private int mMarginRight;
    private SuggestionsAdapter mSuggestionsAdapter = new SuggestionsAdapter();
    private boolean mIsSearchEditTextFocused = false;

    @Bind(R.id.search_et)
    EditText mSearchEditText;
    @Bind(R.id.microphone_iv)
    ImageView mMicrophoneImageView;
    @Bind(R.id.clear_iv)
    ImageView mClearImageView;
    @Bind(R.id.filter_iv)
    ImageView mFilterImageView;
    @Bind(R.id.cv)
    CardView mCardView;
    @Bind(R.id.left_drawable_iv)
    ImageView mLeftDrawableImageView;
    @Bind(R.id.left_drawable_riv)
    CircleImageView mLeftDrawableRoundedImageView;
    @Bind(R.id.divider_v)
    View mDividerView;
    @Bind(R.id.bg_cover_fl)
    FrameLayout mBackgroundCoverFrameLayout;
    @Bind(R.id.rv)
    RecyclerView mRecyclerView;
    // endregion

    // region Listeners
    @OnClick(R.id.bg_cover_fl)
    public void backgroundCoverFrameLayoutClicked() {
        if (mAreSearchSuggestionsVisible) {
            hideSearchSuggestions();
        }
    }

    @OnClick(R.id.microphone_iv)
    public void microphoneImageViewClicked() {
        if (isVoiceAvailable()) {
            hideSearchSuggestions();

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now");
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

            ((Activity) ((ContextWrapper) mMicrophoneImageView.getContext()).getBaseContext()).startActivityForResult(intent, REQUEST_VOICE);
        }
    }

    @OnClick(R.id.left_drawable_iv)
    public void leftDrawableImageViewClicked() {
        if (mAreSearchSuggestionsVisible) {
            hideSearchSuggestions();
        } else {
            LeftDrawableClickedEvent.Type type = null;
            switch (mLeftDrawableType) {
                case 0:
                    type = LeftDrawableClickedEvent.Type.MENU;
                    break;
                case 1:
                    type = LeftDrawableClickedEvent.Type.BACK;
                    break;
                case 2:
                    type = LeftDrawableClickedEvent.Type.SEARCH;
                    mSearchEditText.requestFocus();
                default:
                    break;
            }

            BusProvider.get().post(new LeftDrawableClickedEvent(type));
        }
    }

    @OnClick(R.id.clear_iv)
    public void clearImageViewClicked() {
        setQuery("");
    }

    @OnTextChanged(R.id.search_et)
    public void onSearchEditTextTextChanged(CharSequence text) {
        if (text.length() > 0) {
            mMicrophoneImageView.setVisibility(View.GONE);
            mClearImageView.setVisibility(View.VISIBLE);
        } else {
            mClearImageView.setVisibility(View.GONE);
            mMicrophoneImageView.setVisibility(View.VISIBLE);
        }

        if (mIsSearchEditTextFocused) {
            mSuggestionsAdapter.setCurrentQuery(text.toString());
            BusProvider.get().post(new ShowSearchSuggestionsEvent(text.toString()));
        }

        mFilterImageView.setVisibility(View.GONE);
    }

    @OnClick(R.id.search_et)
    public void searchEditTextClicked() {
        mSearchEditText.requestFocus();
    }

    @OnFocusChange(R.id.search_et)
    public void onSearchEditTextFocusChanged(boolean focused) {
        mIsSearchEditTextFocused = focused;

        if (mIsSearchEditTextFocused) {
            if (!mAreSearchSuggestionsVisible) {
                showSearchSuggestions();
            }
            LoopUtility.showKeyboard(getContext(), mSearchEditText);
        } else {
            LoopUtility.hideKeyboard(getContext(), mSearchEditText);
        }
    }

    private OnClickListener mFilterImageViewOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            BusProvider.get().post(new FilterClickedEvent());
        }
    };
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

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event != null) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (mAreSearchSuggestionsVisible) {
                    hideSearchSuggestions();
                }
            }
        }

        return true;
    }

    // region SuggestionsAdapter.OnItemClickListener Methods
    @Override
    public void onItemClick(int position, View view) {
        TextView suggestionTextView = (TextView) view.findViewById(R.id.suggestion_tv);
        String suggestion = suggestionTextView.getText().toString();

        hideSearchSuggestions();
        BusProvider.get().post(new SearchPerformedEvent(suggestion));
    }
    // endregion

    // region SuggestionsAdapter.OnItemLongClickListener Methods

    @Override
    public void onItemLongClick(int position, View view) {
        TextView suggestionTextView = (TextView) view.findViewById(R.id.suggestion_tv);
        final String suggestion = suggestionTextView.getText().toString();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext(), R.style.DialogTheme);
        alertDialogBuilder.setMessage("Remove from search history?");
        alertDialogBuilder.setPositiveButton(getContext().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RealmUtility.deleteQuery(suggestion);
                BusProvider.get().post(new ShowSearchSuggestionsEvent(getQuery()));
                dialog.dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton(getContext().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialogBuilder.show();
    }
    // endregion

    // region SuggestionsAdapter.OnSearchSuggestionCompleteClickListener Methods
    @Override
    public void onSearchSuggestionCompleteClickListener(int position, TextView textView) {
        mSearchEditText.setText(textView.getText().toString());
        int textLength = mSearchEditText.getText().length();
        mSearchEditText.setSelection(textLength, textLength);
    }
    // endregion

    // region Helper Methods
    private void init(AttributeSet attrs) {
//        if (isInEditMode()) {
//            return;
//        }

        LayoutInflater.from(getContext()).inflate((R.layout.material_search_view), this, true);
        ButterKnife.bind(this);

        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.MaterialSearchView, 0, 0);
            try {
                mLeftDrawableType = a.getInteger(R.styleable.MaterialSearchView_leftDrawableType, 1);
                mHintText = a.getString(R.styleable.MaterialSearchView_hintText);
                mMarginTop = a.getDimensionPixelSize(R.styleable.MaterialSearchView_layout_marginTop, 0);
                mMarginBottom = a.getDimensionPixelSize(R.styleable.MaterialSearchView_layout_marginBottom, 0);
                mMarginLeft = a.getDimensionPixelSize(R.styleable.MaterialSearchView_layout_marginLeft, 0);
                mMarginRight = a.getDimensionPixelSize(R.styleable.MaterialSearchView_layout_marginRight, 0);

            } finally {
                a.recycle();
            }
        }

        setUpLeftDrawable(false);
        setUpCardView();
        setUpHintText();
        setUpListeners();
    }

    private void setUpCardView() {
        LayoutParams params = new LayoutParams(
                mCardView.getLayoutParams());
        params.topMargin = mMarginTop;
        params.bottomMargin = mMarginBottom;
        params.leftMargin = mMarginLeft;
        params.rightMargin = mMarginRight;

        mCardView.setLayoutParams(params);
    }

    private void setUpListeners() {
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideSearchSuggestions();
                    BusProvider.get().post(new SearchPerformedEvent(getQuery()));
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void setUpLeftDrawable(boolean showingSearchSuggestions) {
        if (showingSearchSuggestions) {
            mLeftDrawableImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_arrow_back_black_24dp));
            mLeftDrawableRoundedImageView.setVisibility(View.GONE);
            mLeftDrawableImageView.setVisibility(View.VISIBLE);
        } else {
            switch (mLeftDrawableType) {
                case 0:
                    mLeftDrawableImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu_black_24dp));
                    break;
                case 1:
                    mLeftDrawableImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_arrow_back_black_24dp));
                    break;
                case 2:
                    mLeftDrawableImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_search_black_24dp));
                    break;
                case 3:
                    break;
                default:
                    break;
            }

            if (mLeftDrawableType == 3) {
                mLeftDrawableImageView.setVisibility(View.GONE);
                mLeftDrawableRoundedImageView.setVisibility(View.VISIBLE);
            } else {
                mLeftDrawableRoundedImageView.setVisibility(View.GONE);
                mLeftDrawableImageView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setUpHintText() {
        mSearchEditText.setHint(mHintText);
    }

    private void showSearchSuggestions() {
        BusProvider.get().post(new ShowSearchSuggestionsEvent(getQuery()));

        mSuggestionsAdapter.setOnItemClickListener(this);
        mSuggestionsAdapter.setOnItemLongClickListener(this);
        mSuggestionsAdapter.setOnSearchSuggestionCompleteClickListener(this);

        CustomLinearLayoutManager layoutManager = new CustomLinearLayoutManager(getContext());
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);

        mDividerItemDecoration = new DividerItemDecoration(getResources().getDrawable(R.drawable.divider));
        mRecyclerView.addItemDecoration(mDividerItemDecoration);
//        mRecyclerView.setItemAnimator(new SlideInUpAnimator());
        mRecyclerView.setAdapter(mSuggestionsAdapter);

        if (mSuggestionsAdapter.getItemCount() > 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mDividerView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mDividerView.setVisibility(View.GONE);
        }

        mBackgroundCoverFrameLayout.setVisibility(View.VISIBLE);

        setUpLeftDrawable(true);

        mAreSearchSuggestionsVisible = true;
    }

    private void hideSearchSuggestions() {
        mDividerView.setVisibility(View.GONE);
        mBackgroundCoverFrameLayout.setVisibility(View.GONE);

        setUpLeftDrawable(false);

        mRecyclerView.setVisibility(View.GONE);
        mRecyclerView.removeItemDecoration(mDividerItemDecoration);

        mSearchEditText.clearFocus();
        mAreSearchSuggestionsVisible = false;
    }

    private boolean isVoiceAvailable() {
        PackageManager pm = getContext().getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return (activities.size() != 0);
    }

    public void setQuery(String query) {
        mSearchEditText.setText(query);
        mSuggestionsAdapter.setCurrentQuery(query);
        if (!TextUtils.isEmpty(query))
            mFilterImageView.setVisibility(View.VISIBLE);
    }

    public String getQuery() {
        return mSearchEditText.getText().toString();
    }

    public void addSuggestions(List<String> suggestions) {
        mSuggestionsAdapter.clear();
        mSuggestionsAdapter.addAll(suggestions);

        if (mSuggestionsAdapter.getItemCount() > 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mDividerView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mDividerView.setVisibility(View.GONE);
        }

        mBackgroundCoverFrameLayout.setVisibility(View.VISIBLE);
    }

    public void enableFilter() {
        mFilterImageView.setOnClickListener(mFilterImageViewOnClickListener);
        mFilterImageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.grey_700));
    }

    public void disableFilter() {
        mFilterImageView.setOnClickListener(null);
        mFilterImageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.grey_300));
    }
    // endregion
}
