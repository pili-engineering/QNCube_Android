package com.qncube.liveroom_pullclient

import com.niucube.rtm.RtmManager
import com.niucube.rtm.joinChannel
import com.niucube.rtm.leaveChannel
import com.qncube.liveroomcore.*
import com.qncube.liveroomcore.datasource.RoomDataSource
import com.qncube.liveroomcore.mode.QNLiveRoomInfo

class QNLivePullClientImpl : QNLivePullClient {
    private val mRoomSource = RoomDataSource()
    private var mPlayer: IPullPlayer? = null
    private var mPullClientListener: QNPullClientListener? = null
    private val mQNLiveRoomContext by lazy { QNLiveRoomContext(this) }

    init {
        mQNLiveRoomContext.mRoomScheduler.roomStatusChange = {
            mPullClientListener?.onRoomStatusChange(it, "")
        }
    }

    /**
     * 注册需要的服务
     * @param serviceClasses
     * @param <T>
    </T> */
    override fun <T : QNLiveService> registerService(serviceClasses: Class<T>) {
        mQNLiveRoomContext.registerService(serviceClasses)
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
     * @param liveId
     * @param callBack
     */
    override fun joinRoom(liveId: String, callBack: QNLiveCallBack<QNLiveRoomInfo>?) {
        backGround {
            doWork {
                mQNLiveRoomContext.enter(liveId, QNLiveRoomEngine.getCurrentUserInfo())
                val roomInfo = mRoomSource.joinRoom(liveId)

                if (RtmManager.isInit) {
                    RtmManager.rtmClient.joinChannel(roomInfo.chatId)
                }
                mQNLiveRoomContext.joinedRoom(roomInfo)
                mPlayer?.start(roomInfo)
                callBack?.onSuccess(roomInfo)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    /**
     * 离开房间
     * @param callBack
     */
    override fun leaveRoom(callBack: QNLiveCallBack<Void>?) {

        backGround {
            doWork {
                mRoomSource.leaveRoom(mQNLiveRoomContext.roomInfo?.liveId ?: "")
                if (RtmManager.isInit) {
                    RtmManager.rtmClient.leaveChannel(mQNLiveRoomContext.roomInfo?.chatId ?: "")
                }
                mQNLiveRoomContext.leaveRoom()
                mPlayer?.stopPlay()
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
        mQNLiveRoomContext.close()
        mPlayer?.stopPlay()
    }

    override fun getClientType(): ClientType {
        return ClientType.CLIENT_PULL
    }

    override fun setPullClientListener(listener: QNPullClientListener?) {
        mPullClientListener = listener
    }

    override fun setPullPreview(player: IPullPlayer?) {
        mPlayer = player
    }

    override fun getPullPreview(): IPullPlayer? {
        return mPlayer
    }

}