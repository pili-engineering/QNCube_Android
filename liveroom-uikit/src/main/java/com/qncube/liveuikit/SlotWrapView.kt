package com.qncube.liveuikit

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import com.qncube.liveroomcore.QNLiveRoomClient
import com.qncube.uikitcore.KitContext
import com.qncube.uikitcore.QNViewSlot

class SlotWrapView : FrameLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun attach(
        slot: QNViewSlot,
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient
    ) {
        slot.createView(lifecycleOwner, context, client, this)?.let {
            addView(it)
        }
    }
}