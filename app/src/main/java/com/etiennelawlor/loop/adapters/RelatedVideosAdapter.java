package com.etiennelawlor.loop.adapters;

import android.graphics.Typeface;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.etiennelawlor.loop.LoopApplication;
import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.bus.RxBus;
import com.etiennelawlor.loop.bus.events.SearchPerformedEvent;
import com.etiennelawlor.loop.network.models.response.User;
import com.etiennelawlor.loop.network.models.response.Video;
import com.etiennelawlor.loop.ui.AvatarView;
import com.etiennelawlor.loop.ui.LoadingImageView;
import com.etiennelawlor.loop.utilities.FontCache;
import com.etiennelawlor.loop.utilities.Transformers;
import com.greenfrvr.hashtagview.HashtagView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by etiennelawlor on 5/23/15.
 */

public class RelatedVideosAdapter extends BaseAdapter<Video> {

    // region Static Variables
    private static boolean isLikeOn = false;
    private static boolean isWatchLaterOn = false;
    private static boolean hasDescription = false;
    private static boolean hasTags = false;
    private static Typeface boldFont;
    // endregion

    // region Member Variables
    private Video video;
    private OnLikeClickListener onLikeClickListener;
    private OnWatchLaterClickListener onWatchLaterClickListener;
    private OnCommentsClickListener onCommentsClickListener;
    private OnInfoClickListener onInfoClickListener;
    private FooterViewHolder footerViewHolder;
    // endregion

    // region Interfaces
    public interface OnLikeClickListener {
        void onLikeClick(ImageView imageView);
    }

    public interface OnWatchLaterClickListener {
        void onWatchLaterClick(ImageView imageView);
    }

    public interface OnCommentsClickListener {
        void onCommentsClick();
    }

    public interface OnInfoClickListener {
        void onInfoClick(ImageView imageView);
    }
    // endregion

    // region Constructors
    public RelatedVideosAdapter(Video video) {
        super();
        this.video = video;
        boldFont = FontCache.getTypeface("Ubuntu-Bold.ttf", LoopApplication.getInstance().getApplicationContext());
    }
    // endregion

    @Override
    public int getItemViewType(int position) {
        if(position == 0)
            return HEADER;
        else
            return (isLastPosition(position) && isFooterAdded) ? FOOTER : ITEM;
    }

