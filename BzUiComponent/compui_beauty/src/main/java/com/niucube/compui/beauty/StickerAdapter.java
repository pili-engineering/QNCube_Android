package com.niucube.compui.beauty;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import androidx.recyclerview.widget.RecyclerView;

import com.niucube.compui.beauty.model.StickerItem;

import java.util.ArrayList;

public class StickerAdapter extends RecyclerView.Adapter {

    ArrayList<StickerItem> mStickerList;
    private View.OnClickListener mOnClickStickerListener;
    private int mSelectedPosition = 0;
    Context mContext;

    public StickerAdapter(ArrayList<StickerItem> list, Context context) {
        mStickerList = list;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sticker_item, null);
        return new StickerViewHolder(view);
    }

    /**
     * loading 状态绑定
     *
     * @param stickerItem
     * @param holder
     * @param position
     */
    private void bindState(StickerItem stickerItem, StickerViewHolder holder, int position) {
        if (stickerItem != null) {
            switch (stickerItem.state) {
                case NORMAL_STATE:
                    //设置为等待下载状态
                    if (holder.normalState.getVisibility() != View.VISIBLE) {
                        holder.normalState.setVisibility(View.VISIBLE);
                        holder.downloadingState.setVisibility((View.INVISIBLE));
                        holder.downloadingState.setActivated(false);
                        holder.loadingStateParent.setVisibility((View.INVISIBLE));
                    }
                    break;
                case LOADING_STATE:
                    //设置为loading 状态
                    if (holder.downloadingState.getVisibility() != View.VISIBLE) {
                        holder.normalState.setVisibility(View.INVISIBLE);
                        holder.downloadingState.setActivated(true);
                        holder.downloadingState.setVisibility((View.VISIBLE));
                        holder.loadingStateParent.setVisibility((View.VISIBLE));
                    }

                    break;
                case DONE_STATE:
                    //设置为下载完成状态
                    if (holder.normalState.getVisibility() != View.INVISIBLE || holder.downloadingState.getVisibility() != View.INVISIBLE) {
                        holder.normalState.setVisibility(View.INVISIBLE);
                        holder.downloadingState.setVisibility((View.INVISIBLE));
                        holder.downloadingState.setActivated(false);
                        holder.loadingStateParent.setVisibility((View.INVISIBLE));
                    }

                    break;
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final StickerViewHolder viewHolder = (StickerViewHolder) holder;
        viewHolder.imageView.setImageBitmap(mStickerList.get(position).icon);
        bindState(getItem(position), viewHolder, position);
        holder.itemView.setSelected(mSelectedPosition == position);
        if (mOnClickStickerListener != null) {
            holder.itemView.setTag(position);
            holder.itemView.setOnClickListener(mOnClickStickerListener);
        }
    }

    public void setClickStickerListener(View.OnClickListener listener) {
        mOnClickStickerListener = listener;
    }

    public StickerItem getItem(int position) {
        if (position >= 0 && position < getItemCount()) {
            return mStickerList.get(position);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return mStickerList.size();
    }

    public void recycle() {
        for (StickerItem item :
                mStickerList) {
            item.recycle();
        }
    }

    static class StickerViewHolder extends RecyclerView.ViewHolder {

        View view;
        ImageView imageView;
        ImageView normalState;
        ImageView downloadingState;
        ViewGroup loadingStateParent;

        public StickerViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            imageView = (ImageView) itemView.findViewById(R.id.icon);
            normalState = (ImageView) itemView.findViewById(R.id.normalState);
            downloadingState = (ImageView) itemView.findViewById(R.id.downloadingState);
            loadingStateParent = (ViewGroup) itemView.findViewById(R.id.loadingStateParent);
        }
    }

    public void setSelectedPosition(int position) {
        mSelectedPosition = position;
        notifyDataSetChanged();
    }
}
