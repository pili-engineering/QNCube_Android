package com.niucube.comp.mutabletrackroom

import android.content.Context
import android.text.TextUtils
import com.niucube.comproom.*
import com.niucube.qnrtcsdk.QNRTCEngineEventWrap
import com.niucube.basemutableroom.*
import com.niucube.basemutableroom.RtcException
import com.niucube.basemutableroom.absroom.AudioTrackParams
import com.niucube.basemutableroom.absroom.VideoTrackParams
import com.niucube.basemutableroom.absroom.seat.UserExtension
import com.niucube.basemutableroom.joinRtc
import com.niucube.rtm.RtmException
import com.qiniu.droid.rtc.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/**
 * 多人不定人房间
 */
class MutableTrackRoom(
    appContext: Context,
    mQNRTCSetting: QNRTCSetting = QNRTCSetting(),
    clientConfig: QNRTCClientConfig = QNRTCClientConfig(QNClientMode.LIVE, QNClientRole.BROADCASTER)
) : BaseMutableRoom<MutableMicSeat>(appContext, mQNRTCSetting, clientConfig) {

    //角色
    override val mQNRTCEngineEventWrap =
        QNRTCEngineEventWrap().apply {
            addSelfQNRTCEngineEventWrap(this)
        }

    override fun addSelfQNRTCEngineEventWrap(eventWrap: QNRTCEngineEventWrap) {
        super.addSelfQNRTCEngineEventWrap(eventWrap)
        eventWrap.addExtraQNRTCEngineEventListener(InnerMutableSimpleQNRTCListener(this@MutableTrackRoom))
    }

    //添加麦位监听
    fun addMicSeatListener(micSeatListener: MicSeatListener) {
        mTrackSeatListeners.add(micSeatListener)
    }

    //移除麦位监听
    fun removeMicSeatListener(micSeatListener: MicSeatListener) {
        mTrackSeatListeners.remove(micSeatListener)
    }

    private var mUserExtension: UserExtensionWrap? = null

    //设置角色 暂不支持切换
    suspend fun suspendSetClientRole(value: ClientRoleType) =
        suspendCoroutine<Unit> { continuation ->
            setClientRole(value, object : QNClientRoleResultCallback {
                override fun onResult(p0: QNClientRole?) {
                    continuation.resume(Unit)
                }

                override fun onError(p0: Int, p1: String) {
                    continuation.resumeWithException(RtcException(p0, p1))
                }
            })
        }

    //设置角色
    public override fun setClientRole(value: ClientRoleType, call: QNClientRoleResultCallback) {
        if (RoomManager.mCurrentRoom == null) {
            //初始化设置
            super.setClientRole(value, call)
        } else {

            val roleTemp = mClientRole
            //主播变观众
            if ((mClientRole == ClientRoleType.CLIENT_ROLE_BROADCASTER) &&
                (value == ClientRoleType.CLIENT_ROLE_PULLER || value == ClientRoleType.CLIENT_ROLE_AUDIENCE)
            ) {
                GlobalScope.launch(Dispatchers.Main) {
                    try {
                        setClientRoleSuspend(value)
                        val seat = getUserSeat(RoomManager.mCurrentRoom?.provideMeId() ?: "")
                        mRtcRoomSignaling.sitDown(seat!!)
                        mMicSeats.remove(seat)
                        if (value == ClientRoleType.CLIENT_ROLE_PULLER) {
                            mClient.leave()
                            RoomManager.mCurrentRoom?.let {
                                mIAudiencePlayerView?.startAudiencePlay(it)
                            }
                        }
                        call.onResult(value.toQNClientRoleType())
                    } catch (e: RtcException) {
                        mClientRole = roleTemp
                        call.onError(e.code, e.msg)
                    } catch (e: RtmException) {
                        mClientRole = roleTemp
                        call.onError(e.code, e.msg)
                    } catch (e: Exception) {
                        mClientRole = roleTemp
                        call.onError(0, e.message)
                    }
                }
            } else
                if (mClientRole == ClientRoleType.CLIENT_ROLE_PULLER &&
                    ((value == ClientRoleType.CLIENT_ROLE_BROADCASTER) || (value == ClientRoleType.CLIENT_ROLE_AUDIENCE))
                ) {
                    GlobalScope.launch(Dispatchers.Main) {
                        try {
                            setClientRoleSuspend(value)
                            joinRtc(RoomManager.mCurrentRoom?.provideRoomToken() ?: "", "")
                            val seat = MutableMicSeat().apply {
                                uid = RoomManager.mCurrentRoom?.provideMeId() ?: ""
                                userExtension = mUserExtension
                            }
                            if (value == ClientRoleType.CLIENT_ROLE_BROADCASTER) {
                                mMicSeats.add(seat)
                                mTrackSeatListener.onUserSitDown(seat)
                                mRtcRoomSignaling.sitDown(seat)
                            }
                            mIAudiencePlayerView?.stopAudiencePlay()
                            call.onResult(value.toQNClientRoleType())
                        } catch (e: RtcException) {
                            mClientRole = value
                            mClient.leave()
                            call.onError(e.code, e.msg)
                        } catch (e: RtmException) {
                            mClientRole = value
                            mClient.leave()
                            call.onError(e.code, e.msg)
                        } catch (e: Exception) {
                            mClientRole = value
                            mClient.leave()
                            call.onError(0, e.message)
                        }
                    }
                }
        }
    }

    //加入房间
    public override suspend fun joinRoom(
        roomEntity: RoomEntity,
        userExt: UserExtension?
    ) {

        val uex = userExt ?: UserExtension().apply {
            uid = roomEntity.provideMeId();
        }
        val seat = MutableMicSeat().apply {
            uid = roomEntity.provideMeId() ?: ""
            userExtension = UserExtensionWrap(uex, mClientRole.role)
        }
        mUserExtension = UserExtensionWrap(uex, mClientRole.role)
        super.joinRoom(roomEntity, mUserExtension)
        if (mClientRole == ClientRoleType.CLIENT_ROLE_BROADCASTER) {
            //主播角色回调自己上麦
            mMicSeats.add(seat)
            mTrackSeatListener.onUserSitDown(seat)
            mRtcRoomSignaling.sitDown(seat)
        }
    }

    //离开房间
    public override suspend fun leaveRoom() {
        val seat = getUserSeat(RoomManager.mCurrentRoom?.provideMeId() ?: "")
        if (mClientRole == ClientRoleType.CLIENT_ROLE_BROADCASTER && seat != null) {
            mRtcRoomSignaling.sitUp(seat)
        }
        super.leaveRoom()
    }

    //关闭清理
    override fun closeRoom() {
        super.closeRoom()
    }

    //设置摄像头参数
    public override fun setCameraVideoTrackParams(videoEncoderConfig: VideoTrackParams) {
        super.setCameraVideoTrackParams(videoEncoderConfig)
    }

    //设置麦克风参数
    public override fun setMicrophoneAudioParams(microphoneAudioTrackConfig: AudioTrackParams) {
        super.setMicrophoneAudioParams(microphoneAudioTrackConfig)
    }

    //开启本地音频
    public override fun enableAudio(call: QNPublishResultCallback) {
        super.enableAudio(call)
    }

    //开启本地视频
    public override fun enableVideo(call: QNPublishResultCallback) {
        super.enableVideo(call)
    }

    //关闭本地视频
    public override fun disableVideo() {
        super.disableVideo()
    }

    //禁用本地音频
    public override fun disableAudio() {
        super.disableAudio()
    }

    //禁用本地视频
    public override fun muteLocalVideo(muted: Boolean) {
        super.muteLocalVideo(muted)
    }

    public override fun muteLocalAudio(muted: Boolean) {
        super.muteLocalAudio(muted)
    }

    //禁用远端音频
    public override fun muteRemoteAudio(uid: String, muted: Boolean) {
        super.muteRemoteAudio(uid, muted)
    }

    //禁用远端视频
    public override fun muteRemoteVideo(uid: String, muted: Boolean) {
        super.muteRemoteVideo(uid, muted)
    }

    //禁用远端全部视频
    public override fun muteAllRemoteVideo(muted: Boolean) {
        super.muteAllRemoteVideo(muted)
    }

    //禁用远端全部音频
    public override fun muteAllRemoteAudio(muted: Boolean) {
        super.muteAllRemoteAudio(muted)
    }

    private class InnerMutableSimpleQNRTCListener(val mutableRoom: MutableTrackRoom) :
        MutableSimpleQNRTCListener<MutableMicSeat>(mutableRoom) {

        override fun onUserJoined(p0: String, p1: String?) {
            super.onUserJoined(p0, p1)
            if (mutableRoom.getUserSeat(p0) != null) {
                return
            }
            val seat = MutableMicSeat().apply {
                uid = p0
                if (!TextUtils.isEmpty(p1)) {
                    val json = JSONObject(p1)
                    val ext = UserExtensionWrap().apply {
                        try {
                            clientRoleType = json.optInt("clientRoleType") ?: -1
                            userExtRoleType = json.optString("userExtRoleType")
                            userExtProfile = json.optString("userExtProfile")
                            userExtensionMsg = json.optString("userExtensionMsg")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    userExtension = ext
                }
            }
          //如果加入rtc房间里的人是主播角色 则回调上麦
            if (((seat.userExtension as UserExtensionWrap?)?.clientRoleType
                    ?: -1) == ClientRoleType.CLIENT_ROLE_BROADCASTER.role
            ) {
                mutableRoom.mMicSeats.add(seat)
                mutableRoom.mTrackSeatListener.onUserSitDown(seat)
            }
        }
    }
}