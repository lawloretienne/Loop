package com.etiennelawlor.loop.ui;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.os.Bundle;
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
import android.widget.Toast;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.adapters.SuggestionsAdapter;
import com.etiennelawlor.loop.otto.BusProvider;
import com.etiennelawlor.loop.otto.events.FilterClickedEvent;
import com.etiennelawlor.loop.otto.events.HideSearchSuggestionsEvent;
import com.etiennelawlor.loop.otto.events.LeftDrawableClickedEvent;
import com.etiennelawlor.loop.otto.events.SearchPerformedEvent;
import com.etiennelawlor.loop.otto.events.ShowSearchSuggestionsEvent;
import com.etiennelawlor.loop.realm.RealmUtility;
import com.etiennelawlor.loop.utilities.DisplayUtility;

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
    public static final int REQUEST_VOICE = 1001;
    public static final int MENU = 0;
    public static final int BACK = 1;
    public static final int SEARCH = 2;
    public static final int AVATAR = 3;
    // endregion

    // region Member Variables
    private boolean areSearchSuggestionsVisible;
    private DividerItemDecoration dividerItemDecoration;
    private int leftDrawableType;
    private String hintText;
    private int marginTop;
    private int marginBottom;
    private int marginLeft;
    private int marginRight;
    private String voicePrompt;
    private SuggestionsAdapter suggestionsAdapter = new SuggestionsAdapter();
    private boolean isSearchEditTextFocused = false;

    @Bind(R.id.search_et)
    EditText searchEditText;
    @Bind(R.id.microphone_iv)
    ImageView microphoneImageView;
    @Bind(R.id.clear_iv)
    ImageView clearImageView;
    @Bind(R.id.filter_iv)
    ImageView filterImageView;
    @Bind(R.id.cv)
    CardView cardView;
    @Bind(R.id.left_drawable_iv)
    ImageView leftDrawableImageView;
    @Bind(R.id.left_drawable_riv)
    CircleImageView leftDrawableRoundedImageView;
    @Bind(R.id.divider_v)
    View dividerView;
    @Bind(R.id.bg_cover_fl)
    FrameLayout backgroundCoverFrameLayout;
    @Bind(R.id.rv)
    RecyclerView recyclerView;
    // endregion

    // region Listeners
    @OnClick(R.id.bg_cover_fl)
    public void backgroundCoverFrameLayoutClicked() {
        if (areSearchSuggestionsVisible) {
            hideSearchSuggestions();
        }
    }

    @OnClick(R.id.microphone_iv)
    public void microphoneImageViewClicked() {
        if (isVoiceAvailable()) {
            hideSearchSuggestions();

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, voicePrompt);
//            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
//            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            Bundle bundle = new Bundle();
            bundle.putString(RecognizerIntent.EXTRA_PROMPT, voicePrompt);
            bundle.putString(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            bundle.putInt(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            intent.putExtras(bundle);

            ((Activity) ((ContextWrapper) microphoneImageView.getContext()).getBaseContext()).startActivityForResult(intent, REQUEST_VOICE);
        } else {
            Toast.makeText(getContext(), "Voice Search is unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.left_drawable_iv)
    public void leftDrawableImageViewClicked() {
        if (areSearchSuggestionsVisible) {
            hideSearchSuggestions();
        } else {
            LeftDrawableClickedEvent.Type type = null;
            switch (leftDrawableType) {
                case 0:
                    type = LeftDrawableClickedEvent.Type.MENU;
                    break;
                case 1:
                    type = LeftDrawableClickedEvent.Type.BACK;
                    break;
                case 2:
                    type = LeftDrawableClickedEvent.Type.SEARCH;
                    searchEditText.requestFocus();
                default:
                    break;
            }

            BusProvider.getInstance().post(new LeftDrawableClickedEvent(type));
        }
    }

    @OnClick(R.id.clear_iv)
    public void clearImageViewClicked() {
        setQuery("");
    }

    @OnTextChanged(R.id.search_et)
    public void onSearchEditTextTextChanged(CharSequence text) {
        if (text.length() > 0) {
            microphoneImageView.setVisibility(View.GONE);
            clearImageView.setVisibility(View.VISIBLE);
        } else {
            clearImageView.setVisibility(View.GONE);
            microphoneImageView.setVisibility(View.VISIBLE);
        }

        if (isSearchEditTextFocused) {
            suggestionsAdapter.setCurrentQuery(text.toString());
            BusProvider.getInstance().post(new ShowSearchSuggestionsEvent(text.toString()));
        }

        filterImageView.setVisibility(View.GONE);
    }

    @OnClick(R.id.search_et)
    public void searchEditTextClicked() {
        searchEditText.requestFocus();
    }

    @OnFocusChange(R.id.search_et)
    public void onSearchEditTextFocusChanged(boolean focused) {
        isSearchEditTextFocused = focused;

        if (isSearchEditTextFocused) {
            if (!areSearchSuggestionsVisible) {
                showSearchSuggestions();
            }
            DisplayUtility.showKeyboard(getContext(), searchEditText);
        } else {
            DisplayUtility.hideKeyboard(getContext(), searchEditText);
        }
    }

    private OnClickListener filterImageViewOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            BusProvider.getInstance().post(new FilterClickedEvent());
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
                if (areSearchSuggestionsVisible) {
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
        BusProvider.getInstance().post(new SearchPerformedEvent(suggestion));
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
                BusProvider.getInstance().post(new ShowSearchSuggestionsEvent(getQuery()));
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
        searchEditText.setText(textView.getText().toString());
        int textLength = searchEditText.getText().length();
        searchEditText.setSelection(textLength, textLength);
    }
    // endregion

    // region Helper Methods
    private void init(AttributeSet attrs) {
        if (!isInEditMode()) {
            LayoutInflater.from(getContext()).inflate((R.layout.material_search_view), this, true);
            ButterKnife.bind(this);

            if (attrs != null) {
                TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.MaterialSearchView, 0, 0);
                try {
                    leftDrawableType = a.getInteger(R.styleable.MaterialSearchView_leftDrawableType, 1);
                    hintText = a.getString(R.styleable.MaterialSearchView_hintText);
                    voicePrompt = a.getString(R.styleable.MaterialSearchView_voicePrompt);
                    marginTop = a.getDimensionPixelSize(R.styleable.MaterialSearchView_layout_marginTop, 0);
                    marginBottom = a.getDimensionPixelSize(R.styleable.MaterialSearchView_layout_marginBottom, 0);
                    marginLeft = a.getDimensionPixelSize(R.styleable.MaterialSearchView_layout_marginLeft, 0);
                    marginRight = a.getDimensionPixelSize(R.styleable.MaterialSearchView_layout_marginRight, 0);
                } finally {
                    a.recycle();
                }
            }

            setUpLeftDrawable(false);
            setUpCardView();
            setUpHintText();
            setUpListeners();
        }
    }

    private void setUpCardView() {
        LayoutParams params = new LayoutParams(
                cardView.getLayoutParams());
        params.topMargin = marginTop;
        params.bottomMargin = marginBottom;
        params.leftMargin = marginLeft;
        params.rightMargin = marginRight;

        cardView.setLayoutParams(params);
    }

    private void setUpListeners() {
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideSearchSuggestions();
                    BusProvider.getInstance().post(new SearchPerformedEvent(getQuery()));
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void setUpLeftDrawable(boolean showingSearchSuggestions) {
        if (showingSearchSuggestions) {
            leftDrawableImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_arrow_back_dark));
            leftDrawableRoundedImageView.setVisibility(View.GONE);
            leftDrawableImageView.setVisibility(View.VISIBLE);
        } else {
            switch (leftDrawableType) {
                case MENU:
                    leftDrawableImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu_dark));
                    break;
                case BACK:
                    leftDrawableImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_arrow_back_dark));
                    break;
                case SEARCH:
                    leftDrawableImageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_search));
                    break;
                case AVATAR:
                    break;
                default:
                    break;
            }

            if (leftDrawableType == AVATAR) {
                leftDrawableImageView.setVisibility(View.GONE);
                leftDrawableRoundedImageView.setVisibility(View.VISIBLE);
            } else {
                leftDrawableRoundedImageView.setVisibility(View.GONE);
                leftDrawableImageView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setLeftDrawableType(int type) {
        leftDrawableType = type;
    }

    private void setUpHintText() {
        if (searchEditText != null)
            searchEditText.setHint(hintText);
    }

    private void showSearchSuggestions() {
        BusProvider.getInstance().post(new ShowSearchSuggestionsEvent(getQuery()));

        suggestionsAdapter.setOnItemClickListener(this);
        suggestionsAdapter.setOnItemLongClickListener(this);
        suggestionsAdapter.setOnSearchSuggestionCompleteClickListener(this);

        CustomLinearLayoutManager layoutManager = new CustomLinearLayoutManager(getContext());
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        dividerItemDecoration = new DividerItemDecoration(ContextCompat.getDrawable(getContext(), R.drawable.divider));
        recyclerView.addItemDecoration(dividerItemDecoration);
//        mRecyclerView.setItemAnimator(new SlideInUpAnimator());
        recyclerView.setAdapter(suggestionsAdapter);

        if (suggestionsAdapter.getItemCount() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            dividerView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.GONE);
            dividerView.setVisibility(View.GONE);
        }

        backgroundCoverFrameLayout.setVisibility(View.VISIBLE);

        setUpLeftDrawable(true);

        areSearchSuggestionsVisible = true;
    }

    private void hideSearchSuggestions() {
        dividerView.setVisibility(View.GONE);
        backgroundCoverFrameLayout.setVisibility(View.GONE);

        setUpLeftDrawable(false);

        recyclerView.setVisibility(View.GONE);
        recyclerView.removeItemDecoration(dividerItemDecoration);

        searchEditText.clearFocus();
        areSearchSuggestionsVisible = false;

        BusProvider.getInstance().post(new HideSearchSuggestionsEvent());
    }

    private boolean isVoiceAvailable() {
        PackageManager pm = getContext().getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return (activities.size() != 0);
    }

    public void setQuery(String query) {
        searchEditText.setText(query);
        suggestionsAdapter.setCurrentQuery(query);
        if (!TextUtils.isEmpty(query))
            filterImageView.setVisibility(View.VISIBLE);
    }

    public String getQuery() {
        return searchEditText.getText().toString();
    }

    public void setHint(String hint) {
        searchEditText.setHint(hint);
    }

    public void addSuggestions(List<String> suggestions) {
        suggestionsAdapter.clear();
        suggestionsAdapter.addAll(suggestions);

        if (suggestionsAdapter.getItemCount() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            dividerView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.GONE);
            dividerView.setVisibility(View.GONE);
        }

        backgroundCoverFrameLayout.setVisibility(View.VISIBLE);
    }

    public void enableFilter() {
        filterImageView.setOnClickListener(filterImageViewOnClickListener);
        filterImageView.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ripple));
//        filterImageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.active_icon));
//        filterImageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(filterImageView.getContext(), R.color.active_icon)));
        filterImageView.setImageResource(R.drawable.ic_filter_list_active);

    }

    public void disableFilter() {
        filterImageView.setOnClickListener(null);
        filterImageView.setBackgroundDrawable(null);
//        filterImageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.inactive_icon));
//        filterImageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(filterImageView.getContext(), R.color.inactive_icon)));
        filterImageView.setImageResource(R.drawable.ic_filter_list_inactive);
    }

//    public void setAvatar(Retailer retailer){
////        mLeftDrawableRoundedImageView.bind(retailer);
//        mLeftDrawableImageView.setVisibility(View.GONE);
//        mLeftDrawableRoundedImageView.setVisibility(View.VISIBLE);
//    }
    // endregion
}
