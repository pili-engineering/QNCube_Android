package com.qncube.linkmicservice

import com.qncube.liveroomcore.QNLiveCallBack

/**
 * 主播跨房连麦器
 */
public interface QNAnchorForwardMicLinker {

    /**
     * 开始跨房上麦
     * @param  linker
     * @param callBack 上麦成功失败回调
     */
    fun startLink(
        peerRoomId: String,
        extensions: HashMap<String, String>? = null,
        callBack: QNLiveCallBack<Void>
    )

    /**
     * 结束连麦
     */
    fun stopLink();
}
