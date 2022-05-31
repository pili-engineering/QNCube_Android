package com.qncube.uikitcore;


import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.qncube.liveroomcore.QNLiveRoomClient;

/**
 * 点击item 回调槽位置
 *
 * @param <T>
 */
public interface QNViewClickSlot<T>  {

    /**
     * 点击列表item
     * @param itemData
     */
    View.OnClickListener createItemClick(@NonNull KitContext context, @NonNull QNLiveRoomClient client, T itemData);
}