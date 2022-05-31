package com.qncube.uikitlinkmic

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.qbcube.pkservice.PKInvitation
import com.qbcube.pkservice.QNPKInvitationHandler
import com.qbcube.pkservice.QNPKService
import com.qncube.liveroomcore.*
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.liveroomcore.mode.QNLiveUser
import com.qncube.uikitcore.ISlotView
import com.qncube.uikitcore.KitContext
import com.qncube.uikitcore.QNInternalViewSlot
import com.qncube.uikitcore.dialog.CommonTipDialog
import com.qncube.uikitcore.dialog.FinalDialogFragment

class AnchorReceivedPKApplySlot : QNInternalViewSlot() {
    /**
     * 没有设置代理 时候 使用的默认创建ui
     *
     * @param client
     * @param container
     * @return
     */
    override fun createViewInternal(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient,
        container: ViewGroup?
    ): View? {
        val handler = AnchorReceivedPkHandler()
        handler.attach(lifecycleOwner, context, client)
        return null
    }


}

class AnchorReceivedPkHandler : ISlotView {
    override var client: QNLiveRoomClient? = null
    override var roomInfo: QNLiveRoomInfo? = null
    override var user: QNLiveUser? = null
    override var lifecycleOwner: LifecycleOwner? = null
    override var kitContext: KitContext? = null

    private val mPKInvitationListener = object : QNPKInvitationHandler.PKInvitationListener {
        override fun onReceivedApply(pkInvitation: PKInvitation) {
            CommonTipDialog.TipBuild()
                .setTittle(" ${pkInvitation.receiver.nick} 邀请你PK，是否接受")
                .setContent("")
                .setNegativeText("拒绝")
                .setPositiveText("接受")
                .setListener(object : FinalDialogFragment.BaseDialogListener() {
                    override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                        super.onDialogPositiveClick(dialog, any)
                        client!!.getService(QNPKService::class.java)
                            .pkInvitationHandler.accept(pkInvitation.invitationId, null,
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
                        client!!.getService(QNPKService::class.java)
                            .pkInvitationHandler.reject(pkInvitation.invitationId, null,
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

        override fun onApplyCanceled(pkInvitation: PKInvitation) {}

        override fun onApplyTimeOut(pkInvitation: PKInvitation) {

        }

        override fun onAccept(pkInvitation: PKInvitation) {

        }

        override fun onReject(pkInvitation: PKInvitation) {
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        super.onStateChanged(source, event)
        if (event == Lifecycle.Event.ON_DESTROY) {

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

        client.getService(QNPKService::class.java).pkInvitationHandler.addPKInvitationListener(
            mPKInvitationListener
        )
    }

}