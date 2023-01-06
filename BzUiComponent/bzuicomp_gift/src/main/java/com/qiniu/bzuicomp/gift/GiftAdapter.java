package com.qiniu.bzuicomp.gift;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by duanliuyi on 2018/5/11.
 */

public class GiftAdapter extends BaseAdapter {

    private final ArrayList<Gift> gifts;
    private final Context mContext;

    public GiftAdapter(Context mContext, ArrayList<Gift> gifts) {
        this.mContext = mContext;
        this.gifts = gifts;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public Object getItem(int i) {
        return gifts.get(i);
    }

    @Override
    public int getCount() {
        return gifts.size();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.item_gift, null);
            viewHolder.tvGiftName = view.findViewById(R.id.tv_gift_name);
            viewHolder.ivGiftPic = view.findViewById(R.id.iv_gift_pic);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        Gift gift = gifts.get(i);
        viewHolder.tvGiftName.setText(gift.getGiftName());
        viewHolder.ivGiftPic.setImageResource(gift.getGiftRes());
        return view;
    }

    static class ViewHolder {
        TextView tvGiftName;
        ImageView ivGiftPic;
    }
}
