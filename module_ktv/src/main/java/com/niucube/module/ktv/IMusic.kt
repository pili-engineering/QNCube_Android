package com.niucube.module.ktv

interface IMusic {

    var lrcDownLoadStatus: TagDownLoadStatus
    var accompanyDownLoadStatus: TagDownLoadStatus
    var originVoiceDownLoadStatus: TagDownLoadStatus

    fun getMusicId(): String
    fun getMusicName(): String
    fun getMusicAccompanyDownUrl(): String
    fun getMusicLrcDownUrl(): String
    fun getMusicOriginVoiceDownUrl(): String
    fun getIsNeedDownloadVoice():Boolean

    fun getTagDownLoadStatus(tag: String): TagDownLoadStatus {
        return when (tag) {
            TagDownLoadStatus.TagAccompany -> this.accompanyDownLoadStatus
            TagDownLoadStatus.TagLrc -> this.lrcDownLoadStatus
            TagDownLoadStatus.TagOriginVoice -> this.originVoiceDownLoadStatus
            else -> this.accompanyDownLoadStatus
        }
    }

    fun getTagDownLoadUrl(tag: String): String {
        return when (tag) {
            TagDownLoadStatus.TagAccompany -> getMusicAccompanyDownUrl()
            TagDownLoadStatus.TagLrc -> getMusicLrcDownUrl()
            TagDownLoadStatus.TagOriginVoice -> getMusicOriginVoiceDownUrl()
            else -> getMusicOriginVoiceDownUrl()
        }
    }

    fun getTagDownLoadLocalUrl(tag: String): String {
        return when (tag) {
            TagDownLoadStatus.TagAccompany -> this.accompanyDownLoadStatus.localFile
            TagDownLoadStatus.TagLrc -> this.lrcDownLoadStatus.localFile
            TagDownLoadStatus.TagOriginVoice -> this.originVoiceDownLoadStatus.localFile
            else -> this.accompanyDownLoadStatus.localFile
        }
    }

}
public class TagDownLoadStatus {
    var downloadStatus = 0
    var progress: Long = 0
    var localFile = ""

    companion object {
        var download_Status_undefine = 0
        var download_Status_downloading = 1
        var download_Status_finish = 2
        var TagLrc = "lrc"
        var TagAccompany = "accompany"
        var TagOriginVoice = "originVoice"
    }

}