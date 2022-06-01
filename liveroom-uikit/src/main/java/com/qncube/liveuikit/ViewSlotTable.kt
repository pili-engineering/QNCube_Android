package com.qncube.liveuikit

import com.qncube.kitlivepre.LivePreViewSlot
import com.qncube.uikitcore.QNEmptyViewSlot
import com.qncube.uikitcore.QNInternalViewSlot
import com.qncube.uikitdanmaku.DanmakuTrackManagerSlot
import com.qncube.uikitlinkmic.*
import com.qncube.uikitpublicchat.InputSlot
import com.qncube.uikitpublicchat.PublicChatSlot
import com.qncube.uikitpublicchat.RoomNoticeSlot
import com.qncube.uikituser.*

/**
 * 槽位表
 */
class ViewSlotTable {

    /**
     * 房间背景图
     */
    val mRoomBackGroundSlot = RoomBackGroundSlot()

    /**
     * 房间左上角房主，房主槽位置
     */
    val mRoomHostSlot = RoomHostSlot()

    /**
     * 开播准备
     */
    val mLivePreViewSlot = LivePreViewSlot()

    /**
     * 右上角在线用户槽位
     */
    val mOnlineUserSlot = OnlineUserSlot()

    /**
     * 右上角成员列表
     */
    val mRoomMemberCountSlot = RoomMemberCountSlot()

    /**
     * 右上角房间id 位置
     */
    val mRoomIdSlot = RoomIdSlot()

    /**
     * 右上角房间计时器
     */
    val mRoomTimerSlot = RoomTimerSlot()

    /**
     * 弹幕槽位
     */
    val mDanmakuTrackManagerSlot = DanmakuTrackManagerSlot()

    /**
     *  公屏聊天
     */
    val mPublicChatSlot = PublicChatSlot()


    /**
     * 主播开始pk槽位置
     */
    val mStartPKSlot = StartPKSlot()

    /**
     * PK覆盖层
     */
    val mPKCoverSlot = QNEmptyViewSlot()

    /**
     * pk主播两个小窗口
     */
    val mPKAnchorPreviewSlot = PKAnchorPreviewSlot()

    /**
     *连麦中的用户麦位列表 槽位
     */
    val mLinkerSlot = LinkerSlot()

    /**
     * 房间底部 输入框
     */
    val mInputSlot = InputSlot()

    /**
     *  右下角功能栏目
     */
    val mBottomFucBarSlot = BottomFucBarSlot()

    /**
     * 全局上层覆盖自定义 槽位
     * 空槽位
     */
    val mOuterCoverSlot = QNEmptyViewSlot()

    /**
     * 全局底层覆盖自定义 槽位
     * 空槽位
     */
    val mInnerCoverSlot = QNEmptyViewSlot()

    /**
     * 主播收到连麦申请弹窗
     */
    val mAnchorReceivedLinkMicApplySlot = AnchorReceivedLinkMicApplySlot()

    /**
     * 主播收到pk邀请弹窗
     */
    val mAnchorReceivedPKApplySlot = AnchorReceivedPKApplySlot()


}