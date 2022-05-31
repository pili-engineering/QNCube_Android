package com.qbcube.pkservice

import com.qncube.liveroomcore.*
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.liveroomcore.mode.QNLiveUser
import java.util.*

class AudiencePKSynchro() : BaseService() {

    private val mPKDateSource = PKDateSource()

    var mListenersCall: (() -> LinkedList<QNPKService.PKServiceListener>)? = null
    var mPKSession: QNPKSession? = null
        private set

    private val repeatSynchroJob = Scheduler(3000) {
        if (mPKSession != null) {
            backGround {
                doWork {
                    val temp = mPKSession
                    val info = mPKDateSource.getPkInfo(mPKSession?.sessionId ?: "")
                    if (info.status == 2) {
                        mListenersCall?.invoke()?.forEach {
                            it.onStop(temp!!, 0, "")
                        }
                    }
                }
                catchError {
                }
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
     * 进入回调
     *
     * @param user
     */
    override fun onRoomEnter(roomId: String, user: QNLiveUser) {

    }

    override fun onRoomJoined(roomInfo: QNLiveRoomInfo) {
        super.onRoomJoined(roomInfo)
        if (!roomInfo.pkId.isEmpty()) {
            backGround {
                doWork {
                    val info = mPKDateSource.getPkInfo(mPKSession?.sessionId ?: "")
                    if (info.status == 2) {
                        //todo
                    }
                }
            }
        }
    }

    override fun onRoomLeave() {
        super.onRoomLeave()
        repeatSynchroJob.cancel()
    }

    override fun onRoomClose() {
        super.onRoomClose()
    }

}