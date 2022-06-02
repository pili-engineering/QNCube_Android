package com.qbcube.pkservice

import com.qbcube.pkservice.mode.PKInfo
import com.qncube.liveroomcore.*
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

    private val repeatSynchroJob = Scheduler(3000) {

        backGround {
            doWork {
//                    val temp = mPKSession
//                    val info = mPKDateSource.getPkInfo(mPKSession?.sessionId ?: "")
//                    if (info.status == 2) {
//                        mListenersCall?.invoke()?.forEach {
//                            it.onStop(temp!!, 0, "")
//                        }
//                    }


            }
            catchError {
            }
        }

    }

    private val mPKServiceListener = object : QNPKService.PKServiceListener {

        override fun onInitPKer(pkSession: QNPKSession) {}

        override fun onStart(pkSession: QNPKSession) {
            mPKSession = pkSession
            repeatSynchroJob.start()
        }

        override fun onStop(pkSession: QNPKSession, code: Int, msg: String) {
            mPKSession = null
            repeatSynchroJob.cancel()
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
//            backGround {
//                doWork {
//                    val info = mPKDateSource.getPkInfo(mPKSession?.sessionId ?: "")
//                    if (info.status == PKStatus.RelaySessionStatusSuccess.intValue) {
//
//                        val recever = mUserSource.searchUserByUserId(info.recvUserId)
//                        val inver = mUserSource.searchUserByUserId(info.initUserId)
//                        val pk = fromPkInfo(info)
//                        mListenersCall?.invoke()?.forEach {
//                            it.onInitPKer(pk)
//                        }
//                    }
//                }
//            }
        }
    }

    override fun onRoomLeave() {
        super.onRoomLeave()
        repeatSynchroJob.cancel()
    }

    override fun onRoomClose() {
        super.onRoomClose()
    }


    fun fromPkInfo(info: PKInfo, inver: QNLiveUser, recver: QNLiveUser): QNPKSession {
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