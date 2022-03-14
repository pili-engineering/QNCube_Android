package com.niucube.qnrtcsdk

import com.qiniu.droid.rtc.QNClientEventListener
import com.qiniu.droid.rtc.QNLocalTrack
import com.qiniu.droid.rtc.QNLocalVideoTrack
import com.qiniu.droid.rtc.QNRemoteTrack

interface ExtQNClientEventListener: QNClientEventListener {
     fun onLocalPublished(var1: String, var2: List<QNLocalTrack>)
     fun onLocalUnpublished(var1: String, var2: List<QNLocalTrack>)
}