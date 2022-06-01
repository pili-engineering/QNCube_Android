package com.qncube.linkmicservice

import com.nucube.rtclive.QNCameraParams
import com.nucube.rtclive.QNMicrophoneParams
import com.qiniu.droid.rtc.QNAudioFrameListener
import com.qiniu.droid.rtc.QNConnectionState
import com.qiniu.droid.rtc.QNVideoFrameListener
import com.qncube.liveroomcore.IPullPlayer
import com.qncube.liveroomcore.QNLiveCallBack


/**
 * 观众连麦器
 */
interface QNAudienceMicLinker {

    //观众连麦监听
    public interface LinkMicListener {

        /**
         * 连麦模式连接状态
         * 连接成功后 连麦器会主动禁用推流器 改用rtc
         * @param state
         */
        fun onConnectionStateChanged(
            state: QNConnectionState
        )

        /**
         * 本地角色变化
         */
        fun lonLocalRoleChange(isLinker: Boolean)
    }

    /**
     *  添加连麦监听
     */
    fun addLinkMicListener(listener: LinkMicListener)

    /**
     * 移除连麦监听
     */
    fun removeLinkMicListener(listener: LinkMicListener)

    /**
     * 开始上麦
     *
     * @param cameraParams
     * @param microphoneParams
     * @param callBack         上麦成功失败回调
     */
    fun startLink(
        extensions: HashMap<String, String>?, cameraParams: QNCameraParams?,
        microphoneParams: QNMicrophoneParams?, callBack: QNLiveCallBack<Void>?
    )

    /**
     * 我是不是麦上用户
     */
    fun isLinked(): Boolean

    /**
     * 结束连麦
     */
    fun stopLink(callBack: QNLiveCallBack<Void>?)


    fun switchCamera()

    fun muteLocalCamera(muted: Boolean, callBack: QNLiveCallBack<Void>?)

    fun muteLocalMicrophone(muted: Boolean, callBack: QNLiveCallBack<Void>?)

    fun setVideoFrameListener(frameListener: QNVideoFrameListener)

    fun setAudioFrameListener(frameListener: QNAudioFrameListener)


}