package com.qncube.uikitcore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.qncube.liveroomcore.QNLiveRoomClient
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.liveroomcore.mode.QNLiveUser

abstract class BaseSlotView : ISlotView {

    override var client: QNLiveRoomClient? = null
    override var roomInfo: QNLiveRoomInfo? = null
    override var user: QNLiveUser? = null
    override var lifecycleOwner: LifecycleOwner? = null
    override var kitContext: KitContext? = null


    var view: View? = null
    fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        view = inflater.inflate(getLayoutId(), container, false)
        initView()
        return view!!
    }

    protected open fun initView() {

    }

    protected abstract fun getLayoutId(): Int
}
