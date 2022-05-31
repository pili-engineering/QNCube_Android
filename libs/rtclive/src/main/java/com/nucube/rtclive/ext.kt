package com.nucube.rtclive

import com.qiniu.droid.rtc.*


fun QNTrack.tryPlay( var1: QNRenderView){
    if(this is QNLocalVideoTrack){
        play(var1)
        return
    }
    if(this is QNRemoteVideoTrack){
        play(var1)
        return
    }
}