package com.qncube.liveuikit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.qbcube.pkservice.PKInvitation
import com.qbcube.pkservice.QNPKInvitationHandler
import com.qbcube.pkservice.QNPKService
import com.qbcube.pkservice.QNPKSession
import com.qncube.liveroomcore.Extension
import com.qncube.liveroomcore.QNLiveRoomClient
import com.qncube.liveroomcore.asToast
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.uikitcore.BaseSlotView
import com.qncube.uikitcore.KitContext
import com.qncube.uikitcore.QNInternalViewSlot
import kotlinx.android.synthetic.main.kit_img_bg.view.*

/**
 * 房间背景图
 */
class RoomBackGroundSlot : QNInternalViewSlot() {

    //默认背景图片
    var defaultBackGroundImg = R.drawable.kit_dafault_room_bg

    override fun createViewInternal(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient,
        container: ViewGroup?
    ): View {
        val view = RoomBackGroundView();
        view.defaultBackGroundImg = defaultBackGroundImg
        view.attach(lifecycleOwner, context, client)
        return view.createView(LayoutInflater.from(context.androidContext), container)
    }
}

class RoomBackGroundView : BaseSlotView() {


    private val mPKServiceListener = object : QNPKService.PKServiceListener {
        override fun onInitPKer(pkSession: QNPKSession) {

            view!!.ivBg.setImageResource(defaultBackGroundImg)
        }

        override fun onStart(pkSession: QNPKSession) {
            view!!.ivBg.setImageResource(defaultBackGroundImg)
        }

        override fun onStop(pkSession: QNPKSession, code: Int, msg: String) {
            Glide.with(context!!).load(roomInfo?.coverUrl)
                .into(view!!.ivBg)
        }

        override fun onWaitPeerTimeOut(pkSession: QNPKSession) {
        }

        override fun onPKExtensionUpdate(pkSession: QNPKSession, extension: Extension) {}
    }
    var defaultBackGroundImg = R.drawable.kit_dafault_room_bg
    override fun getLayoutId(): Int {
        return R.layout.kit_img_bg
    }

    override fun initView() {
        super.initView()
        view!!.ivBg.setImageResource(defaultBackGroundImg)
    }

    override fun onRoomJoined(roomInfo: QNLiveRoomInfo) {
        super.onRoomJoined(roomInfo)
        Glide.with(context!!).load(roomInfo.coverUrl)
            .into(view!!.ivBg)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        super.onStateChanged(source, event)
        if (event == Lifecycle.Event.ON_DESTROY) {
            client?.getService(QNPKService::class.java)?.removePKServiceListener(mPKServiceListener)
        }
    }
    override fun attach(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient
    ) {
        super.attach(lifecycleOwner, context, client)

        client.getService(QNPKService::class.java).addPKServiceListener(mPKServiceListener)
    }

}