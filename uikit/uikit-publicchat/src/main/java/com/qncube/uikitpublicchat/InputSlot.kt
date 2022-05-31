package com.qncube.uikitpublicchat

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import com.qncube.liveroomcore.QNLiveCallBack
import com.qncube.liveroomcore.QNLiveRoomClient
import com.qncube.publicchatservice.PubChatModel
import com.qncube.publicchatservice.QNPublicChatService
import com.qncube.uikitcore.BaseSlotView
import com.qncube.uikitcore.KitContext
import com.qncube.uikitcore.QNInternalViewSlot
import com.qucube.uikitinput.RoomInputDialog
import kotlinx.android.synthetic.main.kit_input.view.*


class InputSlot : QNInternalViewSlot() {

    override fun createViewInternal(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient,
        container: ViewGroup?
    ): View {
        val view = InputView()
        view.attach(lifecycleOwner, context, client)
        return view.createView(LayoutInflater.from(context.androidContext), container)
    }
}

class InputView : BaseSlotView() {

    override fun getLayoutId(): Int {
        return R.layout.kit_input
    }

    override fun initView() {
        super.initView()
        view!!.flInput.setOnClickListener {
            RoomInputDialog().apply {
                sendPubCall = {
                    client?.getService(QNPublicChatService::class.java)
                        ?.sendPublicChat(it, object : QNLiveCallBack<PubChatModel> {
                            override fun onError(code: Int, msg: String) {
                                Toast.makeText(context, "发送失败" + msg, Toast.LENGTH_SHORT).show()
                            }

                            override fun onSuccess(data: PubChatModel?) {
                            }
                        })
                }
            }.show(kitContext!!.fm, "")
        }
    }
}