package com.etiennelawlor.loop.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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

/**
 * Created by etiennelawlor on 5/23/15.
 */

public class CategoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // region Member Variables
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
    public CategoriesAdapter() {
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

    public Category getItem(int position) {
        return mCategories.get(position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    private RecyclerView.ViewHolder createCategoryViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_card, parent, false);

        final CategoryViewHolder holder = new CategoryViewHolder(v);

        holder.mCategoryCardRootFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPos = holder.getAdapterPosition();
                if(adapterPos != RecyclerView.NO_POSITION){
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(adapterPos, holder.itemView);
                    }
                }
            }
        });

        return holder;
    }

    private void bindCategoryViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final CategoryViewHolder holder = (CategoryViewHolder) viewHolder;

        Category category = mCategories.get(position);
        if (category != null) {
            setUpThumbnail(holder.mVideoThumbnailImageView, category);
            setUpTitle(holder.mTitleTextView, category);
        }
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
        @Bind(R.id.thumbnail_iv)
        DynamicHeightImageView mVideoThumbnailImageView;
        @Bind(R.id.title_tv)
        TextView mTitleTextView;
        @Bind(R.id.category_card_root_fl)
        FrameLayout mCategoryCardRootFrameLayout;

        public CategoryViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
    // endregion

}