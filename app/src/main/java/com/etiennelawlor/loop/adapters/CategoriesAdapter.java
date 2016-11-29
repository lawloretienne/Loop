package com.etiennelawlor.loop.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.network.models.response.Category;
import com.etiennelawlor.loop.network.models.response.Pictures;
import com.etiennelawlor.loop.network.models.response.Size;
import com.etiennelawlor.loop.ui.DynamicHeightImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 5/23/15.
 */

public class CategoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // region Constants
    private static final int HEADER = 0;
    private static final int ITEM = 1;
    private static final int LOADING = 2;
    // endregion

    // region Member Variables
    private List<Category> categories;
    private OnItemClickListener onItemClickListener;
    private boolean isLoadingFooterAdded = false;
    // endregion

    // region Listeners
    // endregion

    // region Interfaces
    public interface OnItemClickListener {
        void onItemClick(int position, View view);
    }
    // endregion

    // region Constructors
    public CategoriesAdapter() {
        categories = new ArrayList<>();
    }
    // endregion

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;

        switch (viewType) {
            case HEADER:
                break;
            case ITEM:
                viewHolder = createCategoryViewHolder(parent);
                break;
            case LOADING:
                viewHolder = createLoadingViewHolder(parent);
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
            case HEADER:
                break;
            case ITEM:
                bindCategoryViewHolder(viewHolder, position);
                break;
            case LOADING:
                bindLoadingViewHolder(viewHolder);
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    @Override
    public int getItemViewType(int position) {
//        return (position == mEvents.size()-1 && isLoadingFooterAdded) ? LOADING : ITEM;
        return ITEM;
    }

    // region Helper Methods
    private RecyclerView.ViewHolder createCategoryViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_card, parent, false);

        final CategoryViewHolder holder = new CategoryViewHolder(v);

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

    private RecyclerView.ViewHolder createLoadingViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more, parent, false);

        return new MoreViewHolder(v);
    }

    private void bindCategoryViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final CategoryViewHolder holder = (CategoryViewHolder) viewHolder;

        Category category = categories.get(position);
        if (category != null) {
            setUpThumbnail(holder.videoThumbnailImageView, category);
            setUpTitle(holder.titleTextView, category);
        }
    }

    private void bindLoadingViewHolder(RecyclerView.ViewHolder viewHolder) {
        MoreViewHolder holder = (MoreViewHolder) viewHolder;
    }

    private void add(Category item) {
        categories.add(item);
        notifyItemInserted(categories.size() - 1);
    }

    public void addAll(List<Category> categories) {
        for (Category category : categories) {
            add(category);
        }
    }

    public void remove(Category item) {
        int position = categories.indexOf(item);
        if (position > -1) {
            categories.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingFooterAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public void addLoading() {
        isLoadingFooterAdded = true;
        add(new Category());
    }

    public void removeLoading() {
        isLoadingFooterAdded = false;

        int position = categories.size() - 1;
        Category item = getItem(position);

        if (item != null) {
            categories.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Category getItem(int position) {
        return categories.get(position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private void setUpThumbnail(DynamicHeightImageView iv, Category category){
//            holder.mVideoThumbnailImageView.setHeightRatio(9.0D/16.0D);
        iv.setHeightRatio(1.0D/1.0D);

        Pictures pictures = category.getPictures();
        if(pictures != null){
            List<Size> sizes = pictures.getSizes();
            if(sizes != null && sizes.size() > 0){
                Size size = sizes.get(sizes.size()-1);
                if(size != null){
                    String thumbnail = size.getLink();
                    if(!TextUtils.isEmpty(thumbnail)){
                        Glide.with(iv.getContext())
                                .load(thumbnail)
//                                .placeholder(R.drawable.ic_placeholder)
//                                .error(R.drawable.ic_error)
                                .into(iv);
                    }
                }
            }
        }
    }

    private void setUpTitle(TextView tv, Category category){
        String name = category.getName();

        if(!TextUtils.isEmpty(name)){
            if(name.contains("&")){
                name = name.replace("&", "\n&");
            }

            tv.setText(name);
        }
    }

    // endregion

    // region Inner Classes

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        // region Views
        @Bind(R.id.thumbnail_iv)
        DynamicHeightImageView videoThumbnailImageView;
        @Bind(R.id.title_tv)
        TextView titleTextView;
        // endregion

        // region Constructors
        public CategoryViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
        // endregion
    }

    public static class MoreViewHolder extends RecyclerView.ViewHolder {
        // region Views
        // endregion

        // region Constructors
        public MoreViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
        // endregion
    }
    // endregion

}