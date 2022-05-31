package com.qbcube.pkservice

import android.text.TextUtils
import com.niucube.rtm.*
import com.niucube.rtm.msg.RtmTextMsg
import com.nucube.rtclive.CameraMergeOption
import com.nucube.rtclive.DefaultExtQNClientEventListener
import com.nucube.rtclive.MicrophoneMergeOption
import com.nucube.rtclive.RtcLiveRoom
import com.qiniu.droid.rtc.*
import com.qiniu.jsonutil.JsonUtils
import com.qncube.liveroomcore.*
import com.qncube.liveroomcore.datasource.UserDataSource
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.liveroomcore.mode.QNLiveUser
import com.qncube.rtcexcepion.RtcException
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class QNPKServiceImpl : QNPKService, BaseService() {

    companion object {
        val liveroom_pk_start = "liveroom_pk_start"
        val liveroom_pk_stop = "liveroom_pk_stop"

        val PK_STATUS_OK = 1
    }

    private val mPKDateSource = PKDateSource()
    private val pkPKInvitationHandlerImpl: QNPKInvitationHandlerImpl = QNPKInvitationHandlerImpl()
    private val pkServiceListeners = LinkedList<QNPKService.PKServiceListener>()
    private var mPKMixStreamAdapter: QNPKService.PKMixStreamAdapter? = null
    private val mAudiencePKSynchro = AudiencePKSynchro()
    var mPKSession: QNPKSession? = null
        private set

    private var mPKSessionTemp: QNPKSession? = null
    private var trackUidTemp = ""
    private fun checkReceivePk(pkTemp: QNPKSession?, uidTem: String) {
        if (pkTemp != null) {
            mPKSessionTemp = pkTemp
        } else {
            return
        }
        if (!TextUtils.isEmpty(uidTem)) {
            trackUidTemp = uidTem
        }
        if (mPKSessionTemp != null && !TextUtils.isEmpty(trackUidTemp) &&
            mPKSessionTemp?.initiator?.userId == trackUidTemp

        ) {
            //开始收到pk
            backGround {
                doWork {
                   val pkOutline= mPKDateSource.recevPk(mPKSession?.sessionId?:"")

                    val field = client!!.getRtc()
                    val room: RtcLiveRoom = field.get(client) as RtcLiveRoom
                    //转发
                    val sourceInfo = QNMediaRelayInfo(room.roomName, room.roomToken)
                    val configuration = QNMediaRelayConfiguration(sourceInfo)

                    val json = JSONObject(pkOutline.relay_token)
                    val peerRoomName = json.optString("roomName")
                    val destInfo1 = QNMediaRelayInfo(peerRoomName, pkOutline.relay_token)
                    configuration.addDestRoomInfo(destInfo1)
                    startMediaRelay(peerRoomName, room.mClient, configuration)

                    //pk 接收方收到 邀请方
                    mPKSession = mPKSessionTemp
                    mPKSession?.status = PK_STATUS_OK
                    try {
                        mPKDateSource.ackACKPk(mPKSession?.sessionId?:"")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    try {
                        //群信号
                        RtmManager.rtmClient.sendChannelMsg(
                            RtmTextMsg<QNPKSession>(
                                liveroom_pk_start,
                                mPKSession
                            ).toJsonString(), roomInfo!!.chatId, false
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    pkServiceListeners.forEach {
                        it.onStart(mPKSession!!)
                    }
                    //混流
                    resetMixStream(mPKSession?.initiator?.userId ?: "")
                    QNLiveLogUtil.LogE("pk 接收方确认回复pk成功 ")
                }
                catchError {
                    QNLiveLogUtil.LogE("pk 接收方确认回复pk 错误 ${it.getCode()} ${it.message}")
                }
                onFinally {
                    mPKSessionTemp = null
                    trackUidTemp = ""
                }
            }
        }
    }

    private val mC2cListener = object : RtmMsgListener {
        override fun onNewMsg(msg: String, fromId: String, toId: String): Boolean {
            if (msg.optAction() == liveroom_pk_start) {
                QNLiveLogUtil.LogE("pk 接收方收到pk holle ")
                val pk =
                    JsonUtils.parseObject(msg.optData(), QNPKSession::class.java) ?: return true
                checkReceivePk(pk, "")
            }
            return false
        }
    }

    private val groupListener = object : RtmMsgListener {
        override fun onNewMsg(msg: String, fromId: String, toId: String): Boolean {
            when (msg.optAction()) {
                liveroom_pk_start -> {
                    val pk =
                        JsonUtils.parseObject(msg.optData(), QNPKSession::class.java) ?: return true
                    pkServiceListeners.forEach {
                        it.onStart(pk)
                    }
                }
                liveroom_pk_stop -> {
                    val pk =
                        JsonUtils.parseObject(msg.optData(), QNPKSession::class.java) ?: return true
                    pkServiceListeners.forEach {
                        it.onStart(pk)
                    }

                }
            }
            return false
        }
    }

    private var timeoutJob: Job? = null
    private fun startTimeOutJob(timeoutTimestamp: Long) {
        timeoutJob = GlobalScope.launch(Dispatchers.Main) {
            try {

                delay(timeoutTimestamp)
                QNLiveLogUtil.LogE("pk 邀请方等待超时 ")
                if (mPKSession == null) {
                    return@launch
                }
                pkServiceListeners.forEach {
                    it.onWaitPeerTimeOut(mPKSession!!)
                }
                try {

                    mPKDateSource.stopPk(mPKSession?.sessionId?:"")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mPKSession = null
                stopMediaRelay()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val defaultExtQNClientEventListener = object : DefaultExtQNClientEventListener {
        override fun onUserJoined(p0: String, p1: String?) {
            super.onUserJoined(p0, p1)
            if (p0 == mPKSession?.receiver?.userId) {
                QNLiveLogUtil.LogE("pk 邀请方收到对方流 ")
                // 邀请放 收到 接收放确认了
                backGround {
                    doWork {
                        timeoutJob?.cancel()
                        mPKSession?.status = PK_STATUS_OK

                        try {
                            //群信号
                            RtmManager.rtmClient.sendChannelMsg(
                                RtmTextMsg<QNPKSession>(
                                    liveroom_pk_start,
                                    mPKSession
                                ).toJsonString(), roomInfo!!.chatId, false
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        pkServiceListeners.forEach {
                            it.onStart(mPKSession!!)
                        }
                        //混流
                        resetMixStream(mPKSession?.receiver?.userId ?: "")

                    }
                    catchError {}
                }
            } else {
                QNLiveLogUtil.LogE("pk 接收方收到对方流 ")
                checkReceivePk(null, p0)
            }
        }

        override fun onUserLeft(p0: String) {
            super.onUserLeft(p0)
            if (mPKSession == null) {
                return
            }
            if (p0 == mPKSession?.receiver?.userId) {
                QNLiveLogUtil.LogE("pk 对方离开房间 ")
                loopStop(mPKSession!!.initiator!!.userId)
            }

            if (p0 === mPKSession?.initiator?.userId) {
                QNLiveLogUtil.LogE("pk 对方离开房间 ")
                loopStop(mPKSession!!.receiver!!.userId)
            }
        }
    }

    private fun loopStop(peerId: String) {
        backGround {
            doWork {
                var report = false
                var repCount = 0
                do {
                    try {
                        mPKDateSource.stopPk(mPKSession?.sessionId ?: "")
                        report = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        QNLiveLogUtil.LogE("pk 对方离开房间 上报结束失败 ${e.message} ")
                    }
                    repCount++
                    if (!report) {
                        delay(200)
                    } else {
                        QNLiveLogUtil.LogE("pk 对方离开房间 上报结束 成功")
                    }
                } while (!report || repCount < 10)

                stopMediaRelay()
                try {
                    RtmManager.rtmClient.sendChannelMsg(
                        RtmTextMsg<QNPKSession>(
                            liveroom_pk_stop,
                            mPKSession
                        ).toJsonString(), roomInfo!!.chatId, false
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    QNLiveLogUtil.LogE("pk 结束信令发送失败 ")
                }
                if (mPKSession == null) {
                    return@doWork
                }
                pkServiceListeners.forEach {
                    it.onStop(mPKSession!!, 1, "peer stop")
                }
                val peer = if (mPKSession!!.initiator.userId == user?.userId) {
                    mPKSession!!.receiver.userId
                } else {
                    mPKSession!!.initiator.userId
                }
                mPKSession = null

                resetMixStream(peer)
            }
        }
    }

    private fun resetMixStream(peerId: String) {
        if (mPKMixStreamAdapter == null) {
            return
        }
        val field = client!!.getRtc()
        val mRtcLiveRoom: RtcLiveRoom = field.get(client) as RtcLiveRoom

        if (mPKSession != null) {
            if (mRtcLiveRoom.mMixStreamManager.mQNMergeJob == null) {
                mRtcLiveRoom.mMixStreamManager.startNewMixStreamJob(
                    mPKMixStreamAdapter!!.onPKMixStreamStart(
                        mPKSession!!
                    )
                )
            }
            val ops = mPKMixStreamAdapter?.onPKLinkerJoin(mPKSession!!)
            ops?.forEach {
                mRtcLiveRoom.mMixStreamManager.updateUserAudioMergeOptions(
                    it.uid,
                    it.microphoneMergeOption,
                    false
                )
                mRtcLiveRoom.mMixStreamManager.updateUserVideoMergeOptions(
                    it.uid,
                    it.cameraMergeOption,
                    false
                )
            }
            mRtcLiveRoom.mMixStreamManager.commitOpt()
        } else {

            if (mRtcLiveRoom.mMixStreamManager
                    .roomUser == 1
            ) {
                mRtcLiveRoom.mMixStreamManager.startForwardJob()
                return
            } else {
                mRtcLiveRoom.mMixStreamManager.stopMixStreamJob()
                mRtcLiveRoom.mMixStreamManager.startMixStreamJob()
            }

            mRtcLiveRoom.mMixStreamManager.updateUserAudioMergeOptions(
                peerId,
                MicrophoneMergeOption(),
                false
            )
            mRtcLiveRoom.mMixStreamManager.updateUserVideoMergeOptions(
                peerId,
                CameraMergeOption(),
                false
            )

            val ops = mPKMixStreamAdapter?.onPKLinkerLeft()
            ops?.forEach {
                mRtcLiveRoom.mMixStreamManager.updateUserAudioMergeOptions(
                    it.uid,
                    it.microphoneMergeOption,
                    false
                )
                mRtcLiveRoom.mMixStreamManager.updateUserVideoMergeOptions(
                    it.uid,
                    it.cameraMergeOption,
                    false
                )
            }
            mRtcLiveRoom.mMixStreamManager.commitOpt()
        }
    }

    override fun attachRoomClient(client: QNLiveRoomClient) {
        super.attachRoomClient(client)
        RtmManager.addRtmC2cListener(mC2cListener)
        RtmManager.addRtmChannelListener(groupListener)

        if (client.clientType == ClientType.CLIENT_PUSH) {
            val field = client!!.getRtc()
            val room: RtcLiveRoom = field.get(client) as RtcLiveRoom
            room.addExtraQNRTCEngineEventListener(defaultExtQNClientEventListener)
            pkPKInvitationHandlerImpl.attachRoomClient(client)
        } else {
            mAudiencePKSynchro.attachRoomClient(client)
        }
    }

    override fun onRoomClose() {
        super.onRoomClose()
        RtmManager.removeRtmC2cListener(mC2cListener)
        RtmManager.removeRtmChannelListener(groupListener)

        if (client?.clientType == ClientType.CLIENT_PUSH) {
            pkPKInvitationHandlerImpl.onRoomClose()
        } else {
            mAudiencePKSynchro.onRoomClose()
        }
    }

    override fun onRoomEnter(roomId: String, user: QNLiveUser) {
        super.onRoomEnter(roomId, user)
        if (client?.clientType == ClientType.CLIENT_PUSH) {
            pkPKInvitationHandlerImpl.onRoomEnter(roomId, user)
        } else {
            mAudiencePKSynchro.onRoomEnter(roomId, user)
        }
    }

    override fun onRoomLeave() {
        super.onRoomLeave()
        if (client?.clientType == ClientType.CLIENT_PUSH) {
            pkPKInvitationHandlerImpl.onRoomLeave()
        } else {
            mAudiencePKSynchro.onRoomLeave()
        }
    }

    override fun onRoomJoined(roomInfo: QNLiveRoomInfo) {
        super.onRoomJoined(roomInfo)

        if (client?.clientType == ClientType.CLIENT_PUSH) {
            pkPKInvitationHandlerImpl.onRoomJoined(roomInfo)
        } else {
            mAudiencePKSynchro.onRoomJoined(roomInfo)
        }
    }

    /**
     * 设置混流适配器
     * @param adapter
     */
    override fun setPKMixStreamAdapter(adapter: QNPKService.PKMixStreamAdapter?) {
        mPKMixStreamAdapter = adapter
    }

    override fun addPKServiceListener(pkServiceListener: QNPKService.PKServiceListener) {
        pkServiceListeners.add(pkServiceListener)
    }

    override fun removePKServiceListener(pkServiceListener: QNPKService.PKServiceListener) {
        pkServiceListeners.remove(pkServiceListener)
    }

    /**
     * 跟新扩展自定义字段
     *
     * @param extension
     */
    override fun upDataPKExtension(extension: Extension, callBack: QNLiveCallBack<Void>?) {
    }

    override fun start(
        timeoutTimestamp: Long,
        receiverRoomId: String,
        receiverUid: String,
        extensions: HashMap<String, String>?,
        callBack: QNLiveCallBack<QNPKSession>?
    ) {
        if (roomInfo == null) {
            callBack?.onError(0, " roomInfo==null")
            return
        }
        backGround {
            doWork {
                val pkOutline = mPKDateSource.startPk(receiverRoomId, receiverUid)
                val receiver =
                    UserDataSource().searchUserByIMUid(receiverUid)

                val pkSession = QNPKSession()
                pkSession.extensions = extensions
                pkSession.initiator = user
                pkSession.initiatorRoomId = roomInfo?.liveId
                pkSession.receiver = receiver
                pkSession.receiverRoomId = receiverRoomId
                pkSession.sessionId = pkOutline.relay_id
                pkSession.status = pkOutline.relay_status

                mPKSession = pkSession

                //发c2c消息
                RtmManager.rtmClient.sendC2cMsg(
                    RtmTextMsg<QNPKSession>(
                        liveroom_pk_start,
                        mPKSession
                    ).toJsonString(), receiver.imUid, false
                )
                val field = client!!.getRtc()
                val room: RtcLiveRoom = field.get(client) as RtcLiveRoom
                //转发
                val sourceInfo = QNMediaRelayInfo(room.roomName, room.roomToken)
                val configuration = QNMediaRelayConfiguration(sourceInfo)
                val json = JSONObject(pkOutline.relay_token)
                val peerRoomName = json.optString("roomName")
                val destInfo1 = QNMediaRelayInfo(peerRoomName, pkOutline.relay_token)
                configuration.addDestRoomInfo(destInfo1)

                startMediaRelay(peerRoomName, room.mClient, configuration)

                mPKDateSource.ackACKPk(mPKSession?.sessionId ?: "")

                startTimeOutJob(timeoutTimestamp)
                callBack?.onSuccess(pkSession)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    private suspend fun startMediaRelay(
        peerRoomName: String,
        client: QNRTCClient,
        configuration: QNMediaRelayConfiguration
    ) = suspendCoroutine<Unit> { continuation ->
        client.startMediaRelay(configuration, object : QNMediaRelayResultCallback {
            override fun onResult(p0: MutableMap<String, QNMediaRelayState>) {
                if (p0[peerRoomName] == QNMediaRelayState.SUCCESS) {
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(
                        RtcException(
                            -1,
                            "pk startMediaRelay" + p0[peerRoomName]?.name
                        )
                    )
                }
            }

            override fun onError(p0: Int, p1: String) {
                continuation.resumeWithException(RtcException(p0, p1))
            }
        })
    }

    private suspend fun stopMediaRelay() =
        suspendCoroutine<Unit> { continuation ->
            val field = client!!.getRtc()
            val room: RtcLiveRoom = field.get(client) as RtcLiveRoom
            room.mClient.stopMediaRelay(object : QNMediaRelayResultCallback {
                override fun onResult(p0: MutableMap<String, QNMediaRelayState>) {
                    continuation.resume(Unit)
                }

                override fun onError(p0: Int, p1: String) {
                    continuation.resumeWithException(RtcException(p0, p1))
                }
            }
            )
        }

    override fun stop(callBack: QNLiveCallBack<Void>?) {
        if (roomInfo == null || mPKSession?.status != PK_STATUS_OK) {
            callBack?.onError(0, " roomInfo==null")
            return
        }
        backGround {
            doWork {

                mPKDateSource.stopPk(mPKSession?.sessionId ?: "")
                stopMediaRelay()
                try {
                    RtmManager.rtmClient.sendChannelMsg(
                        RtmTextMsg<QNPKSession>(
                            liveroom_pk_stop,
                            mPKSession
                        ).toJsonString(), roomInfo!!.chatId, false
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    QNLiveLogUtil.LogE("pk 结束信令发送失败 ")
                }
                pkServiceListeners.forEach {
                    it.onStop(mPKSession!!, 0, "positive stop")
                }
                val peer = if (mPKSession!!.initiator.userId == user?.userId) {
                    mPKSession!!.receiver.userId
                } else {
                    mPKSession!!.initiator.userId
                }
                mPKSession = null
                callBack?.onSuccess(null)
                resetMixStream(peer)
            }
            catchError {
                callBack?.onError(it.getCode(), it.message)
            }
        }
    }

    /**
     * 设置某人的连麦预览
     *
     * @param uid  麦上用户ID
     * @param view
     */
    override fun setPeerAnchorPreView(uid: String, view: QNRenderView) {
        val field = client!!.getRtc()
        val room: RtcLiveRoom = field.get(client) as RtcLiveRoom
        room.setUserCameraWindowView(uid, view)
    }

    /**
     * 获得pk邀请处理
     *
     * @return
     */
    override fun getPKInvitationHandler(): QNPKInvitationHandler {
        return pkPKInvitationHandlerImpl
    }

}