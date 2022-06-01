package com.nucube.rtclive

import android.content.Context
import android.util.Base64
import android.util.Log
import com.niucube.qnrtcsdk.ExtQNClientEventListener
import com.niucube.qnrtcsdk.RtcEngineWrap
import com.niucube.qnrtcsdk.SimpleQNRTCListener
import com.qiniu.droid.rtc.*
import com.qncube.rtcexcepion.RtcException
import org.json.JSONObject
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

open class RtcLiveRoom(
    val appContext: Context,
    val mQNRTCSetting: QNRTCSetting = QNRTCSetting(),
    val mClientConfig: QNRTCClientConfig = QNRTCClientConfig(
        QNClientMode.LIVE,
        QNClientRole.BROADCASTER
    )
) : RtcEngineWrap(appContext, mQNRTCSetting, mClientConfig) {

    private val TAG = "RtcRoom"

    companion object {
        const val TAG_CAMERA = "camera"

        //屏幕采集轨道的标记
        const val TAG_SCREEN = "screen"
        const val TAG_AUDIO = "audio"
    }

    var meId = ""
        private set
    var roomName = ""
        private set
    var roomToken = ""
        private set

    private val mCameraTrackViewStore = CameraTrackViewStore()
    var localVideoTrack: QNCameraVideoTrack? = null
    private set
    var localAudioTrack: QNMicrophoneAudioTrack? = null
    private set
    private var mVideoFrameListener: QNVideoFrameListener? = null
    private var mAudioFrameListener: QNAudioFrameListener? = null

    //房间里存在的所以轨道
    private var mAllTrack = ArrayList<QNTrack>()

    //混流
    val mMixStreamManager by lazy { MixStreamManager(this) }

    init {
        addExtraQNRTCEngineEventListener(object : ExtQNClientEventListener {
            private fun afterPublished(p0: String, p1: List<QNTrack>, isRemote: Boolean) {
                mAllTrack.addAll(p1)
                p1.forEach { track ->
                    when (track.tag) {
                        TAG_CAMERA -> {
                            Log.d("mUserUnbindCa", "afterPublished  TAG_CAMERA  ${p0}")
                            mCameraTrackViewStore.mUserUnbindVideoWindowMap[p0]?.let {
                                if (track is QNLocalVideoTrack) {
                                    track.play(it)
                                } else if (track is QNRemoteVideoTrack) {
                                    track.play(it)
                                } else {
                                    throw Exception("不支持的轨道")
                                }
                                mCameraTrackViewStore.move2BindedMap(p0)
                            }
                        }
                    }
                }
            }

            override fun onLocalPublished(var1: String, var2: List<QNLocalTrack>) {
                afterPublished(var1, var2, false)
            }

            override fun onLocalUnpublished(var1: String, var2: List<QNLocalTrack>) {
                var2.forEach {
                    if (mAllTrack.contains(it)) {
                        mAllTrack.remove(it)
                    }
                    if (it is QNCameraVideoTrack) {
                        mCameraTrackViewStore.move2UnbindMap(var1)
                    }
                }
            }

            override fun onConnectionStateChanged(
                p0: QNConnectionState?,
                p1: QNConnectionDisconnectedInfo?
            ) {
            }

            override fun onUserJoined(p0: String?, p1: String?) {}
            override fun onUserReconnecting(p0: String?) {}
            override fun onUserReconnected(p0: String?) {}

            override fun onUserLeft(p0: String) {
                val toRemove = ArrayList<QNTrack>()
                mAllTrack.forEach {
                    if (it.userID == p0) {
                        toRemove.add(it)
                        //mUserBindCameraWindowMap.remove(it)
                    }
                }
                mAllTrack.removeAll(toRemove)
                mCameraTrackViewStore.removeUserView(p0)
            }

            override fun onUserPublished(p0: String, p1: MutableList<QNRemoteTrack>) {
                afterPublished(p0, p1, true)
            }

            override fun onUserUnpublished(p0: String, p1: MutableList<QNRemoteTrack>) {
                p1.forEach { it ->
                    if (mAllTrack.contains(it)) {
                        mAllTrack.remove(it)
                    }
                    if (it is QNRemoteVideoTrack) {
                        mCameraTrackViewStore.move2UnbindMap(p0)
                    }
                }
            }

            override fun onSubscribed(
                p0: String?,
                p1: MutableList<QNRemoteAudioTrack>?,
                p2: MutableList<QNRemoteVideoTrack>?
            ) {
            }

            override fun onMessageReceived(p0: QNCustomMessage?) {}
            override fun onMediaRelayStateChanged(p0: String?, p1: QNMediaRelayState?) {}
        })
    }

    private fun createVideoTrack(params: QNVideoEncoderConfig): QNCameraVideoTrack {
        // 创建本地 Camera 视频 Track
        val cameraVideoTrackConfig = QNCameraVideoTrackConfig(TAG_CAMERA)
            .setVideoEncoderConfig(
                params
            )
        return QNRTC.createCameraVideoTrack(cameraVideoTrackConfig);
    }

    private fun createAudioTrack(microphoneAudioTrackConfig: QNMicrophoneAudioTrackConfig): QNMicrophoneAudioTrack {
        return QNRTC.createMicrophoneAudioTrack(QNMicrophoneAudioTrackConfig(TAG_AUDIO).apply {
            setAudioQuality(microphoneAudioTrackConfig.audioQuality)
            setCommunicationModeOn(microphoneAudioTrackConfig.isCommunicationModeOn)
        })
    }

    //启动视频采集
    fun enableCamera(cameraParams: QNCameraParams) {
        localVideoTrack = createVideoTrack(
            QNVideoEncoderConfig(
                cameraParams.width,
                cameraParams.height,
                cameraParams.fps,
                cameraParams.bitrate
            )
        )

        localVideoTrack?.setVideoFrameListener(object : QNVideoFrameListener {
            override fun onYUVFrameAvailable(
                p0: ByteArray?,
                p1: QNVideoFrameType?,
                p2: Int,
                p3: Int,
                p4: Int,
                p5: Long
            ) {
                mVideoFrameListener?.onYUVFrameAvailable(p0, p1, p2, p3, p4, p5)
            }

            override fun onTextureFrameAvailable(
                p0: Int,
                p1: QNVideoFrameType?,
                p2: Int,
                p3: Int,
                p4: Int,
                p5: Long,
                p6: FloatArray?
            ): Int {
                return mVideoFrameListener?.onTextureFrameAvailable(p0, p1, p2, p3, p4, p5, p6)
                    ?: p0
            }
        })

    }

    //启动音频采集
    fun enableMicrophone(microphoneParams: QNMicrophoneParams) {
        localAudioTrack = createAudioTrack(
            QNMicrophoneAudioTrackConfig()
                .setAudioQuality(
                    QNAudioQuality(
                        microphoneParams.mSampleRate,
                        microphoneParams.mChannelCount,
                        microphoneParams.mBitsPerSample,
                        microphoneParams.mBitrate
                    )
                )
        )

        localAudioTrack?.setAudioFrameListener { p0, p1, p2, p3, p4 ->
            mAudioFrameListener?.onAudioFrameAvailable(
                p0,
                p1,
                p2,
                p3,
                p4
            )
        }
    }

    suspend fun joinRtc(token: String, msg: String) =
        suspendCoroutine<Unit> { continuation ->

            val tokens: Array<String> = token.split(":".toRegex()).toTypedArray()
            val b64 = String(Base64.decode(tokens[2].toByteArray(), Base64.DEFAULT))
            val json: JSONObject = JSONObject(b64)
            val mAppId = json.optString("appId")
            val mRoomName = json.optString("roomName")
            val mUserId = json.optString("userId")
            meId = mUserId
            roomName = mRoomName
            roomToken = token

            val trackQNRTCEngineEvent = object : SimpleQNRTCListener {
                override fun onConnectionStateChanged(
                    state: QNConnectionState,
                    p1: QNConnectionDisconnectedInfo?
                ) {
                    if (state == QNConnectionState.CONNECTED) {
                        removeExtraQNRTCEngineEventListener(this)
                        continuation.resume(Unit)
                    }

                    if (state == QNConnectionState.DISCONNECTED) {
                        removeExtraQNRTCEngineEventListener(this)
                        continuation.resumeWithException(
                            RtcException(
                                p1?.errorCode ?: 1,
                                p1?.errorMessage ?: ""
                            )
                        )
                    }
                }
            }

            addExtraQNRTCEngineEventListener(trackQNRTCEngineEvent)
            mClient.join(token, msg)
        }

    suspend fun publishLocal() = suspendCoroutine<Unit> { continuation ->
        val tracks = ArrayList<QNLocalTrack>().apply {
            localVideoTrack?.let { add(it) }
            localAudioTrack?.let { add(it) }
        }
        mClient.publish(object : QNPublishResultCallback {
            override fun onPublished() {
                continuation.resume(Unit)
                mQNRTCEngineEventWrap.onLocalPublished(meId, tracks)
            }

            override fun onError(p0: Int, p1: String) {
                continuation.resumeWithException(RtcException(p0, p1))
            }
        }, tracks)
    }

    suspend fun leave() {
        mClient.leave()
        localAudioTrack = null
        localVideoTrack = null
        mCameraTrackViewStore.clear()
    }

    fun close() {
        mQNRTCEngineEventWrap.clear()
        QNRTC.deinit()
    }

    //切换摄像头
    fun switchCamera() {
        localVideoTrack?.switchCamera(object : QNCameraSwitchResultCallback {
            override fun onSwitched(p0: Boolean) {
            }

            override fun onError(p0: String?) {}
        })
    }

    //设置本地预览
    fun setLocalPreView(view: QNRenderView) {
        localVideoTrack?.tryPlay(view)
    }

    //禁/不禁 本地摄像头推流
    fun muteLocalCamera(muted: Boolean): Boolean {
        localVideoTrack?.isMuted = muted
        return localVideoTrack != null
    }

    //禁/不禁 本地摄像头推流
    fun muteLocalMicrophone(muted: Boolean): Boolean {
        localAudioTrack?.isMuted = muted
        return localVideoTrack != null
    }

    //设置视频帧回调
    fun setVideoFrameListener(frameListener: QNVideoFrameListener?) {
        mVideoFrameListener = frameListener
    }

    //设置音频帧回调
    fun setAudioFrameListener(frameListener: QNAudioFrameListener?) {
        mAudioFrameListener = frameListener
    }

    /**
     * 获取某人的视频轨道 如果需要用到track
     */
    fun getUserVideoTrackInfo(uid: String): QNTrack? {
        mAllTrack.forEach {
            if (it.tag == TAG_CAMERA && it.userID == uid) {
                return it as QNTrack
            }
        }
        return null
    }

    /**
     * 获取某人的音频轨道
     */
    fun getUserAudioTrackInfo(uid: String): QNTrack? {
        mAllTrack.forEach {
            if (it.tag == TAG_AUDIO && it.userID == uid) {
                return it as QNTrack
            }
        }
        return null
    }

    /**
     * 设置某人的摄像头预览窗口 可以在任何时候调用
     */
    fun setUserCameraWindowView(uid: String, view: QNRenderView) {
        var isBind = false
        if (uid == meId) {
            setLocalPreView(view)
            mCameraTrackViewStore.put2BindedMap(uid, view)
            return
        }
        var findTrack = false
        mAllTrack.forEach {
            if (it.tag == TAG_CAMERA && it.userID == uid) {
                findTrack = true
                it.tryPlay(view)
                Log.d("mUserUnbindCa", "setUserCameraWindowView  找打了${uid}")
                return@forEach
            }
        }
        if (!findTrack) {
            Log.d("mUserUnbindCa", "setUserCameraWindowView  没找打${uid}")
            mCameraTrackViewStore.put2UnbindMap(uid, view)
        } else {
            mCameraTrackViewStore.put2BindedMap(uid, view)
        }
    }

}