package com.qiniu.bzuicomp.gift

import android.view.Gravity
import com.hapi.happy_dialog.FinalDialogFragment
import com.niucube.comproom.RoomManager
import com.niucube.rtm.RtmCallBack
import com.niucube.rtm.RtmManager
import com.niucube.rtm.msg.RtmTextMsg
import kotlinx.android.synthetic.main.dialog_gift.*

class GiftPanDialog : FinalDialogFragment() {

    init {
        applyGravityStyle(Gravity.BOTTOM)
        applyDimAmount(0f)
    }

    override fun getViewLayoutId(): Int {
        return R.layout.dialog_gift
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

    override fun init() {
        mGiftPanel.setGiftSendListener {
            senderGiftMsg(it)
            // dismiss()
        }
    }
}