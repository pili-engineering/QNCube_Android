package com.qncube.linkmicservice

import com.nucube.rtclive.CameraMergeOption
import com.nucube.rtclive.MicrophoneMergeOption
import com.nucube.rtclive.RtcLiveRoom
import com.qncube.liveroomcore.BaseService
import com.qncube.liveroomcore.Extension
import com.qncube.liveroomcore.QNLiveRoomClient
import com.qncube.liveroomcore.getRtc

class QNAnchorHostMicLinkerImpl(private val context: MicLinkContext) : QNAnchorHostMicLinker,
    BaseService() {

    private var mMixStreamAdapter: QNAnchorHostMicLinker.MixStreamAdapter? = null

    private val mMicLinkerListener = object : QNLinkMicService.MicLinkerListener {

        override fun onInitLinkers(linkers: MutableList<QNMicLinker>) {}
        override fun onUserJoinLink(micLinker: QNMicLinker) {
            if (context.mRtcLiveRoom.mMixStreamManager.mQNMergeJob == null) {
                context.mRtcLiveRoom.mMixStreamManager.startMixStreamJob()
            }
            val ops = mMixStreamAdapter?.onResetMixParam(context.allLinker, micLinker, true)
            ops?.forEach {
                context.mRtcLiveRoom.mMixStreamManager.updateUserAudioMergeOptions(
                    it.uid,
                    it.microphoneMergeOption,
                    false
                )
                context.mRtcLiveRoom.mMixStreamManager.updateUserVideoMergeOptions(
                    it.uid,
                    it.cameraMergeOption,
                    false
                )
            }
            context.mRtcLiveRoom.mMixStreamManager.commitOpt()

        }

        /**
         * 有人下麦
         *
         * @param micLinker
         */
        override fun onUserLeft(micLinker: QNMicLinker) {

            if (context.mRtcLiveRoom.mMixStreamManager
                    .roomUser == 1
            ) {
                context.mRtcLiveRoom.mMixStreamManager.startForwardJob()
                return
            }

            val ops = mMixStreamAdapter?.onResetMixParam(context.allLinker, micLinker, false)
            context.mRtcLiveRoom.mMixStreamManager.updateUserAudioMergeOptions(
                micLinker.user?.userId ?: "",
                MicrophoneMergeOption(),
                false
            )
            context.mRtcLiveRoom.mMixStreamManager.updateUserVideoMergeOptions(
                micLinker.user?.userId ?: "",
                CameraMergeOption(),
                false
            )

            ops?.forEach {
                context.mRtcLiveRoom.mMixStreamManager.updateUserAudioMergeOptions(
                    it.uid,
                    it.microphoneMergeOption,
                    false
                )
                context.mRtcLiveRoom.mMixStreamManager.updateUserVideoMergeOptions(
                    it.uid,
                    it.cameraMergeOption,
                    false
                )
            }
            context.mRtcLiveRoom.mMixStreamManager.commitOpt()
        }

        override fun onUserMicrophoneStatusChange(micLinker: QNMicLinker) {}
        override fun onUserCameraStatusChange(micLinker: QNMicLinker) {}
        override fun onUserBeKick(micLinker: QNMicLinker, msg: String) {}
        override fun onUserExtension(micLinker: QNMicLinker, extension: Extension) {}
    }

    /**
     * 设置混流适配器
     * @param mixStreamAdapter
     */
    override fun setMixStreamAdapter(mixStreamAdapter: QNAnchorHostMicLinker.MixStreamAdapter?) {
        mMixStreamAdapter = mixStreamAdapter
    }

    override fun attachRoomClient(client: QNLiveRoomClient) {
        val field = client.getRtc()
        val room: RtcLiveRoom = field.get(client) as RtcLiveRoom
        context.mMicLinkerListeners.addFirst(mMicLinkerListener)
        context.mRtcLiveRoom = room
        context.mRtcLiveRoom.addExtraQNRTCEngineEventListener(context.mExtQNClientEventListener)
        super.attachRoomClient(client)

    }

    override fun onRoomClose() {
        super.onRoomClose()
    }

}