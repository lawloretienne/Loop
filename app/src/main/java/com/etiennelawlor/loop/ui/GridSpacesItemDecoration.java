package com.etiennelawlor.loop.ui;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by etiennelawlor on 1/8/15.
 */
public class GridSpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public GridSpacesItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        if(parent.getChildAdapterPosition(view) == 0 || parent.getChildAdapterPosition(view) == 1){
            outRect.top = space;
        }

        if(parent.getChildAdapterPosition(view) % 2 == 0){
            outRect.left = space;
            outRect.right = space/2;
        } else {
            outRect.left = space/2;
            outRect.right = space;
        }

        outRect.bottom = space;
    }
}
