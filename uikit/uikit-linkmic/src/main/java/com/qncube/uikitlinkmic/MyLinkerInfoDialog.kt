package com.qncube.uikitlinkmic

import android.content.DialogInterface
import android.view.Gravity
import android.view.View
import com.bumptech.glide.Glide
import com.qncube.linkmicservice.QNLinkMicService
import com.qncube.linkmicservice.QNMicLinker
import com.qncube.liveroomcore.*
import com.qncube.liveroomcore.mode.QNLiveUser
import com.qncube.uikitcore.dialog.FinalDialogFragment
import com.qncube.uikitcore.dialog.LoadingDialog
import com.qncube.liveroomcore.Scheduler
import com.qncube.uikitcore.ext.setDoubleCheckClickListener
import kotlinx.android.synthetic.main.kit_dialog_my_linker_info.*
import java.text.DecimalFormat

class MyLinkerInfoDialog(val service: QNLinkMicService, val me: QNLiveUser) :
    FinalDialogFragment() {

    init {
        applyGravityStyle(Gravity.BOTTOM)
    }

    private var timeDiff = 0
    private val mScheduler = Scheduler(1000) {
        tvTime?.text = formatTime(timeDiff)
    }

    private fun formatTime(time: Int): String {
        val decimalFormat = DecimalFormat("00")
        val hh: String = decimalFormat.format(time / 3600)
        val mm: String = decimalFormat.format(time % 3600 / 60)
        val ss: String = decimalFormat.format(time % 60)
        return if (hh == "00") {
            "$mm:$ss"
        } else {
            "$hh:$mm:$ss"
        }
    }

    override fun getViewLayoutId(): Int {
        return R.layout.kit_dialog_my_linker_info
    }

    override fun init() {
        val isVideo = StartLinkStore.isVideoLink
        timeDiff = ((System.currentTimeMillis() - StartLinkStore.startTime) / 1000).toInt()
        mScheduler.start()
        if (isVideo) {
            tvTile.text = "视频连麦"
            ivCameraStatus.visibility = View.VISIBLE
        } else {
            tvTile.text = "语音连麦"
            ivCameraStatus.visibility = View.INVISIBLE
        }
        refreshInfo()
        ivCameraStatus.setDoubleCheckClickListener {
            service.audienceMicLinker.muteLocalCamera(!ivCameraStatus.isSelected,
                object : QNLiveCallBack<Void> {
                    override fun onError(code: Int, msg: String?) {
                        msg?.asToast()
                    }

                    override fun onSuccess(data: Void?) {
                        refreshInfo()
                    }
                })
        }
        ivMicStatus.setDoubleCheckClickListener {
            service.audienceMicLinker.muteLocalMicrophone(!ivMicStatus.isSelected,
                object : QNLiveCallBack<Void> {
                    override fun onError(code: Int, msg: String?) {
                        msg?.asToast()
                    }

                    override fun onSuccess(data: Void?) {
                        refreshInfo()
                    }
                })
        }

        Glide.with(requireContext())
            .load(me.avatar)
            .into(ivAvatar)
        tvHangup.setDoubleCheckClickListener {
            LoadingDialog.showLoading(childFragmentManager)
            service.audienceMicLinker.stopLink(object : QNLiveCallBack<Void> {
                override fun onError(code: Int, msg: String?) {
                    msg?.asToast()
                    LoadingDialog.cancelLoadingDialog()
                }

                override fun onSuccess(data: Void?) {
                    LoadingDialog.cancelLoadingDialog()
                    dismiss()
                }
            })
        }

    }

    private fun refreshInfo() {
        var myMic: QNMicLinker? = null
        service.allLinker.forEach {
            if (it.user.userId == me.userId) {
                myMic = it
                return@forEach
            }
        }
        myMic ?: return
        ivCameraStatus.isSelected = !myMic!!.isOpenCamera
        ivMicStatus.isSelected = !myMic!!.isOpenMicrophone

    }

    override fun onDismiss(dialog: DialogInterface) {
        mScheduler.cancel()
        super.onDismiss(dialog)

    }
}