package com.qncube.uikitlinkmic

import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.nucube.rtclive.QNCameraParams
import com.nucube.rtclive.QNMicrophoneParams
import com.qncube.linkmicservice.LinkInvitation
import com.qncube.linkmicservice.QNLinkMicInvitationHandler
import com.qncube.linkmicservice.QNLinkMicService
import com.qncube.liveroomcore.QNLiveCallBack
import com.qncube.liveroomcore.QNLiveRoomClient
import com.qncube.liveroomcore.asToast
import com.qncube.uikitcore.BaseSlotView
import com.qncube.uikitcore.KitContext
import com.qncube.uikitcore.dialog.FinalDialogFragment
import com.qncube.uikitcore.dialog.LoadingDialog

internal object StartLinkStore {
    var isInviting = false
    var isVideoLink = false
    var startTime = 0L
//    private val mScheduler = Scheduler(1000) {
//        total++
//    }
//    var timeCall:(time:)
}

class StartLinkView : BaseSlotView() {

    private val mInvitationListener = object : QNLinkMicInvitationHandler.InvitationListener {

        override fun onReceivedApply(linkInvitation: LinkInvitation) {}

        override fun onApplyCanceled(linkInvitation: LinkInvitation) {
            StartLinkStore.isInviting = false
        }

        override fun onApplyTimeOut(linkInvitation: LinkInvitation) {
            StartLinkStore.isInviting = false
            LoadingDialog.cancelLoadingDialog()
        }

        override fun onAccept(linkInvitation: LinkInvitation) {
            StartLinkStore.isInviting = false
            LoadingDialog.cancelLoadingDialog()
            "主播同意你的申请".asToast()
            client?.getService(QNLinkMicService::class.java)
                ?.audienceMicLinker
                ?.startLink(
                    null, if (StartLinkStore.isVideoLink) {
                        QNCameraParams()
                    } else {
                        null
                    }, QNMicrophoneParams(),
                    object : QNLiveCallBack<Void> {
                        override fun onError(code: Int, msg: String?) {
                            msg?.asToast()
                        }

                        override fun onSuccess(data: Void?) {
                            StartLinkStore.startTime = System.currentTimeMillis()
                        }
                    }
                )
        }

        override fun onReject(linkInvitation: LinkInvitation) {
            StartLinkStore.isInviting = false
            "主播拒绝你的连麦申请".asToast()
            LoadingDialog.cancelLoadingDialog()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.kit_view_start_link
    }

    override fun initView() {
        super.initView()
        view!!.setOnClickListener {
            if (roomInfo == null) {
                return@setOnClickListener
            }
            if (StartLinkStore.isInviting) {
                "正在申请中，请稍后".asToast()
                return@setOnClickListener
            }
            if (client?.getService(QNLinkMicService::class.java)?.audienceMicLinker?.isLinked() == true) {
                MyLinkerInfoDialog(client!!.getService(QNLinkMicService::class.java), user!!).show(
                    kitContext!!.fm,
                    ""
                )
                return@setOnClickListener
            }

            LinkApplyDialog().apply {
                mDefaultListener = object : FinalDialogFragment.BaseDialogListener() {
                    override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                        super.onDialogPositiveClick(dialog, any)
                        LoadingDialog.showLoading(kitContext!!.fm)
                        StartLinkStore.isVideoLink = any as Boolean
                        client!!.getService(QNLinkMicService::class.java).linkMicInvitationHandler.apply(
                            30 * 1000,
                            roomInfo!!.liveId,
                            roomInfo!!.anchorInfo.userId,
                            null,
                            object : QNLiveCallBack<LinkInvitation> {
                                override fun onError(code: Int, msg: String?) {
                                    msg?.asToast()
                                    LoadingDialog.cancelLoadingDialog()
                                }

                                override fun onSuccess(data: LinkInvitation) {
                                    StartLinkStore.isInviting = true
                                    "等待主播同意".asToast()
                                }
                            }
                        )
                    }
                }
            }.show(kitContext!!.fm, "")
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        super.onStateChanged(source, event)
        if (event == Lifecycle.Event.ON_DESTROY) {
            client?.getService(QNLinkMicService::class.java)?.linkMicInvitationHandler?.removeInvitationLister(
                mInvitationListener
            )
        }
    }

    override fun attach(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient
    ) {
        super.attach(lifecycleOwner, context, client)
        client.getService(QNLinkMicService::class.java).linkMicInvitationHandler.addInvitationLister(
            mInvitationListener
        )
    }
}