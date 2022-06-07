package com.qncube.uikitlinkmic

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.niucube.qnrtcsdk.RoundTextureView
import com.qncube.linkmicservice.QNLinkMicService
import com.qncube.linkmicservice.QNMicLinker
import kotlinx.android.synthetic.main.kit_item_linker_surface.view.*

class MicSeatView : LinearLayout {
    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {

    }

    fun addItemView(micLinker: QNMicLinker, linkService: QNLinkMicService) {
        val itemView: View =
            LayoutInflater.from(context).inflate(R.layout.kit_item_linker_surface, this, false)

        val tempLp: LinearLayout.LayoutParams =
            itemView.tempView.layoutParams as LinearLayout.LayoutParams
        tempLp.height = LinkerUIHelper.micBottomUIMargin
        itemView.tempView.layoutParams = tempLp

        val sfLp = itemView.flSurfaceContainer.layoutParams
        sfLp.width = LinkerUIHelper.uiMicWidth
        sfLp.height = LinkerUIHelper.uiMicHeight
        itemView.flSurfaceContainer.layoutParams = sfLp

        val llLp =
            itemView.flSurfaceContainer.layoutParams as LinearLayout.LayoutParams
        llLp.marginEnd = LinkerUIHelper.micRightUIMargin
        itemView.flSurfaceContainer.layoutParams = llLp


        addView(itemView)

        val container = itemView.flSurfaceContainer as FrameLayout
        val size = Math.min(LinkerUIHelper.uiMicWidth, LinkerUIHelper.uiMicHeight)
        container.addView(
            RoundTextureView(context).apply {
                linkService.setUserPreview(micLinker.user?.userId ?: "", this)
                setRadius((size/2).toFloat())
            },
            FrameLayout.LayoutParams(
                size,
                size,
                Gravity.CENTER
            )
        )
    }

    fun removeItem(index: Int) {
        if (index > childCount - 1) {
            return
        }
        removeViewAt(index)
    }

    fun convertItem(index: Int, item: QNMicLinker) {
        if (index > childCount - 1) {
            return
        }
        val container = getChildAt(index).flSurfaceContainer
        if (item.isOpenCamera) {
            container.visibility = View.VISIBLE
        } else {
            container.visibility = View.INVISIBLE
        }
    }
}