    @Override
    protected RecyclerView.ViewHolder createHeaderViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_info, parent, false);
        final HeaderViewHolder holder = new HeaderViewHolder(v);

        holder.likeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onLikeClickListener != null){
                    onLikeClickListener.onLikeClick(holder.likeImageView);
                }
            }
        });

        holder.watchLaterImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onWatchLaterClickListener != null){
                    onWatchLaterClickListener.onWatchLaterClick(holder.watchLaterImageView);
                }
            }
        });

        holder.commentsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onCommentsClickListener != null){
                    onCommentsClickListener.onCommentsClick();
                }
            }
        });

        holder.infoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onInfoClickListener != null){
                    onInfoClickListener.onInfoClick(holder.infoImageView);
                    int visibility = holder.additionalInfoLinearLayout.getVisibility();
                    if(visibility == View.VISIBLE){
                        holder.additionalInfoLinearLayout.setVisibility(View.GONE);
                    } else if(visibility == View.GONE){
                        holder.additionalInfoLinearLayout.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        return holder;
    }

    @Override
    protected RecyclerView.ViewHolder createItemViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_row, parent, false);
        final VideoViewHolder holder = new VideoViewHolder(v);

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

        return holder;
    }

    @Override
    protected RecyclerView.ViewHolder createFooterViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_footer, parent, false);

        final FooterViewHolder holder = new FooterViewHolder(v);
        holder.reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onReloadClickListener != null){
                    onReloadClickListener.onReloadClick();
                }
            }
        });

        return holder;
    }

    @Override
    protected void bindHeaderViewHolder(RecyclerView.ViewHolder viewHolder) {
        HeaderViewHolder holder = (HeaderViewHolder) viewHolder;

        if(video != null){
            holder.bind(video);
        }
    }

    @Override
    protected void bindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final VideoViewHolder holder = (VideoViewHolder) viewHolder;

        final Video video = getItem(position);
        if (video != null) {
            holder.bind(video);
        }
    }

    @Override
    protected void bindFooterViewHolder(RecyclerView.ViewHolder viewHolder) {
        FooterViewHolder holder = (FooterViewHolder) viewHolder;
        footerViewHolder = holder;

        holder.loadingImageView.setMaskOrientation(LoadingImageView.MaskOrientation.LeftToRight);
    }

    @Override
    protected void displayLoadMoreFooter() {
        if(footerViewHolder!= null){
            footerViewHolder.errorRelativeLayout.setVisibility(View.GONE);
            footerViewHolder.loadingFrameLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void displayErrorFooter() {
        if(footerViewHolder!= null){
            footerViewHolder.loadingFrameLayout.setVisibility(View.GONE);
            footerViewHolder.errorRelativeLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void addFooter() {
        isFooterAdded = true;
        add(new Video());
    }

    // region Helper Methods
    public void addHeader(){
        add(new Video());
    }

    public void setOnLikeClickListener(OnLikeClickListener onLikeClickListener) {
        this.onLikeClickListener = onLikeClickListener;
    }

    public void setOnWatchLaterClickListener(OnWatchLaterClickListener onWatchLaterClickListener) {
        this.onWatchLaterClickListener = onWatchLaterClickListener;
    }

    public void setOnCommentsClickListener(OnCommentsClickListener onCommentsClickListener) {
        this.onCommentsClickListener = onCommentsClickListener;
    }

    public void setOnInfoClickListener(OnInfoClickListener onInfoClickListener) {
        this.onInfoClickListener = onInfoClickListener;
    }

    public static boolean isLikeOn() {return isLikeOn; }

    public static void setIsLikeOn(boolean iLO) { isLikeOn = iLO; }

    public static boolean isWatchLaterOn() {return isWatchLaterOn; }

    public static void setIsWatchLaterOn(boolean iWLO) { isWatchLaterOn = iWLO; }
    // endregion

    // region Inner Classes

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        // region Views
        @BindView(R.id.title_tv)
        TextView titleTextView;
        @BindView(R.id.subtitle_tv)
        TextView subtitleTextView;
        @BindView(R.id.user_iv)
        AvatarView userImageView;
        @BindView(R.id.view_count_tv)
        TextView viewCountTextView;
        @BindView(R.id.upload_date_tv)
        TextView uploadDateTextView;
        @BindView(R.id.like_iv)
        ImageView likeImageView;
        @BindView(R.id.watch_later_iv)
        ImageView watchLaterImageView;
        @BindView(R.id.comments_iv)
        ImageView commentsImageView;
        @BindView(R.id.info_iv)
        ImageView infoImageView;
        @BindView(R.id.htv)
        HashtagView hashtagView;
        @BindView(R.id.description_tv)
        TextView descriptionTextView;
        @BindView(R.id.additional_info_ll)
        LinearLayout additionalInfoLinearLayout;
        // endregion

        // region Constructors
        HeaderViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
        // endregion

        // region Helper Methods
        private void bind(Video video){
            setUpTitle(titleTextView, video);
            setUpSubtitle(subtitleTextView, video);
            setUpViewCount(viewCountTextView, video);
            setUpLike(likeImageView, video);
            setUpWatchLater(watchLaterImageView, video);
            setUpUserImage(userImageView, video);
            setUpUploadedDate(uploadDateTextView, video);
            setUpDescription(descriptionTextView, video);
            setUpTags(hashtagView, video);
            setUpInfoImage(infoImageView);
        }

        private void setUpTitle(TextView tv, Video video) {
            String name = video.getName();
            if (!TextUtils.isEmpty(name)) {
                tv.setText(name);
            }
        }

        private void setUpSubtitle(TextView tv, Video video) {
            User user = video.getUser();
            if (user != null) {
                String userName = user.getName();
                if (!TextUtils.isEmpty(userName)) {
                    tv.setText(userName);
                }
            }
        }

        private void setUpLike(ImageView iv, Video video){
            boolean isLiked = video.isLiked();
            if (isLiked) {
                setIsLikeOn(true);
                iv.setImageResource(R.drawable.ic_likes_on);
            }
        }

        private void setUpWatchLater(ImageView iv, Video video){
            boolean isAddedToWatchLater = video.isAddedToWatchLater();
            if (isAddedToWatchLater) {
                setIsWatchLaterOn(true);
                iv.setImageResource(R.drawable.ic_watch_later_on);
            }
        }

        private void setUpUserImage(AvatarView av, Video video) {
            User user = video.getUser();
            if(user != null){
                av.bind(user);
            } else {
                av.nullify();
            }
        }

        private void setUpViewCount(TextView tv, Video video) {
            String formattedViewCount = video.getFormattedViewCount();
            if(!TextUtils.isEmpty(formattedViewCount)){
                tv.setText(formattedViewCount);
                tv.setVisibility(View.VISIBLE);
            } else {
                tv.setVisibility(View.GONE);
            }
        }

        private void setUpUploadedDate(TextView tv, Video video) {
            String formattedCreatedTime = video.getFormattedCreatedTime();
            if (!TextUtils.isEmpty(formattedCreatedTime)) {
                tv.setText(formattedCreatedTime);
                tv.setVisibility(View.VISIBLE);
            } else {
                tv.setVisibility(View.GONE);
            }
        }

        private void setUpTags(final HashtagView htv, Video video) {
            List<String> canonicalTags = video.getCanonicalTags();
            if(canonicalTags.size() > 0){
                hasTags = true;
                htv.setData(canonicalTags, Transformers.HASH);
                htv.setTypeface(boldFont);
                htv.addOnTagClickListener(new HashtagView.TagsClickListener() {
                    @Override
                    public void onItemClicked(Object item) {
                        String tag = (String) item;

                        // TODO this triggers two events somehow
                        RxBus.getInstance().send(new SearchPerformedEvent(tag));
                    }
                });
                htv.setVisibility(View.VISIBLE);
            } else {
                htv.setVisibility(View.GONE);
            }
        }

        private void setUpDescription(TextView tv, Video video) {
            String formattedDescription = video.getFormattedDescription();
            if (!TextUtils.isEmpty(formattedDescription)) {
                hasDescription = true;
//            formattedDescription = formattedDescription.replaceAll("[\\t\\n\\r]+", "\n");
                tv.setText(formattedDescription);
                tv.setVisibility(View.VISIBLE);
            } else {
                tv.setVisibility(View.GONE);
            }
        }

        private void setUpInfoImage(ImageView iv){
            if(hasDescription || hasTags){
                iv.setVisibility(View.VISIBLE);
            }
        }
        // endregion
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        // region Views
        @BindView(R.id.video_thumbnail_iv)
        ImageView videoThumbnailImageView;
        @BindView(R.id.title_tv)
        TextView titleTextView;
        @BindView(R.id.caption_tv)
        TextView captionTextView;
        @BindView(R.id.duration_tv)
        TextView durationTextView;
        @BindView(R.id.subtitle_tv)
        TextView subtitleTextView;
        // endregion

        // region Constructors
        VideoViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
        // endregion

        // region Helper Methods
        private void bind(Video video){
            setUpTitle(titleTextView, video);
            setUpSubtitle(subtitleTextView, video);
            setUpVideoThumbnail(videoThumbnailImageView, video);
            setUpDuration(durationTextView, video);
            setUpCaption(captionTextView, video);

            int adapterPos = getAdapterPosition();
            ViewCompat.setTransitionName(subtitleTextView,"myTransition"+adapterPos);
        }

        private void setUpTitle(TextView tv, Video video) {
            String name = video.getName();
            if (!TextUtils.isEmpty(name)) {
                tv.setText(name);
            }
        }

        private void setUpSubtitle(TextView tv, Video video) {
            User user = video.getUser();
            if (user != null) {
                String userName = user.getName();
                if (!TextUtils.isEmpty(userName)) {
                    tv.setText(userName);
                }
            }
        }

        private void setUpVideoThumbnail(ImageView iv, Video video) {
            String thumbnailUrl = video.getThumbnailUrl();
            if (!TextUtils.isEmpty(thumbnailUrl)) {
                Glide.with(iv.getContext())
                        .load(thumbnailUrl)
//                                .placeholder(R.drawable.ic_placeholder)
//                                .error(R.drawable.ic_error)
                        .into(iv);
            }
        }

        private void setUpDuration(TextView tv, Video video) {
            String formattedDuration = video.getFormattedDuration();
            if(!TextUtils.isEmpty(formattedDuration))
                tv.setText(formattedDuration);
        }

        private void setUpCaption(TextView tv, Video video) {
            String caption = video.getCaption();
            if(!TextUtils.isEmpty(caption))
                tv.setText(caption);
        }
        // endregion
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        // region Views
        @BindView(R.id.loading_fl)
        FrameLayout loadingFrameLayout;
        @BindView(R.id.error_rl)
        RelativeLayout errorRelativeLayout;
        @BindView(R.id.loading_iv)
        LoadingImageView loadingImageView;
        @BindView(R.id.reload_btn)
        Button reloadButton;
        // endregion

        // region Constructors
        public FooterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
        // endregion
    }

    // endregion

}