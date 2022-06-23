package com.niucube.rtm

object RtmManager {

    lateinit var rtmClient: RtmAdapter
        private set
    var isInit = false
        private set
    private var mRtmC2cListeners = ArrayList<RtmMsgListener>()
    private var mRtmChannelListeners = ArrayList<RtmMsgListener>()

    /**
     * 添加点对点消息监听
     */
    fun addRtmC2cListener(rtmC2cListener: RtmMsgListener) {
        if(!mRtmC2cListeners.contains(rtmC2cListener)){
            mRtmC2cListeners.add(rtmC2cListener)
        }
    }

    /**
     * 移除点对点消息
     */
    fun removeRtmC2cListener(rtmC2cListener: RtmMsgListener) {
        mRtmC2cListeners.remove(rtmC2cListener)
    }

    /**
     * 添加群消息监听
     */
    fun addRtmChannelListener(rtmChannelListener: RtmMsgListener) {
        if(!mRtmChannelListeners.contains(rtmChannelListener)){
            mRtmChannelListeners.add(rtmChannelListener)
        }
    }

    /**
     * 添加群消息监听
     */
    fun removeRtmChannelListener(rtmChannelListener: RtmMsgListener) {
        mRtmChannelListeners.remove(rtmChannelListener)
    }

    /**
     * 设置im实现
     */
    fun setRtmAdapter(adapter: RtmAdapter){
        isInit = true
        rtmClient =adapter
        adapter.registerOriginImListener(RtmManager::handleC2cMessage, RtmManager::handleChannelMsg)
    }

    private fun handleC2cMessage(msg: String, fromId: String, toId: String) {
        mRtmC2cListeners.forEach {
            if (it.onNewMsg(msg, fromId, toId)) {
                return@forEach
            }
        }
    }

    private fun handleChannelMsg(msg: String, fromId: String, toId: String) {
        mRtmChannelListeners.forEach {
            if (it.onNewMsg(msg,  fromId, toId)) {
                return@forEach
            }
        }
    }
}