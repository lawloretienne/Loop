package com.etiennelawlor.loop.adapters;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.network.models.response.User;
import com.etiennelawlor.loop.network.models.response.Video;
import com.etiennelawlor.loop.ui.LoadingImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by etiennelawlor on 5/23/15.
 */

public class VideosAdapter extends BaseAdapter<Video> {

    // region Member Variables
    private FooterViewHolder footerViewHolder;
    // endregion

    // region Constructors
    public VideosAdapter() {
        super();
    }
    // endregion

    @Override
    public int getItemViewType(int position) {
        return (isLastPosition(position) && isFooterAdded) ? FOOTER : ITEM;
    }

    @Override
    protected RecyclerView.ViewHolder createHeaderViewHolder(ViewGroup parent) {
        return null;
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

    // region Inner Classes

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
        public VideoViewHolder(View view) {
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