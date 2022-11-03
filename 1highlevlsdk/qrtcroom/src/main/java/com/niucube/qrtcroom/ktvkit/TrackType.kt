package com.niucube.qrtcroom.ktvkit

enum class TrackType(val value:String) {
    //伴奏
    accompany("accompany"),
    //原声
    originVoice("originVoice")
}

fun String.toTrackType():TrackType{
    if(this==TrackType.accompany.value){
        return TrackType.accompany
    }
    return TrackType.originVoice
}