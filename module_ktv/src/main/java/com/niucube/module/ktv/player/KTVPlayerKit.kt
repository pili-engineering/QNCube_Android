package com.niucube.module.ktv.player

import com.alibaba.fastjson.util.ParameterizedTypeImpl
import com.niucube.channelattributes.AttributesCallBack
import com.niucube.channelattributes.RoomAttributesListener
import com.niucube.channelattributes.RoomAttributesManager
import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import com.niucube.comproom.RoomManager
import com.niucube.ktvkit.KTVMusic
import com.niucube.ktvkit.KTVSerialPlayer
import com.niucube.ktvkit.KTVSerialPlayer.Companion.key_current_music
import com.niucube.module.ktv.mode.Song
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.droid.rtc.QNMicrophoneAudioTrack
import com.qiniu.droid.rtc.QNScreenVideoTrack
import com.qiniu.jsonutil.JsonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

class KTVPlayerAdapter : KTVSerialPlayer.KTVSerialPlayerAdapter<Song> {


    private var receiveChannel: (music: KTVMusic<Song>) -> Unit = {}
    private var mRoomAttributesListener = object : RoomAttributesListener {
        override fun onAttributesChange(roomId: String, key: String, values: String) {
            if (roomId == RoomManager.mCurrentRoom?.provideRoomId()
                && key == key_current_music
            ) {
                val pt = ParameterizedTypeImpl(
                    arrayOf(Song::class.java),
                    KTVMusic::class.java,
                    KTVMusic::class.java
                )
                val musicAttribute =
                    JsonUtils.parseObject<KTVMusic<Song>>(values, pt) ?: return
                if(musicAttribute.mixerUid == UserInfoManager.getUserId()){
                    return
                }
                receiveChannel.invoke(musicAttribute)
            }
        }

        override fun onAttributesClear(roomId: String, key: String) {}
    }


    /**
     * 怎么发音乐信令
     */
    override fun sendKTVMusicSignal(
        music: KTVMusic<Song>,
        callback: (isSuccess: Boolean, errorCode: Int, errorMsg: String) -> Unit
    ) {
        RoomAttributesManager.putRoomAttributes(
            RoomManager.mCurrentRoom?.provideRoomId()!!,
            key_current_music,
            JsonUtils.toJson(music),
            true,
            false,
            false, object : AttributesCallBack<Unit> {
                override fun onSuccess(data: Unit) {
                    callback.invoke(true, 0, "")
                }

                override fun onFailure(errorCode: Int, msg: String) {
                    callback.invoke(false, errorCode, msg)
                }
            }
        )
    }

    /**
     * 收到音乐信令的回调
     */
    override fun registerSignalReceiveChannel(channel: (music: KTVMusic<Song>) -> Unit) {
        receiveChannel = channel
    }

    /**
     * 怎么初始化获得当前播放的音乐
     */
    override fun initCurrentPlayingMusic(call: (music: KTVMusic<Song>?) -> Unit) {

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val data = RoomAttributesManager.getRoomAttributesByKeys(
                    RoomManager.mCurrentRoom?.provideRoomId()
                        ?: "",
                    key_current_music
                )
                val pt = ParameterizedTypeImpl(
                    arrayOf(Song::class.java),
                    KTVMusic::class.java,
                    KTVMusic::class.java
                )
                val musicAttribute = JsonUtils.parseObject<KTVMusic<Song>>(data.value, pt)
                call.invoke(musicAttribute)
            } catch (e: Exception) {
                e.printStackTrace()
                call.invoke(null)
            }
        }
    }

    /**
     * 保存当前播放进度
     */
    override fun saveCurrentPlayingMusicToServer(music: KTVMusic<Song>) {
        RoomAttributesManager.putRoomAttributes(
            RoomManager.mCurrentRoom?.provideRoomId()!!,
            key_current_music,
            JsonUtils.toJson(music),
            true,
            false,
            true,
            object : AttributesCallBack<Unit> {
                override fun onSuccess(data: Unit) {
                }

                override fun onFailure(errorCode: Int, msg: String) {
                }
            }
        )
    }


    init {
        RoomAttributesManager.addRoomAttributesListener(mRoomAttributesListener)
    }

    fun releasePlayer() {
        RoomAttributesManager.removeRoomAttributesListener(mRoomAttributesListener)
    }

}

class KTVPlayerKit() : KTVSerialPlayer<Song>(
    UserInfoManager.getUserId(),
    KTVPlayerAdapter()
) {

    private val mRoomLifecycleMonitor = object : RoomLifecycleMonitor {
        override fun onRoomEntering(roomEntity: RoomEntity) {
            super.onRoomEntering(roomEntity)
            onJoinRoom()
        }
    }
    init {
        RoomManager.addRoomLifecycleMonitor(mRoomLifecycleMonitor)
    }


    override fun releasePlayer() {
        RoomManager.removeRoomLifecycleMonitor(mRoomLifecycleMonitor)
        (adapter as KTVPlayerAdapter).releasePlayer()
        super.releasePlayer()
    }
}
