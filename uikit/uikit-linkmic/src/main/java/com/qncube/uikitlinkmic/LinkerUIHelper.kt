package com.qncube.uikitlinkmic

import com.nucube.rtclive.CameraMergeOption
import com.nucube.rtclive.MicrophoneMergeOption
import com.nucube.rtclive.QNMergeOption
import com.qncube.linkmicservice.QNMicLinker
import com.qncube.liveroomcore.mode.QNLiveRoomInfo

object LinkerUIHelper {

    val mixWidth = 720
    val mixHeight = 1280

    var mixMicWidth = 184
    var mixMicHeight = 184

    var uiMicWidth = 0
    var uiMicHeight = 0

    var mixTopMargin = 174
    var uiTopMargin = 0

    /**
     * 混流参数 每个麦位间距
     */
    var micBottomMixMargin = 15

    /**
     * 混流换算成屏幕 每个麦位的间距
     */
    var micBottomUIMargin = 0

    /**
     * 混流参数 每个麦位右间距
     */
    var micRightMixMargin = 30*3

    /**
     * UI
     */
    var micRightUIMargin = 0

    var containerWidth = 0
    var containerHeight = 0


    fun attachUIWidth(w: Int, h: Int, centerCrop: Boolean = true) {
        containerWidth = w
        containerHeight = h

        val uiRatio = h.toDouble() / w
        val mixRatio = mixHeight.toDouble() / mixWidth

        var ratio = 0.0
        if (mixRatio > uiRatio) {
            //视频太高
            ratio = containerWidth.toDouble() / mixWidth

            uiMicWidth = (mixMicWidth * ratio).toInt()
            uiMicHeight = (mixMicHeight * ratio).toInt()
            uiTopMargin = (mixTopMargin * ratio - (mixHeight * ratio - containerHeight) / 2).toInt()
            micBottomUIMargin = (micBottomMixMargin * ratio).toInt()
            micRightUIMargin = (micRightMixMargin * ratio).toInt()

        } else {
            //视频太矮
            ratio = containerHeight.toDouble() / mixHeight

            uiMicWidth = (mixMicWidth * ratio).toInt()
            uiMicHeight = (mixMicHeight * ratio).toInt()
            uiTopMargin = (mixTopMargin * ratio).toInt()
            micBottomUIMargin = (micBottomMixMargin * ratio).toInt()
            micRightUIMargin = 0
        }
    }


    fun getLinkers(micLinkers : List<QNMicLinker>,roomInfo: QNLiveRoomInfo): ArrayList<QNMergeOption> {
        val ops = ArrayList<QNMergeOption>()
        var lastX =
            LinkerUIHelper.mixWidth - LinkerUIHelper.mixMicWidth - LinkerUIHelper.micRightMixMargin


        var lastY = LinkerUIHelper.mixTopMargin
        micLinkers.forEach {
            if (it.user.userId == roomInfo?.anchorInfo?.userId) {
                ops.add(QNMergeOption().apply {
                    uid = it.user.userId
                    cameraMergeOption = CameraMergeOption().apply {
                        isNeed = true
                        mX = 0
                        mY = 0
                        mZ = 0
                        mWidth = LinkerUIHelper.mixWidth
                        mHeight = LinkerUIHelper.mixHeight
                        // mStretchMode=QNRenderMode.
                    }
                    microphoneMergeOption = MicrophoneMergeOption().apply {
                        isNeed = true
                    }
                })
            } else {
                ops.add(QNMergeOption().apply {
                    uid = it.user.userId
                    cameraMergeOption = CameraMergeOption().apply {
                        isNeed = true
                        mX = lastX
                        mY = lastY
                        mZ = 1
                        mWidth = LinkerUIHelper.mixMicWidth
                        mHeight = LinkerUIHelper.mixMicHeight
                        // mStretchMode=QNRenderMode.
                    }
                    lastY += LinkerUIHelper.micBottomMixMargin + LinkerUIHelper.mixMicHeight
                    microphoneMergeOption = MicrophoneMergeOption().apply {
                        isNeed = true
                    }
                })
            }
        }
       return ops

    }

}