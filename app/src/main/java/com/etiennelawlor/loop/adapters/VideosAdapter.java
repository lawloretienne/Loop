package com.etiennelawlor.loop.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.network.models.Pictures;
import com.etiennelawlor.loop.network.models.Size;
import com.etiennelawlor.loop.network.models.Stats;
import com.etiennelawlor.loop.network.models.User;
import com.etiennelawlor.loop.network.models.Video;
import com.etiennelawlor.loop.network.models.VideoWrapper;
import com.etiennelawlor.loop.utilities.LoopUtility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 5/23/15.
 */

public class VideosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // region Member Variables
    private Context mContext;
    private List<VideoWrapper> mVideoWrappers;
    private OnItemClickListener mOnItemClickListener;
    // endregion

    // region Listeners
    // endregion

    // region Interfaces
    public interface OnItemClickListener {
        void onItemClick(int position, View view);
    }
    // endregion

    // region Constructors
    public VideosAdapter(Context context) {
        mContext = context;
        mVideoWrappers = new ArrayList<>();
    }
    // endregion

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
//            case VideoWrapper.HEADER:
//                return createHeaderViewHolder(mHeaderView);
            case VideoWrapper.VIDEO:
                return createVideoViewHolder(parent);
            case VideoWrapper.LOADING:
                return createLoadingViewHolder(parent);
            default:
                Timber.e("[ERR] type is not supported!!! type is %d", viewType);
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (getItemViewType(position)) {
            case VideoWrapper.VIDEO:
                bindVideoViewHolder(viewHolder, position);
                break;
            case VideoWrapper.HEADER:
            case VideoWrapper.LOADING:
            case VideoWrapper.NONE:
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mVideoWrappers.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
    }

    // region Helper Methods
    private void add(VideoWrapper item) {
        mVideoWrappers.add(item);
        notifyItemInserted(mVideoWrappers.size());
    }

    public void addAll(List<Video> videos) {
        for (Video video : videos) {
            add(VideoWrapper.createVideoType(video));
        }

        if (mVideoWrappers.size() > 3) {
            add(VideoWrapper.createLoadingType());
        }
    }

    public void remove(VideoWrapper item) {
        int position = mVideoWrappers.indexOf(item);
        if (position > -1) {
            mVideoWrappers.remove(position);
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

    public void removeLoading() {
        int position = mVideoWrappers.size() - 1;
        VideoWrapper item = getItem(position);

        if (item != null && item.getType() == VideoWrapper.LOADING) {
            mVideoWrappers.remove(position);
            notifyItemChanged(position + 1);
        }
    }

    public VideoWrapper getItem(int position) {
        try {
            return mVideoWrappers.get(position);
        } catch (IndexOutOfBoundsException e) {
            Timber.e(e, "index is %d, and size is %d", position, getItemCount());
            return mVideoWrappers.get(getItemCount() - 1);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }


//    private RecyclerView.ViewHolder createHeaderViewHolder(ViewGroup parent){
//
//    }

    private RecyclerView.ViewHolder createVideoViewHolder(ViewGroup parent) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_row, parent, false);

        return new VideoViewHolder(v);
    }

    private RecyclerView.ViewHolder createLoadingViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more, parent, false);

        return new MoreViewHolder(v);
    }

    private void bindVideoViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        VideoViewHolder holder = (VideoViewHolder) viewHolder;

        VideoWrapper videoWrapper = mVideoWrappers.get(position);
        if (videoWrapper != null) {
            final Video video = videoWrapper.getVideo();

            if (video != null) {
                setUpTitle(holder.mTitleTextView, video);
                setUpSubtitle(holder.mSubtitleTextView, video);
                setUpVideoThumbnail(holder.mVideoThumbnailImageView, video);
                setUpDuration(holder.mDurationTextView, video);
                setUpUploadedDate(holder.mUploadedDateTextView, video);
            }
        }
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
        Pictures pictures = video.getPictures();
        if (pictures != null) {
            List<Size> sizes = pictures.getSizes();
            if (sizes != null && sizes.size() > 0) {
                Size size = sizes.get(sizes.size() - 1);
                if (size != null) {
                    String link = size.getLink();
                    if (!TextUtils.isEmpty(link)) {
                        Glide.with(mContext)
                                .load(link)
//                                .placeholder(R.drawable.ic_placeholder)
//                                .error(R.drawable.ic_error)
                                .into(iv);
                    }
                }
            }
        }
    }

    private void setUpDuration(TextView tv, Video video) {
        Integer duration = video.getDuration();

        long minutes = duration / 60;
        long seconds = duration % 60;

        String time;
        if (minutes == 0L) {
            if (seconds > 0L) {
                if (seconds < 10L)
                    time = String.format("0:0%s", String.valueOf(seconds));
                else
                    time = String.format("0:%s", String.valueOf(seconds));
            } else {
                time = "0:00";
            }

        } else {
            if (seconds > 0L) {
                if (seconds < 10L)
                    time = String.format("%s:0%s", String.valueOf(minutes), String.valueOf(seconds));
                else
                    time = String.format("%s:%s", String.valueOf(minutes), String.valueOf(seconds));
            } else {
                time = String.format("%s:00", String.valueOf(minutes));
            }
        }

        tv.setText(time);
    }

    private void setUpUploadedDate(TextView tv, Video video) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ", Locale.ENGLISH);
        String uploadDate = "";

        String createdTime = video.getCreatedTime();
        try {
            Date date = sdf.parse(createdTime);

            Calendar futureCalendar = Calendar.getInstance();
            futureCalendar.setTime(date);

            uploadDate = LoopUtility.getRelativeDate(futureCalendar);
        } catch (ParseException e) {
            Timber.e("");
        }

        int viewCount = 0;
        Stats stats = video.getStats();
        if (stats != null) {
            viewCount = stats.getPlays();
        }

        if (viewCount > 0) {
//                String formattedViewCount = NumberFormat.getNumberInstance(Locale.US).format(viewCount);
            String formattedViewCount = formatViewCount(viewCount);
            if(!TextUtils.isEmpty(uploadDate))
                tv.setText(String.format("%s - %s", uploadDate, formattedViewCount));
            else
                tv.setText(formattedViewCount);

        } else {
            tv.setText(String.format("%s", uploadDate));
        }
    }

    private String formatViewCount(int viewCount) {
        String formattedViewCount = "";

        if (viewCount < 1000000000 && viewCount >= 1000000) {
            formattedViewCount = String.format("%dM views", viewCount / 1000000);
        } else if (viewCount < 1000000 && viewCount >= 1000) {
            formattedViewCount = String.format("%dK views", viewCount / 1000);
        } else if (viewCount < 1000 && viewCount > 1) {
            formattedViewCount = String.format("%d views", viewCount);
        } else if (viewCount == 1) {
            formattedViewCount = String.format("%d view", viewCount);
        }

        return formattedViewCount;
    }
    // endregion

    // region Inner Classes

    public class VideoViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.video_thumbnail_iv)
        ImageView mVideoThumbnailImageView;
        @InjectView(R.id.title_tv)
        TextView mTitleTextView;
        @InjectView(R.id.uploaded_date_tv)
        TextView mUploadedDateTextView;
        @InjectView(R.id.duration_tv)
        TextView mDurationTextView;
        @InjectView(R.id.subtitle_tv)
        TextView mSubtitleTextView;

        @OnClick(R.id.video_row_root_ll)
        void onVideoClick() {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(getPosition(), itemView);
            }
        }

        VideoViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }

    public static class MoreViewHolder extends RecyclerView.ViewHolder {
        public MoreViewHolder(View view) {
            super(view);
            ProgressBar pb = ButterKnife.findById(view, R.id.progress_bar);
            pb.getIndeterminateDrawable()
                    .mutate()
                    .setColorFilter(view.getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_ATOP);
        }
    }

    // endregion

}