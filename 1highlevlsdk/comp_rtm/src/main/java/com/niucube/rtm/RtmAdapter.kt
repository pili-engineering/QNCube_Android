package com.niucube.rtm

/**
 * im 适配器
 */
interface RtmAdapter {

    /**
     * 发c2c消息
     * @param isDispatchToLocal 发送成功后是否马上往本地的监听分发这个消息
     *
     */
    fun sendC2cMsg(msg: String, peerId:String, isDispatchToLocal:Boolean, callBack: RtmCallBack?)

    /**
     * 发频道消息
     */
    fun sendChannelMsg(msg:String,channelId:String,isDispatchToLocal:Boolean,callBack: RtmCallBack?)

    /**
     * 创建频道
     */
    fun createChannel(channelId :String,callBack: RtmCallBack?)
    /**
     * 创建频道
     */
    fun joinChannel(channelId :String,callBack: RtmCallBack?)

    /**
     * 离开频道
     */
    fun leaveChannel(channelId :String,callBack: RtmCallBack?)

    /**
     * 销毁频道
     */
    fun releaseChannel(channelId :String,callBack: RtmCallBack?)

    /**
     * 获得当前登陆用户的id
     */
    fun getLoginUserId():String

    /**
     * 注册监听
     * @param c2cMessageReceiver  c2c消息接收器
     * @param channelMsgReceiver 群消息接收器
     */
    fun registerOriginImListener(c2cMessageReceiver:(msg: String, fromId: String, toId: String)->Unit , channelMsgReceiver:(msg: String, fromId: String, toId: String)->Unit)

}