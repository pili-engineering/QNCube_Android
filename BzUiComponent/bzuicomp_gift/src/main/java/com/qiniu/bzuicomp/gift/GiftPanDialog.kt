package com.qiniu.bzuicomp.gift

import android.view.Gravity
import com.hapi.baseframe.dialog.BaseVmBindingDialogFragment
import com.hapi.baseframe.dialog.FinalDialogFragment
import com.niucube.comproom.RoomManager
import com.niucube.rtm.RtmCallBack
import com.niucube.rtm.RtmManager
import com.niucube.rtm.msg.RtmTextMsg
import com.qiniu.bzuicomp.gift.databinding.DialogGiftBinding

class GiftPanDialog : BaseVmBindingDialogFragment<DialogGiftBinding>() {

    init {
        applyGravityStyle(Gravity.BOTTOM)
        applyDimAmount(0f)
    }

    private fun senderGiftMsg(model: GiftMsg) {
        RtmManager.rtmClient.sendChannelMsg(RtmTextMsg<GiftMsg>(
            GiftMsg.action_gift, (
                    model)
        ).toJsonString(), RoomManager.mCurrentRoom?.provideImGroupId() ?: "", true, object :
            RtmCallBack {
            override fun onSuccess() {}
            override fun onFailure(code: Int, msg: String) {}
        })
    }

    override fun showLoading(toShow: Boolean) {
    }

    override fun initViewData() {
        binding. mGiftPanel.setGiftSendListener {
            senderGiftMsg(it)
            // dismiss()
        }
    }
}