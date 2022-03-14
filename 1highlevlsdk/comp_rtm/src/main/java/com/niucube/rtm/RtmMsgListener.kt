package com.niucube.rtm

interface RtmMsgListener {
    /**
     * 收到消息
     * @return 是否继续分发
     */
    fun onNewMsg(msg: String, peerId:String):Boolean
}