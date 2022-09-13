package com.niucube.qrtcroom.rtc

import com.qiniu.droid.rtc.QNClientEventListener
import com.qiniu.droid.rtc.QNLocalTrack

interface ExtQNClientEventListener: QNClientEventListener {
     fun onLocalPublished(var1: String, var2: List<QNLocalTrack>)
     fun onLocalUnpublished(var1: String, var2: List<QNLocalTrack>)
}