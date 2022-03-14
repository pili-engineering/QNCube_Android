package com.niucube.qnrtcsdk

import android.content.Context
import com.qiniu.droid.rtc.*

open class RtcEngineWrap(
    val context: Context,
    val setting: QNRTCSetting = QNRTCSetting(),
    val mQNRTCClientConfig:QNRTCClientConfig = QNRTCClientConfig(QNClientMode.LIVE, QNClientRole.BROADCASTER)
) {

    /**
     * 额外的引擎监听包装为了把让各个模块都能监听rtc事件处理自己的逻辑
     */
    open val mQNRTCEngineEventWrap = QNRTCEngineEventWrap()

    /**
     *  添加你需要的引擎状回调
     */
    fun addExtraQNRTCEngineEventListener(extraQNRTCEngineEventListener: ExtQNClientEventListener) {
        mQNRTCEngineEventWrap.addExtraQNRTCEngineEventListener(extraQNRTCEngineEventListener)
    }

    /**
     * 移除额外的监听
     */
    fun removeExtraQNRTCEngineEventListener(extraQNRTCEngineEventListener: ExtQNClientEventListener) {
        mQNRTCEngineEventWrap.removeExtraQNRTCEngineEventListener(extraQNRTCEngineEventListener)
    }

    private val mQNRTCEventListener = QNRTCEventListener { }

    init {
        QNRTC.init(context, setting, mQNRTCEventListener) // 初始化
    }

    /**
     *  rtc
     */
    val mClient by lazy {
        QNRTC.createClient(mQNRTCClientConfig,mQNRTCEngineEventWrap).apply {
            setAutoSubscribe(false)
        }
    }

}