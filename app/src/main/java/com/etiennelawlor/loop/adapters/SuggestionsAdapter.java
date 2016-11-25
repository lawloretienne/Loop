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
    private List<String> suggestions;
    private Typeface blackFont;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private OnSearchSuggestionCompleteClickListener onSearchSuggestionCompleteClickListener;
    private String currentQuery = "";
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
        suggestions = new ArrayList<>();
        blackFont = Typeface.createFromAsset(LoopApplication.getInstance().getAssets(), "fonts/Roboto-Black.ttf");
    }
    // endregion

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case ITEM:
                viewHolder = createSuggestionViewHolder(parent);
                break;
            default:
                Timber.e("[ERR] type is not supported!!! type is %d", viewType);
                break;
        }

        return viewHolder;
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
        return suggestions.size();
    }

    @Override
    public int getItemViewType(int position) {
        return ITEM;
    }

    // region Helper Methods
    private void add(String item) {
        suggestions.add(item);
        notifyItemInserted(suggestions.size()-1);
    }

    public void addAll(List<String> suggestions) {
        for (String suggestion : suggestions) {
            add(suggestion);
        }
    }

    public void remove(String item) {
        int position = suggestions.indexOf(item);
        if (position > -1) {
            suggestions.remove(position);
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
        return suggestions.get(position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setOnSearchSuggestionCompleteClickListener(OnSearchSuggestionCompleteClickListener onSearchSuggestionCompleteClickListener) {
        this.onSearchSuggestionCompleteClickListener = onSearchSuggestionCompleteClickListener;
    }

    private RecyclerView.ViewHolder createSuggestionViewHolder(ViewGroup parent){
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

    private void bindSuggestionViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final SuggestionViewHolder holder = (SuggestionViewHolder) viewHolder;

        final String suggestion = suggestions.get(position);
        if (!TextUtils.isEmpty(suggestion)) {
            setUpSuggestion(holder.suggestionTextView, suggestion);
        }
    }

    private void setUpSuggestion(TextView tv, String suggestion){
        if(!TextUtils.isEmpty(suggestion)){
            if(!TextUtils.isEmpty(currentQuery)){
                CharSequence formattedSuggestion = Trestle.getFormattedText(
                        new Span.Builder(suggestion)
                                .regex(new Regex(currentQuery, Regex.CASE_INSENSITIVE))
                                .foregroundColor(ContextCompat.getColor(tv.getContext(), R.color.primary)) // Pass resolved color instead of resource id
                                .typeface(blackFont)
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
        @Bind(R.id.suggestion_tv)
        TextView suggestionTextView;
        @Bind(R.id.search_suggest_complete_iv)
        ImageView suggestionCompleteImageView;

        public SuggestionViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    // endregion

}