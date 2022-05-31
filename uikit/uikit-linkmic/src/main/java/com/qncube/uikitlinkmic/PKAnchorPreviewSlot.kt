package com.qncube.uikitlinkmic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.nucube.rtclive.CameraMergeOption
import com.nucube.rtclive.MicrophoneMergeOption
import com.nucube.rtclive.MixStreamParams
import com.nucube.rtclive.QNMergeOption
import com.qbcube.pkservice.QNPKService
import com.qbcube.pkservice.QNPKSession
import com.qiniu.droid.rtc.QNTextureView
import com.qncube.linkmicservice.QNLinkMicService
import com.qncube.liveroomcore.Extension
import com.qncube.liveroomcore.QNLiveRoomClient
import com.qncube.pushclient.QNLivePushClient
import com.qncube.uikitcore.BaseSlotView
import com.qncube.uikitcore.KitContext
import com.qncube.uikitcore.QNInternalViewSlot
import kotlinx.android.synthetic.main.kit_anchor_pk_preview.view.*

/**
 * pk主播
 */
class PKAnchorPreviewSlot : QNInternalViewSlot() {

    override fun createViewInternal(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient,
        container: ViewGroup?
    ): View {
        val view = PKAnchorPreview()
        view.attach(lifecycleOwner, context, client)
        return view.createView(LayoutInflater.from(context.androidContext), container)
    }
}

class PKAnchorPreview : BaseSlotView() {

    private val mPKMixStreamAdapter = object : QNPKService.PKMixStreamAdapter {

        override fun onPKLinkerJoin(pkSession: QNPKSession): MutableList<QNMergeOption> {
            val ops = ArrayList<QNMergeOption>()
            val peer = if (pkSession.initiator.userId == user?.userId) {
                pkSession.receiver
            } else {
                pkSession.initiator
            }
            ops.add(QNMergeOption().apply {
                uid = user!!.userId
                cameraMergeOption = CameraMergeOption().apply {
                    isNeed = true
                    mX = 0
                    mY = 0
                    mZ = 0
                    mWidth = PKUIHelper.mixWidth / 2
                    mHeight = PKUIHelper.mixHeight
                    // mStretchMode=QNRenderMode.
                }
                microphoneMergeOption = MicrophoneMergeOption().apply {
                    isNeed = true
                }
            })
            ops.add(QNMergeOption().apply {
                uid = peer.userId
                cameraMergeOption = CameraMergeOption().apply {
                    isNeed = true
                    mX = PKUIHelper.mixWidth / 2
                    mY = 0
                    mZ = 0
                    mWidth = PKUIHelper.mixWidth / 2
                    mHeight = PKUIHelper.mixHeight
                    // mStretchMode=QNRenderMode.
                }
                microphoneMergeOption = MicrophoneMergeOption().apply {
                    isNeed = true
                }
            })
            return ops
        }

        override fun onPKMixStreamStart(pkSession: QNPKSession): MixStreamParams {
            return MixStreamParams().apply {
                mixStreamWidth = PKUIHelper.mixWidth
                mixStringHeight = PKUIHelper.mixHeight
                mixBitrate = 1500 * 1000
                fps = 25
            }
        }

        override fun onPKLinkerLeft(): MutableList<QNMergeOption> {
            val ops = ArrayList<QNMergeOption>()
            client?.getService(QNLinkMicService::class.java)
                ?.allLinker?.let {
                    ops.addAll(LinkerUIHelper.getLinkers(it, roomInfo!!))
                }
            return ops
        }
    }

    private var originPreViewParent: ViewGroup? = null
    private var localRenderView: View? = null
    private val mPKServiceListener = object : QNPKService.PKServiceListener {

        override fun onInitPKer(pkSession: QNPKSession) {}

        override fun onStart(pkSession: QNPKSession) {
            val peer = if (pkSession.initiator.userId == user?.userId) {
                pkSession.receiver
            } else {
                pkSession.initiator
            }
            localRenderView = (client as QNLivePushClient).localPreView as View
            originPreViewParent = localRenderView!!.parent as ViewGroup
            originPreViewParent?.removeView(localRenderView)
            view!!.flMeContainer.addView(localRenderView)
            view!!.flPeerContainer.addView(
                QNTextureView(context).apply {
                    client?.getService(QNPKService::class.java)
                        ?.setPeerAnchorPreView(peer.userId, this)
                },
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

        override fun onStop(pkSession: QNPKSession, code: Int, msg: String) {
            view!!.flMeContainer.removeView(localRenderView)
            originPreViewParent?.addView(
                localRenderView, ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            view!!.flPeerContainer.removeAllViews()
        }

        override fun onWaitPeerTimeOut(pkSession: QNPKSession) {}
        override fun onPKExtensionUpdate(pkSession: QNPKSession, extension: Extension) {}
    }

    override fun getLayoutId(): Int {
        return R.layout.kit_anchor_pk_preview
    }

    override fun initView() {
        super.initView()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        super.onStateChanged(source, event)
        if (event == Lifecycle.Event.ON_DESTROY) {
            client?.getService(QNPKService::class.java)?.removePKServiceListener(mPKServiceListener)
            client?.getService(QNPKService::class.java)?.setPKMixStreamAdapter(null)
        }
    }

    override fun attach(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient
    ) {
        super.attach(lifecycleOwner, context, client)

        client.getService(QNPKService::class.java).addPKServiceListener(mPKServiceListener)
        client.getService(QNPKService::class.java).setPKMixStreamAdapter(mPKMixStreamAdapter)
    }


}
