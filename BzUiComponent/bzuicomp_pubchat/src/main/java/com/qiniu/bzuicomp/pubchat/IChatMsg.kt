package com.qiniu.bzuicomp.pubchat

import com.alibaba.fastjson.annotation.JSONField

/**
 * 公屏数据抽象
 */
interface IChatMsg {
    /**
     * 返回 html格式公屏样式
     */
    @JSONField(serialize = false)
    fun getMsgHtml():String
    @JSONField(serialize = false)
    fun getMsgAction():String
}