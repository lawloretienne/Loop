package com.etiennelawlor.loop.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.network.models.response.Comment;
import com.etiennelawlor.loop.network.models.response.User;
import com.etiennelawlor.loop.ui.AvatarView;
import com.etiennelawlor.loop.utilities.DateUtility;
import com.etiennelawlor.loop.utilities.FontCache;
import com.etiennelawlor.trestle.library.Span;
import com.etiennelawlor.trestle.library.Trestle;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by etiennelawlor on 12/20/15.
 */

// Setup ReverseBaseAdapter for pagination
public class VideoCommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // region Member Variables
    private List<Comment> comments;
    private Typeface boldFont;
    private OnItemLongClickListener onItemLongClickListener;
    private Typeface italicFont;
    // endregion

    // region Interfaces
    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }
    // endregion

    // region Constructors
    public VideoCommentsAdapter(Context context) {
        comments = new ArrayList<>();

        boldFont = FontCache.getTypeface("Ubuntu-Bold.ttf", context);
        italicFont = FontCache.getTypeface("Ubuntu-Italic.ttf", context);
    }
    // endregion

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_row, parent, false);
        final CommentViewHolder holder = new CommentViewHolder(v);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int adapterPos = holder.getAdapterPosition();
                if(adapterPos != RecyclerView.NO_POSITION){
                    if (onItemLongClickListener != null) {
                        onItemLongClickListener.onItemLongClick(adapterPos);
                    }
                }

                return false;
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        CommentViewHolder holder = (CommentViewHolder) viewHolder;

        final Comment comment = comments.get(position);

        if (comment != null) {
            setUpCommentText(holder.commentTextView, comment);
            setUpCommentImage(holder.commentImageView, comment);
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    // region Helper Methods
    public void add(Comment item) {
        comments.add(item);
        notifyItemInserted(comments.size() - 1);
    }

    public void addAll(List<Comment> comments) {
        for (Comment comment : comments) {
            add(comment);
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

    public void remove(Comment item) {
        int position = comments.indexOf(item);
        if (position > -1) {
            comments.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Comment getItem(int position) {
        return comments.get(position);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    private void setUpCommentText(TextView tv, Comment comment){
        String commentText = comment.getText();
        User user = comment.getUser();

        String commentDate = getCommentDate(comment);

        String displayName = "";

        if (user != null) {
            displayName = user.getName();
        }

        List<Span> spans = new ArrayList<>();

        if(!TextUtils.isEmpty(displayName)
                && !TextUtils.isEmpty(commentText)
                && !TextUtils.isEmpty(commentDate)){
            spans.add(new Span.Builder(String.format("%s ", displayName))
                    .foregroundColor(ContextCompat.getColor(tv.getContext(), R.color.primary))
                    .typeface(boldFont)
                    .build());
            spans.add(new Span.Builder(commentText)
                    .build());
            spans.add(new Span.Builder("\n")
                    .build());
            spans.add(new Span.Builder(commentDate)
                    .foregroundColor(ContextCompat.getColor(tv.getContext(), R.color.tertiary_text))
                    .typeface(italicFont)
                    .build());
        } else if(!TextUtils.isEmpty(commentText)
                    && !TextUtils.isEmpty(commentDate)){
            spans.add(new Span.Builder(commentText)
                    .build());
            spans.add(new Span.Builder("\n")
                    .build());
            spans.add(new Span.Builder(commentDate)
                    .foregroundColor(ContextCompat.getColor(tv.getContext(), R.color.tertiary_text))
                    .typeface(italicFont)
                    .build());
        }

        CharSequence formattedText = Trestle.getFormattedText(spans);
        tv.setText(formattedText);
    }

    private String getCommentDate(Comment comment){
        String createdOn = comment.getCreatedOn();

        String formattedCreatedOn = DateUtility.getFormattedTime(DateUtility.getCalendar(createdOn), DateUtility.FORMAT_RELATIVE);
        return formattedCreatedOn;
    }

    private void setUpCommentImage(AvatarView av, Comment comment){
        User user = comment.getUser();

        if(user != null){
            av.bind(user);
        } else {
            av.nullify();
        }
    }
    // endregion

    // region Inner Classes

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        // region Views
        @Bind(R.id.comment_tv)
        TextView commentTextView;
        @Bind(R.id.comment_iv)
        AvatarView commentImageView;
        // endregion

        // region Constructors
        public CommentViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
        // endregion
    }

    // endregion

}
