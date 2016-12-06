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
import com.etiennelawlor.loop.utilities.FontCache;
import com.etiennelawlor.trestle.library.Regex;
import com.etiennelawlor.trestle.library.Span;
import com.etiennelawlor.trestle.library.Trestle;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by etiennelawlor on 5/23/15.
 */

public class SuggestionsAdapter extends BaseAdapter<String> {

    // region Member Variables
    private Typeface font;
    private OnItemLongClickListener onItemLongClickListener;
    private OnSearchSuggestionCompleteClickListener onSearchSuggestionCompleteClickListener;
    private String currentQuery = "";
    // endregion

    // region Interfaces
    public interface OnItemLongClickListener {
        void onItemLongClick(int position, View view);
    }

    public interface OnSearchSuggestionCompleteClickListener {
        void onSearchSuggestionCompleteClickListener(int position, TextView textView);
    }
    // endregion

    // region Constructors
    public SuggestionsAdapter() {
        super();
        font = FontCache.getTypeface("Ubuntu-Bold.ttf", LoopApplication.getInstance().getApplicationContext());
    }
    // endregion

    @Override
    public int getItemViewType(int position) {
        return ITEM;
    }

    @Override
    protected RecyclerView.ViewHolder createHeaderViewHolder(ViewGroup parent) {
        return null;
    }

    @Override
    protected RecyclerView.ViewHolder createItemViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.suggestion, parent, false);

        final SuggestionViewHolder holder = new SuggestionViewHolder(v);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPos = holder.getAdapterPosition();
                if(adapterPos != RecyclerView.NO_POSITION){
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(adapterPos, holder.itemView);
                    }
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int adapterPos = holder.getAdapterPosition();
                if(adapterPos != RecyclerView.NO_POSITION){
                    if (onItemLongClickListener != null) {
                        onItemLongClickListener.onItemLongClick(adapterPos, holder.itemView);
                    }
                }
                return true;
            }
        });

        holder.suggestionCompleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPos = holder.getAdapterPosition();
                if(adapterPos != RecyclerView.NO_POSITION){
                    if (onSearchSuggestionCompleteClickListener != null) {
                        onSearchSuggestionCompleteClickListener.onSearchSuggestionCompleteClickListener(adapterPos, holder.suggestionTextView);
                    }
                }
            }
        });

        return holder;
    }

    @Override
    protected RecyclerView.ViewHolder createFooterViewHolder(ViewGroup parent) {
        return null;
    }

    @Override
    protected void bindHeaderViewHolder(RecyclerView.ViewHolder viewHolder) {

    }

    @Override
    protected void bindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final SuggestionViewHolder holder = (SuggestionViewHolder) viewHolder;

        final String suggestion = getItem(position);
        if (!TextUtils.isEmpty(suggestion)) {
            setUpSuggestion(holder.suggestionTextView, suggestion);
        }
    }

    @Override
    protected void bindFooterViewHolder(RecyclerView.ViewHolder viewHolder) {

    }

    @Override
    protected void displayLoadMoreFooter() {

    }

    @Override
    protected void displayErrorFooter() {

    }

    @Override
    public void addFooter() {

    }

    // region Helper Methods
    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setOnSearchSuggestionCompleteClickListener(OnSearchSuggestionCompleteClickListener onSearchSuggestionCompleteClickListener) {
        this.onSearchSuggestionCompleteClickListener = onSearchSuggestionCompleteClickListener;
    }

    private void setUpSuggestion(TextView tv, String suggestion){
        if(!TextUtils.isEmpty(suggestion)){
            if(!TextUtils.isEmpty(currentQuery)){
                CharSequence formattedSuggestion = Trestle.getFormattedText(
                        new Span.Builder(suggestion)
                                .regex(new Regex(currentQuery, Regex.CASE_INSENSITIVE))
                                .foregroundColor(ContextCompat.getColor(tv.getContext(), R.color.primary)) // Pass resolved color instead of resource id
                                .typeface(font)
                                .build());

                tv.setText(formattedSuggestion);
            } else {
                tv.setText(suggestion);
            }
        }
    }

    public void setCurrentQuery(String query){
        currentQuery = query;
    }
    // endregion

    // region Inner Classes

    public static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        // region Views
        @Bind(R.id.suggestion_tv)
        TextView suggestionTextView;
        @Bind(R.id.search_suggest_complete_iv)
        ImageView suggestionCompleteImageView;
        // endregion

        // region Constructors
        public SuggestionViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
        // endregion
    }

    // endregion

}