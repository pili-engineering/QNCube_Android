package com.niucube.module.ktv.playerlist

import android.content.Context
import android.os.Environment
import android.util.Log
import com.downloader.*
import com.downloader.internal.DownloadRequestQueue
import com.downloader.request.DownloadRequest
import com.downloader.utils.Utils

import com.hapi.ut.AppCache
import java.io.File
import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import com.niucube.comproom.RoomManager
import com.niucube.module.ktv.IMusic
import com.niucube.module.ktv.KTVService
import com.niucube.module.ktv.TagDownLoadStatus
import com.niucube.module.ktv.mode.Song
import com.niucube.rtm.*
import com.niucube.rtm.msg.RtmTextMsg
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.jsonutil.JsonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception


class KTVPlaylistsManager(val context: Context) {

    companion object {

        init {
            val config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .build()
            PRDownloader.initialize(AppCache.getContext(), config);
        }
    }

    //队列头音乐改变回调
    var headMusicChangedCall: ((music: IMusic) -> Unit) = {}

    private var headerIMusic: IMusic? = null
    private val parentFile = if (android.os.Build.VERSION.SDK_INT > 29) {
        context.cacheDir.absolutePath
    } else {
        Environment.getExternalStorageDirectory().absolutePath
    }

    private val selected = ArrayList<IMusic>()

    fun getPlaylists(): List<IMusic> {
        return selected
    }

