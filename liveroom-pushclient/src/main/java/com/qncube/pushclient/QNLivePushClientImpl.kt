package com.qncube.pushclient

import com.niucube.rtm.RtmManager
import com.niucube.rtm.joinChannel
import com.niucube.rtm.leaveChannel
import com.nucube.rtclive.*
import com.qiniu.droid.rtc.*
import com.qncube.liveroomcore.*
import com.qncube.liveroomcore.datasource.RoomDataSource
import com.qncube.liveroomcore.mode.QNLiveRoomInfo

class QNLivePushClientImpl : QNLivePushClient {
    private val mRoomSource = RoomDataSource()
    private val mQNLiveRoomContext by lazy { QNLiveRoomContext(this) }
    private var mQNPushClientListener: QNPushClientListener? = null

    private var mCameraParams: QNCameraParams = QNCameraParams()
    private var mQNMicrophoneParams: QNMicrophoneParams = QNMicrophoneParams()

    //用于反射字段名字勿动
    private var mRtcLiveRoom: RtcLiveRoom? = null

    private val mRtcRoom by lazy {
        val file = RtcLiveRoom(AppCache.appContext).apply {
            addExtraQNRTCEngineEventListener(PushExtQNClientEventListener(mQNPushClientListener))
        }
        file
    }

    init {
        mRtcLiveRoom = mRtcRoom
        mRtcRoom.addExtraQNRTCEngineEventListener(
            object : DefaultExtQNClientEventListener {
                override fun onConnectionStateChanged(
                    p0: QNConnectionState,
                    p1: QNConnectionDisconnectedInfo?
                ) {
                    super.onConnectionStateChanged(p0, p1)
                    mQNPushClientListener?.onConnectionStateChanged(p0, p1?.reason?.name ?: "")
                }
            }
        )

        mQNLiveRoomContext.mRoomScheduler.roomStatusChange = {
            mQNPushClientListener?.onRoomStatusChange(it, "")
        }
    }

    /**
     * 注册需要的服务
     *
     * @param serviceClass
     * @param <T>
    </T> */
    override fun <T : QNLiveService> registerService(serviceClass: Class<T>) {
        mQNLiveRoomContext.registerService(serviceClass)
    }

    /**
     * 获取服务实例
     *
     * @param serviceClass
     * @param <T>
     * @return
    </T> */
    override fun <T : QNLiveService> getService(serviceClass: Class<T>): T? {
        return mQNLiveRoomContext.getService(serviceClass)
    }

    /**
     * 添加房间生命周期状态监听
     *
     * @param lifeCycleListener
     */
    override fun addRoomLifeCycleListener(lifeCycleListener: QNRoomLifeCycleListener) {
        mQNLiveRoomContext.addRoomLifeCycleListener(lifeCycleListener)
    }

    override fun removeRoomLifeCycleListener(lifeCycleListener: QNRoomLifeCycleListener) {
        mQNLiveRoomContext.removeRoomLifeCycleListener(lifeCycleListener)
    }

    /**
     * 加入房间
     *
     * @param roomId
     * @param callBack
     */
    override fun joinRoom(roomId: String, callBack: QNLiveCallBack<QNLiveRoomInfo>?) {
        backGround {
            doWork {
                mQNLiveRoomContext.enter(roomId, QNLiveRoomEngine.getCurrentUserInfo())

                val roomInfo = mRoomSource.pubRoom(roomId)

                if (RtmManager.isInit) {
                    RtmManager.rtmClient.joinChannel(roomInfo.chatId)
                }
                if (!mRtcRoom.mMixStreamManager.isInit) {
                    mRtcRoom.mMixStreamManager.init(roomId,  roomInfo.pushUrl, MixStreamParams().apply {
                        this.mixStreamWidth = mCameraParams.width
                        this.mixBitrate = mCameraParams.bitrate + mQNMicrophoneParams.mBitrate
                        this.fps = mCameraParams.fps
                    })
                }
                mRtcRoom.joinRtc(roomInfo.roomToken, "")
                mRtcRoom.publishLocal()

                mQNLiveRoomContext.joinedRoom(roomInfo)
                callBack?.onSuccess(roomInfo)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }

    }

    /**
     * 离开房间
     *
     * @param callBack
     */
    override fun leaveRoom(callBack: QNLiveCallBack<Void>?) {
        backGround {
            doWork {
                mRoomSource.leaveRoom(mQNLiveRoomContext.roomInfo?.liveId ?: "")
                if (RtmManager.isInit) {
                    RtmManager.rtmClient.leaveChannel(mQNLiveRoomContext.roomInfo?.chatId ?: "")
                }
                mRtcRoom.leave()
                mQNLiveRoomContext.leaveRoom()
                callBack?.onSuccess(null)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    /**
     * 关闭房间
     */
    override fun closeRoom() {
        mRtcRoom.close()
        mQNLiveRoomContext.close()
    }

    override fun enableCamera(cameraParams: QNCameraParams?) {
        cameraParams?.let { mCameraParams = it }
        mRtcRoom.enableCamera(cameraParams ?: QNCameraParams())
    }

    override fun enableMicrophone(microphoneParams: QNMicrophoneParams?) {
        microphoneParams?.let { mQNMicrophoneParams = it }
        mRtcRoom.enableMicrophone(microphoneParams ?: QNMicrophoneParams())
    }

    override fun switchCamera() {
        mRtcRoom.switchCamera()
    }

    override fun setPushClientListener(pushClientListener: QNPushClientListener) {
        mQNPushClientListener = pushClientListener
    }

    private var mLocalPreView: QNRenderView? = null

    override fun setLocalPreView(view: QNRenderView) {
        mRtcRoom.setLocalPreView(view)
    }

    override fun getLocalPreView(): QNRenderView? {
        return mLocalPreView
    }

    override fun muteLocalCamera(muted: Boolean) {
        if (mRtcRoom.muteLocalCamera(muted)) {
            mQNPushClientListener?.onCameraStatusChange(!muted)
        }
    }

    override fun muteLocalMicrophone(muted: Boolean) {
        if (mRtcRoom.muteLocalCamera(muted)) {
            mQNPushClientListener?.onMicrophoneStatusChange(!muted)
        }
    }

    override fun setVideoFrameListener(frameListener: QNVideoFrameListener?) {
        mRtcRoom.setVideoFrameListener(frameListener)
    }

    override fun setAudioFrameListener(frameListener: QNAudioFrameListener?) {
        mRtcRoom.setAudioFrameListener(frameListener)
    }

    override fun getClientType(): ClientType {
        return ClientType.CLIENT_PUSH
    }

}