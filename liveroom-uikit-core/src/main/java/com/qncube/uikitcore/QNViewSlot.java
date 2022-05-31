package com.qncube.uikitcore;

import android.content.Context;
import android.view.ContentInfo;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

import com.qncube.liveroomcore.QNLiveRoomClient;



/**
 * ui槽位置
 */
public interface QNViewSlot  {

    /**
     * 创建组件UI

     * @param client   房间实例
     * @param container 填充它的父容器
     * @return 返回一个ui
     */
    public View createView(@NonNull LifecycleOwner lifecycleOwner,@NonNull KitContext context, @NonNull QNLiveRoomClient client, @Nullable ViewGroup container);

}