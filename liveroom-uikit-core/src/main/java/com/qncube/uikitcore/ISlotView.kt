package com.qncube.uikitcore

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.qncube.liveroomcore.QNLiveRoomClient
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.liveroomcore.mode.QNLiveUser
import com.qncube.liveroomcore.QNRoomLifeCycleListener

interface ISlotView : LifecycleEventObserver, QNRoomLifeCycleListener {

    var client: QNLiveRoomClient?
    var roomInfo: QNLiveRoomInfo?
    var user: QNLiveUser?
    var lifecycleOwner: LifecycleOwner?
    var kitContext: KitContext?

    val context: Context?
        get() = kitContext?.androidContext

    open fun attach(
        lifecycleOwner: LifecycleOwner,
        context: KitContext,
        client: QNLiveRoomClient,
    ) {
        this.client = client
        this.kitContext = context
        this.lifecycleOwner = lifecycleOwner
        client.addRoomLifeCycleListener(this)
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            client?.removeRoomLifeCycleListener(this)
        }
    }

    override fun onRoomJoined(roomInfo: QNLiveRoomInfo) {
        this.roomInfo = roomInfo
    }

    override fun onRoomEnter(roomId: String, user: QNLiveUser) {
        this.user = user
    }

    override fun onRoomLeave() {
    }

    override fun onRoomClose() {
    }

}