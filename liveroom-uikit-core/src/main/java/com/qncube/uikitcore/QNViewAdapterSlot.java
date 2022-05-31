package com.qncube.uikitcore;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.qncube.liveroomcore.QNLiveRoomClient;

/**
 * 列表项目槽位item适配器
 * @param <T>
 */
public interface QNViewAdapterSlot<T> {

    /**
     * 创建列表适配器
     * @return
     */
    public BaseQuickAdapter<T, BaseViewHolder> createAdapter(@NonNull  LifecycleOwner lifecycleOwner, @NonNull KitContext context, @NonNull QNLiveRoomClient client);
}
