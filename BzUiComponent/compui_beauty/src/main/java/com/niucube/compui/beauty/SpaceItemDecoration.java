package com.niucube.compui.beauty;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

// 分隔间距,继承于 RecyclerView.ItemDecoration
class SpaceItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public SpaceItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (parent.getChildAdapterPosition(view) != 0) {
            outRect.top = space;
        }
    }
}