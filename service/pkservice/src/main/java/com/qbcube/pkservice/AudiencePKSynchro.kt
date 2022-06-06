package com.qbcube.pkservice

import com.qbcube.pkservice.mode.PKInfo
import com.qncube.liveroomcore.*
import com.qncube.liveroomcore.datasource.RoomDataSource
import com.qncube.liveroomcore.datasource.UserDataSource
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.liveroomcore.mode.QNLiveUser
import java.util.*

class AudiencePKSynchro() : BaseService() {

    private val mPKDateSource = PKDateSource()
    private val mUserSource = UserDataSource()
    var mListenersCall: (() -> LinkedList<QNPKService.PKServiceListener>)? = null
    var mPKSession: QNPKSession? = null
        private set

    private val repeatSynchroJob = Scheduler(6000) {

        if (roomInfo == null) {
            return@Scheduler
        }
        backGround {
            doWork {

                if (roomInfo?.pkId?.isEmpty() == false) {
                    //当前房间在PK
                    val info = mPKDateSource.getPkInfo(roomInfo?.liveId ?: "")
                    if (info.status == PKStatus.RelaySessionStatusStopped.intValue && mPKSession != null) {
                        roomInfo?.pkId = ""
                        mListenersCall?.invoke()?.forEach {
                            it.onStop(mPKSession!!, -1, "time out")
                        }
                        mPKSession = null
                    }
                } else {
                    val reFreshRoom = RoomDataSource().refreshRoomInfo(roomInfo!!.liveId)
                    if (!reFreshRoom.pkId.isEmpty() && mPKSession == null) {

                        val info = mPKDateSource.getPkInfo(reFreshRoom.liveId ?: "")
                        if (info.status == PKStatus.RelaySessionStatusSuccess.intValue) {

                            val recever = mUserSource.searchUserByUserId(info.recvUserId)
                            val inver = mUserSource.searchUserByUserId(info.initUserId)
                            val pk = fromPkInfo(info, inver, recever)
                            mPKSession = pk
                            roomInfo?.pkId = reFreshRoom.pkId
                            mListenersCall?.invoke()?.forEach {
                                it.onStart(mPKSession!!)
                            }
                        }

                    }
                }
            }
            catchError {

            }
        }

    }

    private val mPKServiceListener = object : QNPKService.PKServiceListener {

        override fun onInitPKer(pkSession: QNPKSession) {}

        override fun onStart(pkSession: QNPKSession) {
            mPKSession = pkSession
            // repeatSynchroJob.start()
        }

        override fun onStop(pkSession: QNPKSession, code: Int, msg: String) {
            mPKSession = null
            // repeatSynchroJob.cancel()
            roomInfo?.pkId = ""
        }

        override fun onWaitPeerTimeOut(pkSession: QNPKSession) {
        }

        override fun onPKExtensionUpdate(pkSession: QNPKSession, extension: Extension) {
        }
    }

    override fun attachRoomClient(client: QNLiveRoomClient) {
        super.attachRoomClient(client)
        mListenersCall?.invoke()?.add(mPKServiceListener)
    }

    /**
     * 进入回
     * @param user
     */
    override fun onRoomEnter(liveId: String, user: QNLiveUser) {
    }

    override fun onRoomJoined(roomInfo: QNLiveRoomInfo) {
        super.onRoomJoined(roomInfo)
        if (!roomInfo.pkId.isEmpty()) {
            backGround {
                doWork {
                    val info = mPKDateSource.getPkInfo(roomInfo.pkId ?: "")
                    if (info.status == PKStatus.RelaySessionStatusSuccess.intValue) {
                        val recever = mUserSource.searchUserByUserId(info.recvUserId)
                        val inver = mUserSource.searchUserByUserId(info.initUserId)
                        val pk = fromPkInfo(info, inver, recever)
                        mPKSession = pk
                        mListenersCall?.invoke()?.forEach {
                            it.onInitPKer(pk)
                        }
                    }
                }
                catchError {

                }
                onFinally {
                    repeatSynchroJob.start(true)
                }
            }
        } else {
            repeatSynchroJob.start(true)
        }
    }

    override fun onRoomLeave() {
        super.onRoomLeave()
        repeatSynchroJob.cancel()
    }

    override fun onRoomClose() {
        super.onRoomClose()
    }


    private fun fromPkInfo(info: PKInfo, inver: QNLiveUser, recver: QNLiveUser): QNPKSession {
        return QNPKSession().apply {
            //PK场次ID
            sessionId = info.id
            //发起方
            initiator = inver
            //接受方
            receiver = recver
            //发起方所在房间
            initiatorRoomId = info.initRoomId
            //接受方所在房间
            receiverRoomId = info.recvRoomId
            //扩展字段
            extensions = info.extensions
            //pk 状态 0邀请过程  1pk中 2结束 其他自定义状态比如惩罚时间
            status = info.status
            //pk开始时间戳
            startTimeStamp = info.createdAt
        }
    }
}