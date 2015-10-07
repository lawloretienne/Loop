package com.etiennelawlor.loop.adapters;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.etiennelawlor.loop.LoopApplication;
import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.activities.SearchableActivity;
import com.etiennelawlor.loop.network.models.response.Pictures;
import com.etiennelawlor.loop.network.models.response.Size;
import com.etiennelawlor.loop.network.models.response.Stats;
import com.etiennelawlor.loop.network.models.response.Tag;
import com.etiennelawlor.loop.network.models.response.User;
import com.etiennelawlor.loop.network.models.response.Video;
import com.etiennelawlor.loop.otto.BusProvider;
import com.etiennelawlor.loop.otto.events.SearchPerformedEvent;
import com.etiennelawlor.loop.ui.LoadingImageView;
import com.etiennelawlor.loop.utilities.LoopUtility;
import com.etiennelawlor.loop.utilities.Transformers;
import com.greenfrvr.hashtagview.HashtagView;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 5/23/15.
 */

public class RelatedVideosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // region Constants
    public static final int ITEM = 0;
    public static final int LOADING = 1;
    public static final int HEADER = 2;
    // endregion

    // region Member Variables
    private Video mVideo;
    private List<Video> mVideos;
    private OnItemClickListener mOnItemClickListener;
    private boolean mIsLoadingFooterAdded = false;
    private Typeface mBoldFont;
    // endregion

    // region Listeners
    // endregion

    // region Interfaces
    public interface OnItemClickListener {
        void onItemClick(int position, View view);
    }
    // endregion

    // region Constructors
    public RelatedVideosAdapter(Video video) {
        mVideo = video;
        mVideos = new ArrayList<>();

        mBoldFont = Typeface.createFromAsset(LoopApplication.get().getApplicationContext().getAssets(), "fonts/Roboto-Bold.ttf");
    }
    // endregion

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM:
                return createVideoViewHolder(parent);
            case LOADING:
                return createLoadingViewHolder(parent);
            case HEADER:
                return createHeaderViewHolder(parent);
            default:
                Timber.e("[ERR] type is not supported!!! type is %d", viewType);
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (getItemViewType(position)) {
            case ITEM:
                bindVideoViewHolder(viewHolder, position);
                break;
            case LOADING:
                bindLoadingViewHolder(viewHolder);
                break;
            case HEADER:
                bindHeaderViewHolder(viewHolder);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mVideos.size();
    }

    @Override
    public int getItemViewType(int position) {
//        Timber.d("getItemViewType() : position - "+position);
//        Timber.d("getItemViewType() : mVideos.size() - "+mVideos.size());

        if(position == 0)
            return HEADER;
        else
            return (position == mVideos.size()-1 && mIsLoadingFooterAdded) ? LOADING : ITEM;
    }

    // region Helper Methods
    private void add(Video item) {
        mVideos.add(item);
        notifyItemInserted(mVideos.size()-1);
    }

    public void addAll(List<Video> videos) {
        for (Video video : videos) {
            add(video);
        }
    }

    public void remove(Video item) {
        int position = mVideos.indexOf(item);
        if (position > -1) {
            mVideos.remove(position);
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

    public void addHeader(){
        add(new Video());
    }

    public void addLoading(){
        mIsLoadingFooterAdded = true;
        add(new Video());
    }

    public void removeLoading() {
        mIsLoadingFooterAdded = false;

        int position = mVideos.size() - 1;
        Video item = getItem(position);

        if (item != null) {
            mVideos.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Video getItem(int position) {
        return mVideos.get(position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }


    private RecyclerView.ViewHolder createHeaderViewHolder(ViewGroup parent){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_info, parent, false);

        return new HeaderViewHolder(v);
    }

    private RecyclerView.ViewHolder createVideoViewHolder(ViewGroup parent) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_row, parent, false);

        return new VideoViewHolder(v);
    }

    private RecyclerView.ViewHolder createLoadingViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more, parent, false);

        return new MoreViewHolder(v);
    }

    private void bindVideoViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        final VideoViewHolder holder = (VideoViewHolder) viewHolder;

        final Video video = mVideos.get(position);
        if (video != null) {
            setUpTitle(holder.mTitleTextView, video);
            setUpSubtitle(holder.mSubtitleTextView, video);
            setUpVideoThumbnail(holder.mVideoThumbnailImageView, video);
            setUpDuration(holder.mDurationTextView, video);
            setUpUploadedDate(holder.mUploadedDateTextView, video);

            holder.mVideoRowRootLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(position, holder.itemView);
                    }
                }
            });

        }
    }

    private void bindLoadingViewHolder(RecyclerView.ViewHolder viewHolder){
        MoreViewHolder holder = (MoreViewHolder) viewHolder;

        holder.mLoadingImageView.setMaskOrientation(LoadingImageView.MaskOrientation.LeftToRight);
    }

    private void bindHeaderViewHolder(RecyclerView.ViewHolder viewHolder) {
        HeaderViewHolder holder = (HeaderViewHolder) viewHolder;

        if(mVideo != null){
            setUpTitle(holder.mTitleTextView, mVideo);
            setUpSubtitle(holder.mSubtitleTextView, mVideo);
            setUpUserImage(holder.mUserImageView, mVideo);
            setUpDescription(holder.mDescriptionTextView, mVideo);
            setUpViewCount(holder.mViewCountTextView, mVideo);
            setUpUploadedDate2(holder.mUploadDateTextView, mVideo);
            setUpTags(holder.mHashtagView, mVideo);
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

    private void setUpUserImage(ImageView iv, Video video) {
        boolean isPictureAvailable = false;

        User user = video.getUser();
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

    private void setUpViewCount(TextView tv, Video video) {
        int viewCount = 0;
        Stats stats = video.getStats();
        if (stats != null) {
            viewCount = stats.getPlays();
        }

        if (viewCount > 0) {
            String formattedViewCount = NumberFormat.getNumberInstance(Locale.US).format(viewCount);
//                String formattedViewCount = formatViewCount(viewCount);
            if (viewCount > 1) {
                tv.setText(String.format("%s views", formattedViewCount));
            } else {
                tv.setText(String.format("%s view", formattedViewCount));
            }
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    private void setUpUploadedDate2(TextView tv, Video video) {
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

        if (!TextUtils.isEmpty(uploadDate)) {
            tv.setText(String.format("Uploaded %s", uploadDate));
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    private void setUpTags(final HashtagView htv, Video video) {
        List<Tag> tags = video.getTags();
        if (tags != null && tags.size() > 0) {
            ArrayList<String> canonicalTags = new ArrayList<>();
            Timber.d("setUpTags() : tags.size() - " + tags.size());

            for (Tag tag : tags) {
                String canonicalTag = tag.getCanonical();
                if(canonicalTag.length() > 0) {
                    Timber.d("setUpTags() : canonicalTag - " + canonicalTag);
                    canonicalTags.add(canonicalTag);
                }
            }

            Timber.d("setUpTags() : canonicalTags.size() - " + canonicalTags.size());

            if(canonicalTags.size() > 0){
                htv.setData(canonicalTags, Transformers.HASH);
                htv.setTypeface(mBoldFont);
                htv.setOnTagClickListener(new HashtagView.TagsClickListener() {
                    @Override
                    public void onItemClicked(Object item) {
                        String tag = (String) item;
                        Timber.d("setUpTags() : tag - " + tag);

                        BusProvider.get().post(new SearchPerformedEvent(tag));
                    }
                });
                htv.setVisibility(View.VISIBLE);
            } else {
                htv.setVisibility(View.GONE);
            }
        }
    }


    private void setUpDescription(TextView tv, Video video) {
        String description = video.getDescription();
        if (!TextUtils.isEmpty(description)) {
//            description = description.replaceAll("[\\t\\n\\r]+", "\n");
            tv.setText(description.trim());
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    // endregion

    // region Inner Classes

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.video_thumbnail_iv)
        ImageView mVideoThumbnailImageView;
        @Bind(R.id.title_tv)
        TextView mTitleTextView;
        @Bind(R.id.uploaded_date_tv)
        TextView mUploadedDateTextView;
        @Bind(R.id.duration_tv)
        TextView mDurationTextView;
        @Bind(R.id.subtitle_tv)
        TextView mSubtitleTextView;
        @Bind(R.id.video_row_root_ll)
        LinearLayout mVideoRowRootLinearLayout;

        VideoViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public static class MoreViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.loading_iv)
        LoadingImageView mLoadingImageView;

        MoreViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.title_tv)
        TextView mTitleTextView;
        @Bind(R.id.subtitle_tv)
        TextView mSubtitleTextView;
        @Bind(R.id.user_iv)
        CircleImageView mUserImageView;
        @Bind(R.id.view_count_tv)
        TextView mViewCountTextView;
        @Bind(R.id.upload_date_tv)
        TextView mUploadDateTextView;
        @Bind(R.id.htv)
        HashtagView mHashtagView;
        @Bind(R.id.description_tv)
        TextView mDescriptionTextView;
        @Bind(R.id.additional_info_ll)
        LinearLayout mAdditionalInfoLinearLayout;

        HeaderViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    // endregion

}