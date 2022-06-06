package com.qncube.chatservice

import com.niucube.rtm.*
import com.qiniu.droid.imsdk.QNIMClient
import com.qncube.liveroomcore.*
import im.floo.floolib.BMXErrorCode
import im.floo.floolib.BMXGroup
import im.floo.floolib.BMXGroupServiceListener
import im.floo.floolib.ListOfLongLong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class QNChatRoomServiceImpl : BaseService(), QNChatRoomService {

    private val mC2CRtmMsgListener = object : RtmMsgListener {
        /**
         * 收到消息
         * @return 是否继续分发
         */
        override fun onNewMsg(msg: String, fromId: String, toId: String): Boolean {
            return if (msg.optAction() == "") {
                mChatServiceListeners.forEach {
                    it.onReceivedC2CMsg(msg, fromId, toId)
                }
                true
            } else {
                false
            }
        }
    }

    private val mGroupRtmMsgListener = object : RtmMsgListener {
        /**
         * 收到消息
         * @return 是否继续分发
         */
        override fun onNewMsg(msg: String, fromId: String, toId: String): Boolean {
            return if (msg.optAction() == "") {
                mChatServiceListeners.forEach {
                    it.onReceivedGroupMsg(msg, fromId, toId)
                }
                true
            } else {
                false
            }
        }
    }

    private val mBMXGroupServiceListener = object : BMXGroupServiceListener() {
        override fun onAdminsAdded(group: BMXGroup, members: ListOfLongLong) {
            super.onAdminsAdded(group, members)
            if (group.groupId().toString() != roomInfo?.chatId) {
                return
            }
            for (i in 0 until members.size().toInt()) {
                val id = members[i]
                GlobalScope.launch(Dispatchers.Main) {
                    try {
                        mChatServiceListeners.forEach {
                            it.onAdminAdd(id.toString())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        override fun onAdminsRemoved(group: BMXGroup, members: ListOfLongLong, reason: String?) {
            super.onAdminsRemoved(group, members, reason)
            if (group.groupId().toString() != roomInfo?.chatId) {
                return
            }
            for (i in 0 until members.size().toInt()) {
                val id = members[i]
                GlobalScope.launch(Dispatchers.Main) {
                    try {
                        mChatServiceListeners.forEach {
                            it.onAdminRemoved(id.toString(), reason ?: "")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        override fun onMemberLeft(group: BMXGroup, memberId: Long, reason: String?) {
            super.onMemberLeft(group, memberId, reason)
            if (group.groupId().toString() != roomInfo?.chatId) {
                return
            }
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    mChatServiceListeners.forEach {
                        it.onUserLevel(memberId.toString())
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }

        override fun onMemberJoined(group: BMXGroup, memberId: Long, inviter: Long) {
            super.onMemberJoined(group, memberId, inviter)
            if (group.groupId().toString() != roomInfo?.chatId) {
                return
            }

            GlobalScope.launch(Dispatchers.Main) {
                try {
                    mChatServiceListeners.forEach {
                        it.onUserJoin(memberId.toString())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        override fun onMembersBanned(group: BMXGroup, members: ListOfLongLong, duration: Long) {
            super.onMembersBanned(group, members, duration)
            if (group.groupId().toString() != roomInfo?.chatId) {
                return
            }
            for (i in 0 until members.size().toInt()) {
                val id = members[i]

                GlobalScope.launch(Dispatchers.Main) {
                    try {
                        mChatServiceListeners.forEach {
                            it.onUserBeMuted(true, id.toString(), duration)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            }
        }

        override fun onMembersUnbanned(group: BMXGroup, members: ListOfLongLong) {
            super.onMembersUnbanned(group, members)
            if (group.groupId().toString() != roomInfo?.chatId) {
                return
            }
            for (i in 0 until members.size().toInt()) {
                val id = members[i]

                GlobalScope.launch(Dispatchers.Main) {
                    try {
                        mChatServiceListeners.forEach {
                            it.onUserBeMuted(false, id.toString(),0)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            }
        }
    }

    private val mChatServiceListeners = ArrayList<QNChatRoomServiceListener>()
    override fun addChatServiceListener(chatServiceListener: QNChatRoomServiceListener) {
        mChatServiceListeners.add(chatServiceListener)
    }

    override fun removeChatServiceListener(chatServiceListener: QNChatRoomServiceListener) {
        mChatServiceListeners.remove(chatServiceListener)
    }

    override fun attachRoomClient(client: QNLiveRoomClient) {
        super.attachRoomClient(client)
        QNIMClient.getGroupManager().addGroupListener(mBMXGroupServiceListener)
        RtmManager.addRtmC2cListener(mC2CRtmMsgListener)
        RtmManager.addRtmChannelListener(mGroupRtmMsgListener)

    }

    override fun onRoomClose() {
        super.onRoomClose()
        QNIMClient.getGroupManager().removeGroupListener(mBMXGroupServiceListener)
        RtmManager.removeRtmC2cListener(mC2CRtmMsgListener)
        RtmManager.removeRtmChannelListener(mGroupRtmMsgListener)
    }

    /**
     * 发c2c消息
     * @param msg
     * @param memberId
     * @param callBack
     */
    override fun sendCustomC2CMsg(
        msg: String,
        memberId: String,
        callBack: QNLiveCallBack<Void>?
    ) {
        RtmManager.rtmClient.sendC2cMsg(msg, memberId, true, object : RtmCallBack {
            override fun onSuccess() {
                callBack?.onSuccess(null)
            }

            override fun onFailure(code: Int, msg: String) {
                callBack?.onError(code, msg)
            }
        })
    }

    /**
     * 发群消息
     * @param msg
     * @param callBack
     */
    override fun sendCustomGroupMsg(msg: String, callBack: QNLiveCallBack<Void>?) {
        RtmManager.rtmClient.sendChannelMsg(
            msg,
            roomInfo?.chatId ?: "",
            true,
            object : RtmCallBack {
                override fun onSuccess() {
                    callBack?.onSuccess(null)
                }

                override fun onFailure(code: Int, msg: String) {
                    callBack?.onError(code, msg)
                }
            })
    }

    /**
     * 踢人
     * @param msg
     * @param memberId
     * @param callBack
     */
    override fun kickUser(msg: String, memberId: String, callBack: QNLiveCallBack<Void>?) {
        QNIMClient.getGroupManager().getGroupList(
            roomInfo?.chatId?.toLong() ?: 0L, true
        ) { code, data ->
            if (code == BMXErrorCode.NoError) {
                QNIMClient.getGroupManager()
                    .removeMembers(
                        data, ListOfLongLong().apply { add(memberId.toLong()) }, msg
                    ) {
                        if (it == BMXErrorCode.NoError) {
                            callBack?.onSuccess(null)
                        } else {
                            callBack?.onError(it.swigValue(), it.name)
                        }
                    }
            } else {
                callBack?.onError(code.swigValue(), code.name)
            }
        }

    }

    /**
     * 禁言
     * @param isMute
     * @param msg
     * @param memberId
     * @param duration
     * @param callBack
     */
    override fun muteUser(
        isMute: Boolean,
        msg: String,
        memberId: String,
        duration: Long,
        callBack: QNLiveCallBack<Void>?
    ) {

        QNIMClient.getGroupManager().getGroupList(
            roomInfo?.chatId?.toLong() ?: 0L, true
        ) { code, data ->
            if (code == BMXErrorCode.NoError) {
                if (isMute) {
                    QNIMClient.getGroupManager()
                        .banMembers(
                            data, ListOfLongLong().apply { add(memberId.toLong()) }, duration, msg
                        ) {
                            if (it == BMXErrorCode.NoError) {
                                callBack?.onSuccess(null)
                            } else {
                                callBack?.onError(it.swigValue(), it.name)
                            }
                        }
                } else {
                    QNIMClient.getGroupManager()
                        .unbanMembers(
                            data, ListOfLongLong().apply { add(memberId.toLong()) }
                        ) {
                            if (it == BMXErrorCode.NoError) {
                                callBack?.onSuccess(null)
                            } else {
                                callBack?.onError(it.swigValue(), it.name)
                            }
                        }
                }

            } else {
                callBack?.onError(code.swigValue(), code.name)
            }
        }
    }

    /**
     * 添加管理员
     * @param memberId
     * @param callBack
     */
    override fun addAdmin(memberId: String, callBack: QNLiveCallBack<Void>?) {
        QNIMClient.getGroupManager().getGroupList(
            roomInfo?.chatId?.toLong() ?: 0L, true
        ) { code, data ->
            if (code == BMXErrorCode.NoError) {
                QNIMClient.getGroupManager()
                    .addAdmins(
                        data, ListOfLongLong().apply { add(memberId.toLong()) }, ""
                    ) {
                        if (it == BMXErrorCode.NoError) {
                            callBack?.onSuccess(null)
                        } else {
                            callBack?.onError(it.swigValue(), it.name)
                        }
                    }
            } else {
                callBack?.onError(code.swigValue(), code.name)
            }
        }
    }

    /**
     * 移除管理员
     * @param msg
     * @param memberId
     * @param callBack
     */
    override fun removeAdmin(msg: String, memberId: String, callBack: QNLiveCallBack<Void>?) {
        QNIMClient.getGroupManager().getGroupList(
            roomInfo?.chatId?.toLong() ?: 0L, true
        ) { code, data ->
            if (code == BMXErrorCode.NoError) {
                QNIMClient.getGroupManager()
                    .removeAdmins(
                        data, ListOfLongLong().apply { add(memberId.toLong()) }, msg
                    ) {
                        if (it == BMXErrorCode.NoError) {
                            callBack?.onSuccess(null)
                        } else {
                            callBack?.onError(it.swigValue(), it.name)
                        }
                    }
            } else {
                callBack?.onError(code.swigValue(), code.name)
            }
        }
    }

}