package com.niucube.module.ktv.mode

import com.niucube.comproom.RoomManager
import com.niucube.module.ktv.IMusic
import com.niucube.module.ktv.TagDownLoadStatus
import com.qiniudemo.baseapp.been.asBaseRoomEntity
import com.qiniudemo.baseapp.been.isRoomHost

class Song : IMusic {

    var songId = ""
    var name = ""
    var album = ""
    var image = ""
    var author = ""
    var kind = ""
    var originUrl = ""
    var accompanimentUrl = ""
    var status = ""
    var lyrics=""
    var demander=""

    override var lrcDownLoadStatus: TagDownLoadStatus = TagDownLoadStatus()
    override var accompanyDownLoadStatus: TagDownLoadStatus = TagDownLoadStatus()
    override var originVoiceDownLoadStatus: TagDownLoadStatus = TagDownLoadStatus()

    override fun getMusicId(): String {
        return songId
    }

    override fun getMusicName(): String {
        return name
    }

    override fun getMusicAccompanyDownUrl(): String {
        return accompanimentUrl
    }

    override fun getMusicLrcDownUrl(): String {
        return lyrics
    }

    override fun getMusicOriginVoiceDownUrl(): String {
        return originUrl
    }

    override fun getIsNeedDownloadVoice(): Boolean {
        return RoomManager.mCurrentRoom?.asBaseRoomEntity()?.isRoomHost()==true
    }
}