package com.qncube.uikitlinkmic

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.niucube.qnrtcsdk.RoundTextureView
import com.qiniu.droid.rtc.QNConnectionState
import com.qiniu.droid.rtc.QNTextureView
import com.qncube.linkmicservice.QNAnchorHostMicLinker
import com.qncube.linkmicservice.QNAudienceMicLinker
import com.qncube.linkmicservice.QNLinkMicService
import com.qncube.linkmicservice.QNMicLinker
import com.qncube.liveroomcore.*
import com.qncube.uikitcore.*
import com.qncube.uikitcore.ext.ViewUtil
import kotlinx.android.synthetic.main.kit_view_linkers.view.*

/**
 * 连麦槽位
 */
class LinkerSlot : QNInternalViewSlot() {

    /**
     *设置每个麦位item自定义布局 没有设置则默认
     */
    var mViewAdapterSlot: QNViewAdapterSlot<QNMicLinker>? = null

    /**
     * 内置槽位 设置 点击事件回调
     */
    var mClickCallback: QNViewClickSlot<QNMicLinker>? = null

    override fun createViewInternal(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient,
        container: ViewGroup?
    ): View {
        val view = MicLinkerView()
        mViewAdapterSlot?.let {
            view.mViewAdapterSlot = it
        }
        mClickCallback?.let {
            view.mClickCallback = it
        }
        view.attach(lifecycleOwner, context, client)
        return view.createView(LayoutInflater.from(context.androidContext), container)
    }
}

class MicLinkerView : BaseSlotView() {

    private val linkService get() = client!!.getService(QNLinkMicService::class.java)!!

    var mViewAdapterSlot: QNViewAdapterSlot<QNMicLinker>? = null
    var mClickCallback: QNViewClickSlot<QNMicLinker>? = null

    private var mLinkerAdapter: BaseQuickAdapter<QNMicLinker, BaseViewHolder> =
        LinkerAdapter().apply {
            setOnItemChildClickListener { _, view, position ->
                mClickCallback?.createItemClick(kitContext!!, client!!, data[position])
                    ?.onClick(view)
            }
        }

    private val mMicLinkerListener = object : QNLinkMicService.MicLinkerListener {

        override fun onInitLinkers(linkers: MutableList<QNMicLinker>) {
            val lcs = linkers.filter {
                it.user.userId != roomInfo?.anchorInfo?.userId
            }
            mLinkerAdapter.setNewData(linkers)
        }

        override fun onUserJoinLink(micLinker: QNMicLinker) {
            Log.d("LinkerSlot", " onUserJoinLink 有人上麦 ${micLinker.user.nick}")
            mLinkerAdapter.addData(micLinker)
            if (isMyOnMic()) {
                addLinkerPreview(micLinker)
            }
        }

        override fun onUserLeft(micLinker: QNMicLinker) {
            Log.d("LinkerSlot", " onUserLeft 有人下麦 ${micLinker.user.nick}")
            removePreview(micLinker)
            mLinkerAdapter.remove(mLinkerAdapter.data.indexOf(micLinker))
        }

        override fun onUserMicrophoneStatusChange(micLinker: QNMicLinker) {
            val index = mLinkerAdapter.data.indexOf(micLinker)
            mLinkerAdapter.notifyItemChanged(index)
        }

        override fun onUserCameraStatusChange(micLinker: QNMicLinker) {
            val index = mLinkerAdapter.data.indexOf(micLinker)
            mLinkerAdapter.notifyItemChanged(index)
        }

        override fun onUserBeKick(micLinker: QNMicLinker, msg: String) {
            if (micLinker.user.userId == user?.userId) {
                linkService.audienceMicLinker.stopLink(object : QNLiveCallBack<Void> {
                    override fun onError(code: Int, msg: String?) {
                        msg?.asToast()
                    }

                    override fun onSuccess(data: Void?) {}
                })
            }
        }

        override fun onUserExtension(micLinker: QNMicLinker, extension: Extension) {

        }
    }

    private val mQNAudienceMicLinker = object : QNAudienceMicLinker.LinkMicListener {
        /**
         * 连麦模式连接状态
         * 连接成功后 连麦器会主动禁用推流器 改用rtc
         * @param state
         */
        override fun onConnectionStateChanged(state: QNConnectionState) {

        }

        /**
         * 本地角色变化
         */
        override fun lonLocalRoleChange(isLinker: Boolean) {
            Log.d("LinkerSlot", " lonLocalRoleChange 本地角色变化 ${isLinker}")
            if (isLinker) {
                client?.getService(QNLinkMicService::class.java)?.allLinker?.forEach {
                    Log.d("LinkerSlot", "  添加窗口 ${it.user.nick}")
                    addLinkerPreview(it)
                }
            } else {
                client?.getService(QNLinkMicService::class.java)?.allLinker?.forEach {
                    Log.d("LinkerSlot", "  移除窗口 ${it.user.nick}")
                    removePreview(it)
                }
            }
        }
    }

