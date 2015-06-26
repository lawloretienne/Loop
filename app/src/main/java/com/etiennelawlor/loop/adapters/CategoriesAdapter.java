package com.etiennelawlor.loop.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.network.models.Category;
import com.etiennelawlor.loop.ui.DynamicHeightImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by etiennelawlor on 5/23/15.
 */

public class CategoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // region Member Variables
    private Context mContext;
    private List<Category> mCategories;
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
    public CategoriesAdapter(Context context) {
        mContext = context;
        mCategories = new ArrayList<>();
    }
    // endregion

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return createCategoryViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        bindCategoryViewHolder(viewHolder, position);
    }

    @Override
    public int getItemCount() {
        return mCategories.size();
    }

    // region Helper Methods
    private void add(Category item) {
        mCategories.add(item);
        notifyItemInserted(mCategories.size());
    }

    public void addAll(List<Category> categories) {
        for (Category category : categories) {
            add(category);
        }
    }

    public void remove(Category item) {
        int position = mCategories.indexOf(item);
        if (position > -1) {
            mCategories.remove(position);
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

//    public void removeLoading() {
//        int position = mVideoWrappers.size() - 1;
//        VideoWrapper item = getItem(position);
//
//        if (item != null && item.getType() == VideoWrapper.LOADING) {
//            mVideoWrappers.remove(position);
//            notifyItemChanged(position + 1);
//        }
//    }

    public Category getItem(int position) {
        return mCategories.get(position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }


//    private RecyclerView.ViewHolder createHeaderViewHolder(ViewGroup parent){
//
//    }

    private RecyclerView.ViewHolder createCategoryViewHolder(ViewGroup parent) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_card, parent, false);

        return new CategoryViewHolder(v);
    }

//    private RecyclerView.ViewHolder createLoadingViewHolder(ViewGroup parent) {
//        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more, parent, false);
//
//        return new MoreViewHolder(v);
//    }

    private void bindCategoryViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        CategoryViewHolder holder = (CategoryViewHolder) viewHolder;

        Category category = mCategories.get(position);
        if (category != null) {

            String name = category.getName();

            if(name.contains("&")){
                name = name.replace("&", "\n&");
            }

            holder.mTitleTextView.setText(name);

            String link = "";

            String[] categoryThumbnails = mContext.getResources().getStringArray(R.array.category_thumbnails);

            holder.mVideoThumbnailImageView.setHeightRatio(9.0D/16.0D);
//            holder.mVideoThumbnailImageView.setHeightRatio(1.0D/1.0D);


            Glide.with(mContext)
                    .load(categoryThumbnails[position])
//                                .placeholder(R.drawable.ic_placeholder)
//                                .error(R.drawable.ic_error)
                    .into(holder.mVideoThumbnailImageView);



//            final Video video = videoWrapper.getVideo();
//
//            if (video != null) {
//                setUpTitle(holder.mTitleTextView, video);
//                setUpSubtitle(holder.mSubtitleTextView, video);
//                setUpVideoThumbnail(holder.mVideoThumbnailImageView, video);
//                setUpDuration(holder.mDurationTextView, video);
//                setUpUploadedDate(holder.mUploadedDateTextView, video);
//            }
        }
    }

//    private void setUpTitle(TextView tv, Video video) {
//        String name = video.getName();
//        if (!TextUtils.isEmpty(name)) {
//            tv.setText(name);
//        }
//    }
//
//    private void setUpSubtitle(TextView tv, Video video) {
//        User user = video.getUser();
//        if (user != null) {
//            String userName = user.getName();
//            if (!TextUtils.isEmpty(userName)) {
//                tv.setText(userName);
//            }
//        }
//    }
//
//    private void setUpVideoThumbnail(ImageView iv, Video video) {
//        Pictures pictures = video.getPictures();
//        if (pictures != null) {
//            List<Size> sizes = pictures.getSizes();
//            if (sizes != null && sizes.size() > 0) {
//                Size size = sizes.get(sizes.size() - 1);
//                if (size != null) {
//                    String link = size.getLink();
//                    if (!TextUtils.isEmpty(link)) {
//                        Picasso.with(mContext)
//                                .load(link)
////                                .placeholder(R.drawable.ic_placeholder)
////                                .error(R.drawable.ic_error)
//                                .into(iv);
//                    }
//                }
//            }
//        }
//    }
//
//    private void setUpDuration(TextView tv, Video video) {
//        Integer duration = video.getDuration();
//
//        long minutes = duration / 60;
//        long seconds = duration % 60;
//
//        String time;
//        if (minutes == 0L) {
//            if (seconds > 0L) {
//                if (seconds < 10L)
//                    time = String.format("0:0%s", String.valueOf(seconds));
//                else
//                    time = String.format("0:%s", String.valueOf(seconds));
//            } else {
//                time = "0:00";
//            }
//
//        } else {
//            if (seconds > 0L) {
//                if (seconds < 10L)
//                    time = String.format("%s:0%s", String.valueOf(minutes), String.valueOf(seconds));
//                else
//                    time = String.format("%s:%s", String.valueOf(minutes), String.valueOf(seconds));
//            } else {
//                time = String.format("%s:00", String.valueOf(minutes));
//            }
//        }
//
//        tv.setText(time);
//    }
//
//    private void setUpUploadedDate(TextView tv, Video video) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ", Locale.ENGLISH);
//        String uploadDate = "";
//
//        String createdTime = video.getCreatedTime();
//        try {
//            Date date = sdf.parse(createdTime);
//
//            Calendar futureCalendar = Calendar.getInstance();
//            futureCalendar.setTime(date);
//
//            uploadDate = LoopUtility.getRelativeDate(futureCalendar);
//        } catch (ParseException e) {
//            Timber.e("");
//        }
//
//        int viewCount = 0;
//        Stats stats = video.getStats();
//        if (stats != null) {
//            viewCount = stats.getPlays();
//        }
//
//        if (viewCount > 0) {
////                String formattedViewCount = NumberFormat.getNumberInstance(Locale.US).format(viewCount);
//            String formattedViewCount = formatViewCount(viewCount);
//            tv.setText(String.format("%s - %s", uploadDate, formattedViewCount));
//        } else {
//            tv.setText(String.format("%s", uploadDate));
//        }
//    }
//
//    private String formatViewCount(int viewCount) {
//        String formattedViewCount = "";
//
//        if (viewCount < 1000000000 && viewCount >= 1000000) {
//            formattedViewCount = String.format("%dM views", viewCount / 1000000);
//        } else if (viewCount < 1000000 && viewCount >= 1000) {
//            formattedViewCount = String.format("%dK views", viewCount / 1000);
//        } else if (viewCount < 1000 && viewCount > 1) {
//            formattedViewCount = String.format("%d views", viewCount);
//        } else if (viewCount == 1) {
//            formattedViewCount = String.format("%d view", viewCount);
//        }
//
//        return formattedViewCount;
//    }
    // endregion

    // region Inner Classes

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.thumbnail_iv)
        DynamicHeightImageView mVideoThumbnailImageView;
        @InjectView(R.id.title_tv)
        TextView mTitleTextView;

        @OnClick(R.id.category_card_root_fl)
        void onCategoryClick() {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(getPosition(), itemView);
            }
        }

        CategoryViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }

//    public static class MoreViewHolder extends RecyclerView.ViewHolder {
//        public MoreViewHolder(View view) {
//            super(view);
//            ProgressBar pb = ButterKnife.findById(view, R.id.progress_bar);
//            pb.getIndeterminateDrawable()
//                    .mutate()
//                    .setColorFilter(view.getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_ATOP);
//        }
//    }

    // endregion

}