package com.qncube.uikitcore;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

import com.qncube.liveroomcore.QNLiveRoomClient;

/**
 * roomKit内置槽位置
 */
public abstract class QNInternalViewSlot implements QNViewSlot {
    /**
     * 用户自定义替换方案
     * 如果设置了代理 则使用代理替换实现
     */
    public QNViewSlot mViewSlotProxy = null;

    /**
     * 是否需要
     * 默认要
     */
    public boolean isEnable = true;

    @Override
    public final View createView(@NonNull LifecycleOwner lifecycleOwner, @NonNull KitContext context, @NonNull QNLiveRoomClient client, ViewGroup container) {
        //如果设置替换代理

        if (mViewSlotProxy != null) {
            return mViewSlotProxy.createView(lifecycleOwner, context, client, container);
        } else if (isEnable) {
            return createViewInternal(lifecycleOwner, context, client, container);
        } else {
            return null;
        }
    }

    /**
     * 没有设置代理 时候 使用的默认创建ui
     *
     * @param client
     * @param container
     * @return
     */
    protected abstract View createViewInternal(@NonNull LifecycleOwner lifecycleOwner, @NonNull KitContext context, @NonNull QNLiveRoomClient client, ViewGroup container);

}