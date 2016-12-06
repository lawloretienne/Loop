package com.etiennelawlor.loop.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.etiennelawlor.loop.R;
import com.etiennelawlor.loop.network.models.response.Category;
import com.etiennelawlor.loop.network.models.response.Pictures;
import com.etiennelawlor.loop.network.models.response.Size;
import com.etiennelawlor.loop.ui.DynamicHeightImageView;
import com.etiennelawlor.loop.ui.LoadingImageView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by etiennelawlor on 5/23/15.
 */

public class CategoriesAdapter extends BaseAdapter<Category> {

    // region Member Variables
    private FooterViewHolder footerViewHolder;
    // endregion

    // region Constructors
    public CategoriesAdapter() {
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
        final CategoryViewHolder holder = (CategoryViewHolder) viewHolder;

        Category category = getItem(position);
        if (category != null) {
            setUpThumbnail(holder.videoThumbnailImageView, category);
            setUpTitle(holder.titleTextView, category);
        }
    }

    @Override
    protected void bindFooterViewHolder(RecyclerView.ViewHolder viewHolder) {
        FooterViewHolder holder = (FooterViewHolder) viewHolder;
        footerViewHolder = holder;
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
        add(new Category());
    }

    // region Helper Methods

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

    public static class FooterViewHolder extends RecyclerView.ViewHolder {
        // region Views
        @Bind(R.id.loading_fl)
        FrameLayout loadingFrameLayout;
        @Bind(R.id.error_rl)
        RelativeLayout errorRelativeLayout;
        @Bind(R.id.loading_iv)
        LoadingImageView loadingImageView;
        @Bind(R.id.reload_btn)
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