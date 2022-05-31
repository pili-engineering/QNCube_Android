package com.qncube.uikitlinkmic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.qbcube.pkservice.PKInvitation
import com.qbcube.pkservice.QNPKInvitationHandler
import com.qbcube.pkservice.QNPKService
import com.qbcube.pkservice.QNPKSession
import com.qncube.liveroomcore.*
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.uikitcore.BaseSlotView
import com.qncube.uikitcore.KitContext
import com.qncube.uikitcore.QNInternalViewSlot
import com.qncube.uikitcore.dialog.FinalDialogFragment
import com.qncube.uikitcore.dialog.LoadingDialog
import kotlinx.android.synthetic.main.kit_start_pk_view.view.*

class StartPKSlot : QNInternalViewSlot() {

    override fun createViewInternal(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient,
        container: ViewGroup?
    ): View {
        val view = StartPKView()
        view.attach(lifecycleOwner, context, client)
        return view.createView(LayoutInflater.from(context.androidContext), container)
    }
}


class StartPKView : BaseSlotView() {

    private var showingPKListDialog: PKAbleListDialog? = null
    private val mPKServiceListener = object : QNPKService.PKServiceListener {
        override fun onInitPKer(pkSession: QNPKSession) {}
        override fun onStart(pkSession: QNPKSession) {
            view!!. llStartPK.visibility = View.GONE
            view!!. tvStopPK.visibility = View.VISIBLE
        }

        override fun onStop(pkSession: QNPKSession, code: Int, msg: String) {
            view!!.  llStartPK.visibility = View.VISIBLE
            view!!.  tvStopPK.visibility = View.GONE
        }

        override fun onWaitPeerTimeOut(pkSession: QNPKSession) {
            "等待主播 ${pkSession.receiver.nick} 推流超时".asToast()
        }

        override fun onPKExtensionUpdate(pkSession: QNPKSession, extension: Extension) {}
    }

    private val mPKInvitationListener = object : QNPKInvitationHandler.PKInvitationListener {
        override fun onReceivedApply(pkInvitation: PKInvitation) {

        }

        override fun onApplyCanceled(pkInvitation: PKInvitation) {}

        override fun onApplyTimeOut(pkInvitation: PKInvitation) {
            LoadingDialog.cancelLoadingDialog()
            "邀请主播 ${pkInvitation.receiver.nick} 超时".asToast()

        }

        override fun onAccept(pkInvitation: PKInvitation) {
            "主播 ${pkInvitation.receiver.nick} 接收".asToast()
            client?.getService(QNPKService::class.java)?.start(20 * 1000,
                pkInvitation.receiverRoomId, pkInvitation.receiver.userId, null,
                object : QNLiveCallBack<QNPKSession> {
                    override fun onError(code: Int, msg: String) {
                        "开始pk失败 ${msg.asToast()}"
                    }

                    override fun onSuccess(data: QNPKSession) {
                    }
                })
            showingPKListDialog?.dismiss()
            showingPKListDialog = null
        }

        override fun onReject(pkInvitation: PKInvitation) {
            "主播 ${pkInvitation.receiver.nick} 拒绝".asToast()

        }
    }


    override fun getLayoutId(): Int {
        return R.layout.kit_start_pk_view
    }

    override fun initView() {
        super.initView()
       view!!. flPkBtn.setOnClickListener {
            showingPKListDialog = PKAbleListDialog()
            showingPKListDialog?.setInviteCall {
                showInvite(it)
            }
            showingPKListDialog?.setDefaultListener(object :
                FinalDialogFragment.BaseDialogListener() {
                override fun onDismiss(dialog: DialogFragment) {
                    super.onDismiss(dialog)
                    showingPKListDialog = null
                }
            })
            showingPKListDialog?.show(kitContext!!.fm, "")
        }
    }

    private fun showInvite(room: QNLiveRoomInfo) {
        client!!.getService(QNPKService::class.java)
            .pkInvitationHandler
            .applyJoin(60 * 1000, room.liveId, room.anchorInfo.userId, null,
                object : QNLiveCallBack<PKInvitation> {
                    override fun onError(code: Int, msg: String?) {
                        "邀请失败${msg}".asToast()
                    }

                    override fun onSuccess(data: PKInvitation) {
                        "等待对方接受".asToast()
                        LoadingDialog.showLoading(kitContext!!.fm)
                    }
                })
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        super.onStateChanged(source, event)
        if (event == Lifecycle.Event.ON_DESTROY) {
            client?.getService(QNPKService::class.java)?.removePKServiceListener(mPKServiceListener)
            client?.getService(QNPKService::class.java)?.pkInvitationHandler?.removePKInvitationListener(
                mPKInvitationListener
            )
        }
    }

    override fun attach(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient
    ) {
        super.attach(lifecycleOwner, context, client)
        client.getService(QNPKService::class.java).addPKServiceListener(mPKServiceListener)
        client.getService(QNPKService::class.java).pkInvitationHandler.addPKInvitationListener(
            mPKInvitationListener
        )
    }

}