    private val mMixStreamAdapter =
        QNAnchorHostMicLinker.MixStreamAdapter { micLinkers, target, isJoin ->
            LinkerUIHelper.getLinkers(micLinkers, roomInfo!!)
        }

    override fun getLayoutId(): Int {
        return R.layout.kit_view_linkers
    }

    override fun initView() {
        super.initView()
        view!!.recyLinker.layoutManager = LinearLayoutManager(context)
        view!!.flLinkContent.post {
            init()
        }
    }

    private fun isMyOnMic(): Boolean {
        if (client?.clientType == ClientType.CLIENT_PUSH) {
            return true
        }
        linkService.allLinker.forEach {
            if (it.user.userId == user?.userId) {
                return true
            }
        }
        return false
    }

    private fun addLinkerPreview(micLinker: QNMicLinker) {
        if (micLinker.user.userId != roomInfo?.anchorInfo?.userId) {
            view!!.recyLinker.post {
                val index = mLinkerAdapter.data.indexOf(micLinker)
                val container = (mLinkerAdapter.getViewByPosition(
                    index,
                    R.id.flSurfaceContainer
                ) as ViewGroup?) ?: return@post

                val size = Math.min(LinkerUIHelper.uiMicWidth, LinkerUIHelper.uiMicHeight)
                container.addView(
                    RoundTextureView(context).apply {
                        linkService.setUserPreview(micLinker.user?.userId ?: "", this)
                        setRadius(ViewUtil.dip2px(size / 2f).toFloat())
                    },
                    FrameLayout.LayoutParams(
                        size,
                        size,
                        Gravity.CENTER
                    )
                )
            }
        } else {
            Log.d("LinkerSlot", "  添加窗口房主")
            view!!.flAnchorSurfaceCotiner.visibility = View.VISIBLE
            view!!.flAnchorSurfaceCotiner.addView(
                QNTextureView(context).apply {
                    linkService.setUserPreview(micLinker.user?.userId ?: "", this)
                },
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }
    }

    private fun removePreview(micLinker: QNMicLinker) {
        if (micLinker.user.userId != roomInfo?.anchorInfo?.userId) {
            val index = mLinkerAdapter.data.indexOf(micLinker)
            val container = (mLinkerAdapter.getViewByPosition(
                index,
                R.id.flSurfaceContainer
            ) as ViewGroup?)
            container?.removeAllViews()
        } else {
            Log.d("LinkerSlot", "  移除窗口房主")
            view!!.flAnchorSurfaceCotiner.removeAllViews()
            view!!.flAnchorSurfaceCotiner.visibility = View.INVISIBLE
        }
    }

    private fun init() {
        LinkerUIHelper.attachUIWidth(view!!.flLinkContent.width, view!!.flLinkContent.height)
        val rcLp: FrameLayout.LayoutParams =
            view!!.recyLinker.layoutParams as FrameLayout.LayoutParams
        rcLp.topMargin = LinkerUIHelper.uiTopMargin
        mLinkerAdapter.bindToRecyclerView(view!!.recyLinker)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        super.onStateChanged(source, event)
        if (event == Lifecycle.Event.ON_DESTROY) {
            if (client?.clientType == ClientType.CLIENT_PUSH) {
                client?.getService(QNLinkMicService::class.java)?.anchorHostMicLinker?.setMixStreamAdapter(
                    null
                )
            } else {
                client?.getService(QNLinkMicService::class.java)?.audienceMicLinker?.removeLinkMicListener(
                    mQNAudienceMicLinker
                )
            }
            client?.getService(QNLinkMicService::class.java)
                ?.removeMicLinkerListener(mMicLinkerListener)
        }
    }

    override fun attach(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient
    ) {
        super.attach(lifecycleOwner, context, client)
        mViewAdapterSlot?.let {
            mLinkerAdapter = it.createAdapter(lifecycleOwner, context, client)
        }
        if (client.clientType == ClientType.CLIENT_PUSH) {
            client.getService(QNLinkMicService::class.java).anchorHostMicLinker.setMixStreamAdapter(
                mMixStreamAdapter
            )
        } else {
            client.getService(QNLinkMicService::class.java).audienceMicLinker.addLinkMicListener(
                mQNAudienceMicLinker
            )
        }
        client.getService(QNLinkMicService::class.java).addMicLinkerListener(mMicLinkerListener)
    }

}