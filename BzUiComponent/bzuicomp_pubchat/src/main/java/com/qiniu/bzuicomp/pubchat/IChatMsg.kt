package com.qiniu.bzuicomp.pubchat

import com.alibaba.fastjson.annotation.JSONField

/**
 * 公屏数据抽象
 */
interface IChatMsg {

    fun pubchat_senderAvatar():String
    fun pubchat_sendName():String
    fun pubchat_msgOrigin():String
    fun pubchat_sendID():String
    /**
     * 返回 html格式公屏样式
     */
    @JSONField(serialize = false)
    fun pubchat_getMsgHtml():String
    @JSONField(serialize = false)
    fun pubchat_getMsgAction():String


}