package com.etiennelawlor.loop.adapters;

import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.etiennelawlor.loop.LoopApplication;
import com.etiennelawlor.loop.R;
import com.etiennelawlor.trestle.library.Regex;
import com.etiennelawlor.trestle.library.Span;
import com.etiennelawlor.trestle.library.Trestle;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 5/23/15.
 */

public class SuggestionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // region Constants
    public static final int ITEM = 0;
    // endregion

    // region Member Variables
    private List<String> mSuggestions;
    private Typeface mBlackFont;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnSearchSuggestionCompleteClickListener mOnSearchSuggestionCompleteClickListener;
    private String mCurrentQuery = "";
    // endregion

    // region Listeners
    // endregion

    // region Interfaces
    public interface OnItemClickListener {
        void onItemClick(int position, View view);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position, View view);
    }

    public interface OnSearchSuggestionCompleteClickListener {
        void onSearchSuggestionCompleteClickListener(int position, TextView textView);
    }
    // endregion

    // region Constructors
    public SuggestionsAdapter() {
        mSuggestions = new ArrayList<>();
        mBlackFont = Typeface.createFromAsset(LoopApplication.get().getAssets(), "fonts/Roboto-Black.ttf");
    }
    // endregion

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM:
                return createSuggestionViewHolder(parent);
            default:
                Timber.e("[ERR] type is not supported!!! type is %d", viewType);
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (getItemViewType(position)) {
            case ITEM:
                bindSuggestionViewHolder(viewHolder, position);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mSuggestions.size();
    }

    @Override
    public int getItemViewType(int position) {
        return ITEM;
    }

    // region Helper Methods
    private void add(String item) {
        mSuggestions.add(item);
        notifyItemInserted(mSuggestions.size()-1);
    }

    public void addAll(List<String> suggestions) {
        for (String suggestion : suggestions) {
            add(suggestion);
        }
    }

    public void remove(String item) {
        int position = mSuggestions.indexOf(item);
        if (position > -1) {
            mSuggestions.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public String getItem(int position) {
        return mSuggestions.get(position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.mOnItemLongClickListener = onItemLongClickListener;
    }

    public void setOnSearchSuggestionCompleteClickListener(OnSearchSuggestionCompleteClickListener onSearchSuggestionCompleteClickListener) {
        this.mOnSearchSuggestionCompleteClickListener = onSearchSuggestionCompleteClickListener;
    }

    private RecyclerView.ViewHolder createSuggestionViewHolder(ViewGroup parent){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.suggestion, parent, false);

        return new SuggestionViewHolder(v);
    }

    private void bindSuggestionViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        final SuggestionViewHolder holder = (SuggestionViewHolder) viewHolder;

        final String suggestion = mSuggestions.get(position);
        if (!TextUtils.isEmpty(suggestion)) {
            setUpSuggestion(holder.mSuggestionTextView, suggestion);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(position, holder.itemView);
                    }
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mOnItemLongClickListener != null) {
                        mOnItemLongClickListener.onItemLongClick(position, holder.itemView);
                    }
                    return true;
                }
            });

            holder.mSuggestionCompleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnSearchSuggestionCompleteClickListener != null) {
                        mOnSearchSuggestionCompleteClickListener.onSearchSuggestionCompleteClickListener(position, holder.mSuggestionTextView);
                    }
                }
            });
        }
    }

    private void setUpSuggestion(TextView tv, String suggestion){
        if(!TextUtils.isEmpty(suggestion)){
            if(!TextUtils.isEmpty(mCurrentQuery)){
                CharSequence formattedSuggestion = Trestle.getFormattedText(
                        new Span.Builder(suggestion)
                                .regex(new Regex(mCurrentQuery, Regex.CASE_INSENSITIVE))
                                .foregroundColor(ContextCompat.getColor(tv.getContext(), R.color.primary)) // Pass resolved color instead of resource id
                                .typeface(mBlackFont)
                                .build());

                tv.setText(formattedSuggestion);
            } else {
                tv.setText(suggestion);
            }
        }
    }

    public void setCurrentQuery(String query){
        mCurrentQuery = query;
    }
    // endregion

    // region Inner Classes

    public static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.suggestion_tv)
        TextView mSuggestionTextView;
        @Bind(R.id.search_suggest_complete_iv)
        ImageView mSuggestionCompleteImageView;

        SuggestionViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    // endregion

}