    /**
     * 刷新已经点的歌单
     */
    suspend fun fetchPlaylists(): List<IMusic> {

        val list = ArrayList<IMusic>()
        try {
            list.addAll(
                RetrofitManager.create(KTVService::class.java)
                    .selectedSongList(100, 1, RoomManager.mCurrentRoom?.provideRoomId() ?: "").list
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list.forEach {
            if (it.getIsNeedDownloadVoice()) {
                checkDownLoadMusic(
                    it,
                    TagDownLoadStatus.TagAccompany
                )
                checkDownLoadMusic(
                    it,
                    TagDownLoadStatus.TagOriginVoice
                )
            }

            checkDownLoadMusic(
                it,
                TagDownLoadStatus.TagLrc
            )
        }
        val headTemp = headerIMusic
        if (list.isEmpty()) {
            headerIMusic = null
        } else {
            headerIMusic = list[0]
        }

        if (headTemp?.getMusicId() ?: Math.random().toString() != headerIMusic?.getMusicId()) {
            GlobalScope.launch(Dispatchers.Main) {
                headerIMusic?.let {
                    headMusicChangedCall.invoke(headerIMusic!!)
                }
            }
        }
        selected.clear()
        selected.addAll(list)
        return list
    }

    private fun refreshPlayerList() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                fetchPlaylists()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val mRtmMsgListener = object : RtmMsgListener {
        override fun onNewMsg(msg: String, peerId: String): Boolean {
            when (msg.optAction()) {
                action_player_list_add -> {
                    val music =
                        JsonUtils.parseObject(msg.optData(), Song::class.java) ?: return true

                    if (music.getIsNeedDownloadVoice()) {
                        checkDownLoadMusic(
                            music,
                            TagDownLoadStatus.TagAccompany
                        )
                        checkDownLoadMusic(
                            music,
                            TagDownLoadStatus.TagOriginVoice
                        )
                    }
                    checkDownLoadMusic(
                        music,
                        TagDownLoadStatus.TagLrc
                    )
                    if (selected.size == 0) {
                        headerIMusic = music
                        headMusicChangedCall.invoke(music)
                    }
                    selected.add(music)
                    return true
                }
                action_player_list_remove -> {
                    val music =
                        JsonUtils.parseObject(msg.optData(), Song::class.java) ?: return true
                    selected.remove(music)
                    return true
                }
            }
            return false
        }
    }

    init {
        RtmManager.addRtmChannelListener(mRtmMsgListener)
        // RoomAttributesManager.addRoomAttributesListener(mRoomAttributesListener)
        RoomManager.addRoomLifecycleMonitor(object : RoomLifecycleMonitor {
            override fun onRoomEntering(roomEntity: RoomEntity) {
                refreshPlayerList()
            }

            override fun onRoomClosed(roomEntity: RoomEntity?) {
                RtmManager.addRtmChannelListener(mRtmMsgListener)
                super.onRoomClosed(roomEntity)
                //   RoomAttributesManager.removeRoomAttributesListener(mRoomAttributesListener)
            }
        })
    }

    var lastMusicEndCall: ((error: Boolean) -> Unit)? = null

    fun loadMusicFile(IMusic: IMusic, endCall: ((error: Boolean) -> Unit)) {
        lastMusicEndCall = null
        val cp = checkDownLoadMusic(
            IMusic,
            TagDownLoadStatus.TagAccompany
        )
        val ov = checkDownLoadMusic(
            IMusic,
            TagDownLoadStatus.TagOriginVoice
        )
        if (ov == null && cp == null) {
            endCall.invoke(false)
            return
        }

        lastMusicEndCall = endCall
        (ov?.downloadListener as InnerOkDownloadListener?)?.loadEndCall = {
            if(it){
                lastMusicEndCall?.invoke(true)
                lastMusicEndCall = null
            }else{
                val otherEnd = checkDownLoadMusic(
                    IMusic,
                    TagDownLoadStatus.TagAccompany
                ) == null
                if(otherEnd){
                    lastMusicEndCall?.invoke(false)
                    lastMusicEndCall = null
                }
            }
        }
        (cp?.downloadListener as InnerOkDownloadListener?)?.loadEndCall = {
            if(it){
                lastMusicEndCall?.invoke(true)
                lastMusicEndCall = null
            }else{

                val otherEnd =checkDownLoadMusic(
                    IMusic,
                    TagDownLoadStatus.TagOriginVoice
                ) == null

                if(otherEnd){
                    lastMusicEndCall?.invoke(false)
                    lastMusicEndCall = null
                }
            }
        }
    }

    var lastLrcEndCall: ((error: Boolean) -> Unit)? = null
    fun loadMusicLirc(IMusic: IMusic, endCall: ((error: Boolean) -> Unit)) {
        lastLrcEndCall = null
        val task = checkDownLoadMusic(
            IMusic,
            TagDownLoadStatus.TagLrc
        )
        if (task == null) {
            endCall.invoke(false)
        } else {
            lastLrcEndCall = endCall
            (task.downloadListener as InnerOkDownloadListener).loadEndCall = endCall
        }
    }

    private fun checkDownLoadMusic(IMusic: IMusic, taskTag: String): DownloadRequest? {
        val remoteUri = IMusic.getTagDownLoadUrl(taskTag)
        val fileDir = parentFile + "/${taskTag}"
        val fileName = IMusic.getMusicName() + IMusic.getMusicId()
        val file = File(fileDir)
        if (!file.exists()) {
            file.mkdir()
        }

        val localPath = fileDir + "/${fileName}"
        IMusic.getTagDownLoadStatus(taskTag).localFile = localPath

        if (PRDownloader.isDownloadComplete(remoteUri, fileDir, fileName)) {
            return null
        }

        val id = Utils.getUniqueId(remoteUri, fileDir, fileName)
        val lastTask = DownloadRequestQueue.getInstance().getDownloadRequest(id)
        if (lastTask != null) {
            if (lastTask.status == Status.FAILED) {
                lastTask.start(InnerOkDownloadListener(IMusic, taskTag))
            }
            return lastTask
        }
        val downloadRequest = PRDownloader.download(remoteUri, fileDir, fileName)
            .build()
        downloadRequest.setOnProgressListener {
            Log.d(
                "InnerOkDownloadListener",
                "setOnProgressListener  ${IMusic.getMusicName()}  ${(taskTag)} ${it.currentBytes}  ${it.totalBytes} "
            )
        }
        downloadRequest.start(InnerOkDownloadListener(IMusic, taskTag))
        return downloadRequest
    }

    suspend fun getNext(current: IMusic?): IMusic? {
        fetchPlaylists()
        if (headerIMusic == null) {
            return null
        }
        if (selected.isEmpty()) {
            return null
        }

        if (current == null) {
            return headerIMusic
        }
        if (selected.size == 1 && headerIMusic?.getMusicId() == selected[0].getMusicId()) {
            return null
        }
        selected.forEach {
            if (it.getMusicId() != current.getMusicId()) {
                try {
                    removePlaylist(current, false)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return it
            }
        }
        return headerIMusic
    }

    suspend fun addToPlaylists(music: IMusic) {

        RetrofitManager.create(KTVService::class.java)
            .operateSong(
                "select",
                music.getMusicId(),
                RoomManager.mCurrentRoom?.provideRoomId() ?: ""
            )
        RtmManager.rtmClient.sendChannelMsg(RtmTextMsg<IMusic>(
            action_player_list_add,
            music
        ).toJsonString(),
            RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
            true,
            object : RtmCallBack {
                override fun onSuccess() {}
                override fun onFailure(code: Int, msg: String) {}
            })

    }

    suspend fun removeHeadFromPlaylist(music: IMusic, notice: Boolean = true) {
        if (headerIMusic?.getMusicId() != music.getMusicId()) {
            return
        }
        removePlaylist(music, notice)
    }

    suspend fun removePlaylist(music: IMusic, notice: Boolean = true) {
        RetrofitManager.create(KTVService::class.java)
            .operateSong(
                "delete",
                music.getMusicId(),
                RoomManager.mCurrentRoom?.provideRoomId() ?: ""
            )
        if (notice) {
            RtmManager.rtmClient.sendChannelMsg(RtmTextMsg<IMusic>(
                action_player_list_remove,
                music
            ).toJsonString(),
                RoomManager.mCurrentRoom?.provideImGroupId() ?: "",
                true,
                object : RtmCallBack {
                    override fun onSuccess() {}
                    override fun onFailure(code: Int, msg: String) {}
                })
        }
    }


    inner class InnerOkDownloadListener(
        var music: IMusic,
        val tag: String,
        var loadEndCall: ((error: Boolean) -> Unit)? = null
    ) : OnDownloadListener {
        override fun onDownloadComplete() {
            Log.d(
                "InnerOkDownloadListener",
                "onDownloadComplete  ${music.getMusicName()}  ${(tag)} "
            )
            music.getTagDownLoadStatus(tag).downloadStatus =
                TagDownLoadStatus.download_Status_finish
            loadEndCall?.invoke(false)
        }

        override fun onError(error: Error?) {
            Log.d("InnerOkDownloadListener", "onError  ${music.getMusicName()}  ${(tag)} ")
            loadEndCall?.invoke(true)
            music.getTagDownLoadStatus(tag).downloadStatus =
                TagDownLoadStatus.download_Status_undefine

        }

    }
}