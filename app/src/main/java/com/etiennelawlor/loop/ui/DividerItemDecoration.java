package com.etiennelawlor.loop.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.etiennelawlor.loop.utilities.DisplayUtility;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    // region Member Variables
    private Drawable divider;
    private boolean showFirstDivider = false;
    private boolean showLastDivider = false;
    int mOrientation = -1;
    // endregion

    // region Constructors
    public DividerItemDecoration(Context context, AttributeSet attrs) {
        final TypedArray a = context
                .obtainStyledAttributes(attrs, new int[]{android.R.attr.listDivider});
        divider = a.getDrawable(0);
        a.recycle();
    }

    public DividerItemDecoration(Context context, AttributeSet attrs, boolean showFirstDivider,
                                 boolean showLastDivider) {
        this(context, attrs);
        this.showFirstDivider = showFirstDivider;
        this.showLastDivider = showLastDivider;
    }

    public DividerItemDecoration(Context context, int resId) {
        divider = ContextCompat.getDrawable(context, resId);
    }

    public DividerItemDecoration(Context context, int resId, boolean showFirstDivider,
                                 boolean showLastDivider) {
        this(context, resId);
        this.showFirstDivider = showFirstDivider;
        this.showLastDivider = showLastDivider;
    }

    public DividerItemDecoration(Drawable divider) {
        divider = divider;
    }

    public DividerItemDecoration(Drawable divider, boolean showFirstDivider,
                                 boolean showLastDivider) {
        this(divider);
        this.showFirstDivider = showFirstDivider;
        this.showLastDivider = showLastDivider;
    }
    // endregion

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (divider == null) {
            return;
        }

        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION || (position == 0 && !showFirstDivider)) {
            return;
        }

        if (mOrientation == -1)
            getOrientation(parent);

        if (mOrientation == LinearLayoutManager.VERTICAL) {
            outRect.top = divider.getIntrinsicHeight();
            if (showLastDivider && position == (state.getItemCount() - 1)) {
                outRect.bottom = outRect.top;
            }
        } else {
            outRect.left = divider.getIntrinsicWidth();
            if (showLastDivider && position == (state.getItemCount() - 1)) {
                outRect.right = outRect.left;
            }
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (divider == null) {
            super.onDrawOver(c, parent, state);
            return;
        }

        // Initialization needed to avoid compiler warning
        int left = 0, right = 0, top = 0, bottom = 0, size;
        int orientation = mOrientation != -1 ? mOrientation : getOrientation(parent);
        int childCount = parent.getChildCount();

        if (orientation == LinearLayoutManager.VERTICAL) {
            size = divider.getIntrinsicHeight();
            left = parent.getPaddingLeft() + DisplayUtility.dp2px(parent.getContext(), 56);
            right = parent.getWidth() - parent.getPaddingRight();
        } else { //horizontal
            size = divider.getIntrinsicWidth();
            top = parent.getPaddingTop();
            bottom = parent.getHeight() - parent.getPaddingBottom();
        }

        for (int i = showFirstDivider ? 0 : 1; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            if (orientation == LinearLayoutManager.VERTICAL) {
                top = child.getTop() - params.topMargin - size;
                bottom = top + size;
            } else { //horizontal
                left = child.getLeft() - params.leftMargin + DisplayUtility.dp2px(parent.getContext(), 56);
                right = left + size;
            }

            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }

        // show last divider
        if (showLastDivider && childCount > 0) {
            View child = parent.getChildAt(childCount - 1);
            if (parent.getChildAdapterPosition(child) == (state.getItemCount() - 1)) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                if (orientation == LinearLayoutManager.VERTICAL) {
                    top = child.getBottom() + params.bottomMargin;
                    bottom = top + size;
                } else { // horizontal
                    left = child.getRight() + params.rightMargin + DisplayUtility.dp2px(parent.getContext(), 56);
                    right = left + size;
                }
                divider.setBounds(left, top, right, bottom);
                divider.draw(c);
            }
        }
    }

    // region Helper Methods
    private int getOrientation(RecyclerView parent) {
        if (mOrientation == -1) {
            if (parent.getLayoutManager() instanceof LinearLayoutManager) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
                mOrientation = layoutManager.getOrientation();
            } else {
                throw new IllegalStateException(
                        "DividerItemDecoration can only be used with a LinearLayoutManager.");
            }
        }
        return mOrientation;
    }
    // endregion
}
