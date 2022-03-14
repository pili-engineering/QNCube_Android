package com.qiniu.bzuicomp.pubchat

class InterviewRoomTipMsg(val msgContent: String) : IChatMsg {
    /**
     * 返回 html格式公屏样式
     */
    override fun getMsgHtml(): String {
        return " <font color='#50000'>$msgContent</font> "
    }

    override fun getMsgAction(): String {
        return "InterviewRoomTipMsg"
    }
}