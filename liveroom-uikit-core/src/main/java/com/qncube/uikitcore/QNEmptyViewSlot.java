package com.qncube.uikitcore;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

import com.qncube.liveroomcore.QNLiveRoomClient;

/**
 * 空位置 等带用户插入代理
 */
public class QNEmptyViewSlot implements QNViewSlot {

    /**
     * 用户自定义替换方案
     */
    public QNViewSlot mViewSlotProxy = null;

    @Override
    public final View createView(@NonNull LifecycleOwner lifecycleOwner, @NonNull KitContext context, @NonNull QNLiveRoomClient client, ViewGroup container) {
        //如果设置替换代理

        if (mViewSlotProxy != null) {
            return mViewSlotProxy.createView(lifecycleOwner, context, client, container);
        } else {
            return null;
        }
    }
}



