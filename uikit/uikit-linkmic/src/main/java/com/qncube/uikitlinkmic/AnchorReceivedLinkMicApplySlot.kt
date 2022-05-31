package com.qncube.uikitlinkmic

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.qncube.linkmicservice.LinkInvitation
import com.qncube.linkmicservice.QNLinkMicInvitationHandler
import com.qncube.linkmicservice.QNLinkMicService
import com.qncube.liveroomcore.*
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.liveroomcore.mode.QNLiveUser
import com.qncube.uikitcore.ISlotView
import com.qncube.uikitcore.KitContext
import com.qncube.uikitcore.QNInternalViewSlot
import com.qncube.uikitcore.dialog.CommonTipDialog
import com.qncube.uikitcore.dialog.FinalDialogFragment

/**
 * 主播收到连麦邀请弹窗
 */
class AnchorReceivedLinkMicApplySlot : QNInternalViewSlot() {

    override fun createViewInternal(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient,
        container: ViewGroup?
    ): View? {
        val handler = AnchorLinkerHandler()
        handler.attach(lifecycleOwner,context, client)
        return null
    }

}

class AnchorLinkerHandler : ISlotView{

    override var client: QNLiveRoomClient? =null
    override var roomInfo: QNLiveRoomInfo?=null
    override var user: QNLiveUser?=null
    override var lifecycleOwner: LifecycleOwner?=null
    override var kitContext: KitContext?=null

    private val mInvitationListener = object : QNLinkMicInvitationHandler.InvitationListener {

        override fun onReceivedApply(linkInvitation: LinkInvitation) {
            CommonTipDialog.TipBuild()
                .setTittle(" ${linkInvitation.initiator.nick} 申请连麦是否同意，是否接受")
                .setContent("")
                .setNegativeText("拒绝")
                .setPositiveText("接受")
                .setListener(object : FinalDialogFragment.BaseDialogListener() {
                    override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                        super.onDialogPositiveClick(dialog, any)
                        client!!.getService(QNLinkMicService::class.java)
                            .linkMicInvitationHandler.accept(linkInvitation.invitationId, null,
                                object : QNLiveCallBack<Void> {
                                    override fun onError(code: Int, msg: String?) {
                                        msg?.asToast()
                                    }

                                    override fun onSuccess(data: Void?) {
                                    }
                                })
                    }

                    override fun onDialogNegativeClick(dialog: DialogFragment, any: Any) {
                        super.onDialogNegativeClick(dialog, any)
                        client!!.getService(QNLinkMicService::class.java)
                            .linkMicInvitationHandler.reject(linkInvitation.invitationId, null,
                                object : QNLiveCallBack<Void> {
                                    override fun onError(code: Int, msg: String?) {
                                        // msg?.asToast()
                                    }

                                    override fun onSuccess(data: Void?) {
                                    }
                                })
                    }
                }
                ).build()
                .show(kitContext!!.fm, "")
        }

        override fun onApplyCanceled(linkInvitation: LinkInvitation) {

        }

        override fun onApplyTimeOut(linkInvitation: LinkInvitation) {

        }

        override fun onAccept(linkInvitation: LinkInvitation) {

        }

        override fun onReject(linkInvitation: LinkInvitation) {
        }
    }


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        super.onStateChanged(source, event)
        if (event == Lifecycle.Event.ON_DESTROY) {
            client?.getService(QNLinkMicService::class.java)
                ?.linkMicInvitationHandler?.addInvitationLister(mInvitationListener)
        }
    }
    override fun attach(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient
    ) {
        this.client = client
        this.kitContext = context
        client.getService(QNLinkMicService::class.java)
            .linkMicInvitationHandler.addInvitationLister(mInvitationListener)
    }


}