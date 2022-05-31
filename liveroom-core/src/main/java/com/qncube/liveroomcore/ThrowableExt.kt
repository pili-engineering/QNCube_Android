package com.qncube.liveroomcore

import com.niucube.rtm.RtmException
import com.qncube.rtcexcepion.RtcException

fun Throwable.getCode(): Int {
    if (this is RtmException) {
        return code
    }
    if (this is RtcException) {
        return code
    }
    return -1
}