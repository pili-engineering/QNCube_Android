package com.qiniu.bzuicomp.bottominput

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import com.hapi.baseframe.dialog.BaseVmBindingDialogFragment
import com.qiniu.bzuicomp.bottominput.databinding.DialogRoomInputBinding

/**
 * 底部输入框
 */
class RoomInputDialog(val style: Int = 1) : BaseVmBindingDialogFragment<DialogRoomInputBinding>() {

    companion object {
        const val type_text = 1
        const val type_danmu = 2
    }

    init {
        applyGravityStyle(Gravity.BOTTOM)
        applyDimAmount(0f)
    }

    /**
     * 发消息拦截回调
     */
    var sendPubCall: ((msg: String) -> Unit)? = null

    override fun init() {
        val viewInput = when (style) {
            1 -> {
                binding.flInput.setBackgroundColor(Color.parseColor("#eeeeee"))
                LightInputView(requireContext())
            }
            2 -> {
                binding.flInput.setBackgroundColor(Color.parseColor("#000000"))
                DarkInputView(requireContext())
            }
            else -> {
                RoomInputView(requireContext())
            }
        }

        binding.flInput.addView(
            viewInput,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        viewInput.attachActivity(requireActivity())
        viewInput.sendPubCall = {
            sendPubCall?.invoke(it)
            dismiss()
        }
        viewInput.requestEditFocus()
    }

    override fun initViewData() {
        TODO("Not yet implemented")
    }

    override fun showLoading(toShow: Boolean) {
        TODO("Not yet implemented")
    }
}