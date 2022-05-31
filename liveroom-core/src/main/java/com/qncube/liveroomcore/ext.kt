package com.qncube.liveroomcore

import android.content.res.Resources
import android.widget.Toast
import com.qncube.liveroomcore.mode.QNCreateRoomParam
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.rtcexcepion.RtcException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

fun String.asToast() {
    if (this.isEmpty()) {
        return
    }
    Toast.makeText(AppCache.appContext, this, Toast.LENGTH_SHORT).show()
}

fun Resources.toast(res: Int) {
    getString(res).asToast()
}

