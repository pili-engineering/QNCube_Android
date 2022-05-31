package com.qncube.uikitdanmaku

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.qncube.danmakuservice.DanmakuModel
import com.qncube.danmakuservice.QNDanmakuService
import com.qncube.liveroomcore.QNLiveCallBack
import com.qncube.liveroomcore.asToast
import com.qncube.uikitcore.BaseSlotView

class SendDanmakuView : BaseSlotView() {


    override fun getLayoutId(): Int {
       return R.layout.kit_view_send_danmaku
    }

    fun send(msg: String) {
        client?.getService(QNDanmakuService::class.java)?.sendDanmaku(msg, null,
            object : QNLiveCallBack<DanmakuModel> {
                override fun onError(code: Int, msg: String?) {
                    msg?.asToast()
                }

                override fun onSuccess(data: DanmakuModel?) {
                }
            })
    }
}