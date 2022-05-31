package com.qncube.kitlivepre

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.qncube.liveroomcore.*
import com.qncube.liveroomcore.mode.QNCreateRoomParam
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.pushclient.QNLivePushClient
import com.qncube.rtcexcepion.RtcException
import com.qncube.uikitcore.BaseSlotView
import com.qncube.uikitcore.KitContext
import com.qncube.uikitcore.QNInternalViewSlot
import com.qncube.uikitcore.dialog.LoadingDialog
import com.qncube.uikitcore.ext.bg
import kotlinx.android.synthetic.main.kit_live_preview.view.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * 开播预览槽位
 */
class LivePreViewSlot : QNInternalViewSlot() {

    /**
     * 开播点击回调 sdk
     * @param  QNCreateRoomParam 返回给sdk 开播参数
     */


    override fun createViewInternal(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient,
        container: ViewGroup?
    ): View {
        val view = LivePreView()

        view.attach(lifecycleOwner, context, client)
        return view.createView(LayoutInflater.from(context.androidContext), container)
    }
}

class LivePreView : BaseSlotView() {


    override fun getLayoutId(): Int {
        return R.layout.kit_live_preview
    }

    suspend fun createSuspend(p: QNCreateRoomParam) = suspendCoroutine<QNLiveRoomInfo> { ct ->
        QNLiveRoomEngine.createRoom(p, object : QNLiveCallBack<QNLiveRoomInfo> {
            override fun onError(code: Int, msg: String) {
                ct.resumeWithException(RtcException(code, msg))
            }

            override fun onSuccess(data: QNLiveRoomInfo) {
                ct.resume(data)
            }

        })
    }

    private suspend fun suspendJoinRoom(roomId: String) = suspendCoroutine<QNLiveRoomInfo> { cont ->
        client!!.joinRoom(roomId, object : QNLiveCallBack<QNLiveRoomInfo> {
            override fun onError(code: Int, msg: String?) {
                cont.resumeWithException(RtcException(code, msg ?: ""))
            }

            override fun onSuccess(data: QNLiveRoomInfo) {
                cont.resume(data)
            }
        })
    }

    private fun create(p: QNCreateRoomParam) {
        lifecycleOwner?.bg {
            LoadingDialog.showLoading(kitContext!!.fm)
            doWork {
                val info = createSuspend(p)
                suspendJoinRoom(info.liveId)
            }
            catchError {
                it.message?.asToast()
            }

            onFinally {
                LoadingDialog.cancelLoadingDialog()
            }
        }
    }

    override fun initView() {
        super.initView()
        view!!.ivClose.setOnClickListener {
            kitContext?.currentActivity?.finish()
        }
        view!!.tvStart.setOnClickListener {
            val titleStr = view!!.etTitle.text.toString()
            if (titleStr.isEmpty()) {
                context!!.resources.toast(R.string.preview_tip_input_title)
                return@setOnClickListener
            }
            val noticeStr = view!!.etNotice.text.toString() ?: ""
            create(QNCreateRoomParam().apply {
                title = titleStr
                notice = noticeStr
                cover_url = QNLiveRoomEngine.getCurrentUserInfo()?.avatar
            })
        }

        view!!.llBeauty.setOnClickListener {

        }

        view!!.llSwitch.setOnClickListener {
            (client as QNLivePushClient)
                .switchCamera()
        }

    }
}