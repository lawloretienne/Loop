package com.etiennelawlor.loop.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.network.models.response.Comment;
import com.etiennelawlor.loop.network.models.response.Pictures;
import com.etiennelawlor.loop.network.models.response.Size;
import com.etiennelawlor.loop.network.models.response.User;
import com.etiennelawlor.loop.utilities.DateUtility;
import com.etiennelawlor.trestle.library.Span;
import com.etiennelawlor.trestle.library.Trestle;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 12/20/15.
 */

public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // region Member Variables
    private List<Comment> mComments;
    private Context mContext;
    private Typeface mBoldFont;
    private OnItemLongClickListener mOnItemLongClickListener;
    private Typeface mItalicFont;
    // endregion

    // region Interfaces
    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }
    // endregion

    // region Constructors
    public CommentsAdapter(Context context) {
        mContext = context;
        mComments = new ArrayList<>();
        mBoldFont = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Bold.ttf");
        mItalicFont = Typeface.createFromAsset(mContext.getAssets(), "fonts/Roboto-Italic.ttf");
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
                    if (mOnItemLongClickListener != null) {
                        mOnItemLongClickListener.onItemLongClick(adapterPos);
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

        final Comment comment = mComments.get(position);

        if (comment != null) {
            setUpCommentText(holder.mCommentTextView, comment);
            setUpCommentImage(holder.mCommentImageView, comment);
        }
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    // region Helper Methods
    public void add(int position, Comment item) {
        mComments.add(position, item);
        notifyItemInserted(position);
    }

    public void addAll(List<Comment> comments) {
        for (Comment comment : comments) {
            add(getItemCount(), comment);
        }
    }

    public void clear() {
        while (getItemCount() > 0) {
            remove(0);
        }
    }

    public void remove(int position) {
        mComments.remove(position);
        notifyItemRemoved(position);
    }

    public Comment getItem(int position) {
        return mComments.get(position);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.mOnItemLongClickListener = onItemLongClickListener;
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
                    .typeface(mBoldFont)
                    .build());
            spans.add(new Span.Builder(commentText)
                    .build());
            spans.add(new Span.Builder("\n")
                    .build());
            spans.add(new Span.Builder(commentDate)
                    .foregroundColor(ContextCompat.getColor(tv.getContext(), R.color.grey_400))
                    .typeface(mItalicFont)
                    .build());
        } else if(!TextUtils.isEmpty(commentText)
                    && !TextUtils.isEmpty(commentDate)){
            spans.add(new Span.Builder(commentText)
                    .build());
            spans.add(new Span.Builder("\n")
                    .build());
            spans.add(new Span.Builder(commentDate)
                    .foregroundColor(ContextCompat.getColor(tv.getContext(), R.color.grey_400))
                    .typeface(mItalicFont)
                    .build());
        }

        CharSequence formattedText = Trestle.getFormattedText(spans);
        tv.setText(formattedText);
    }

    private String getCommentDate(Comment comment){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ", Locale.ENGLISH);
        String commentDate = "";

        String createdOn = comment.getCreatedOn();
        try {
            Date date = sdf.parse(createdOn);

            Calendar futureCalendar = Calendar.getInstance();
            futureCalendar.setTime(date);

            commentDate = DateUtility.getRelativeDate(futureCalendar);
        } catch (ParseException e) {
            Timber.e("");
        }

        return commentDate;
    }

    private void setUpCommentImage(ImageView iv, Comment comment){
        boolean isPictureAvailable = false;

        User user = comment.getUser();
        if (user != null) {

            Pictures pictures = user.getPictures();
            if (pictures != null) {
                List<Size> sizes = pictures.getSizes();
                if (sizes != null && sizes.size() > 0) {
                    Size size = sizes.get(sizes.size() - 1);
                    if (size != null) {
                        String link = size.getLink();
                        if (!TextUtils.isEmpty(link)) {
                            isPictureAvailable = true;
                            Glide.with(iv.getContext())
                                    .load(link)
//                                .placeholder(R.drawable.ic_placeholder)
//                                .error(R.drawable.ic_error)
                                    .into(iv);
                        }
                    }
                }
            }
        }

        if (!isPictureAvailable) {
            iv.setImageResource(R.drawable.ic_loop);
        }
    }
    // endregion

    // region Inner Classes

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.comment_tv)
        TextView mCommentTextView;
        @Bind(R.id.comment_iv)
        ImageView mCommentImageView;

        public CommentViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    // endregion

}
