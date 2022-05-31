package com.qncube.liveuikit;

import android.Manifest;
import android.content.Context;

import com.qncube.liveroomcore.QNLiveCallBack;
import com.qncube.liveroomcore.mode.QNLiveRoomInfo;
import com.qncube.uikitcore.ext.permission.PermissionAnywhere;

/**
 * ui kit
 */
public class QNLiveRoomUIKit {

    /**
     * 槽位表
     */
    public static final ViewSlotTable mViewSlotTable = new ViewSlotTable();


    /**
     * 加入房间
     *
     * @param context
     * @param callBack
     */
    public static void joinRoom(Context context, String liveRoomId, QNLiveCallBack<QNLiveRoomInfo> callBack) {
        RoomPullActivity.Companion.start(context,liveRoomId,callBack);
    }

    /**
     * 主播开播
     *
     * @param context
     * @param callBack
     */
    public static void createAndJoinRoom(Context context, QNLiveCallBack<QNLiveRoomInfo> callBack) {
        RoomPushActivity.Companion.start(context, callBack);
    }
}