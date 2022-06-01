package com.qncube.liveroom_pullclient

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import com.pili.pldroid.player.widget.PLVideoTextureView
import com.qncube.liveroomcore.ClientRoleType
import com.qncube.liveroomcore.IPullPlayer
import com.qncube.liveroomcore.mode.QNLiveRoomInfo

class QNPLPlayer : PLVideoTextureView, IPullPlayer {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){

    }

    /**
     * 开始播放
     * @param roomInfo
     */
    override fun start(roomInfo: QNLiveRoomInfo) {
        setVideoURI(Uri.parse(roomInfo.rtmpUrl))
        start()
    }

    override fun stopPlay() {
        stop()
    }

    /**
     * 角色变化
     * @param roleType
     */
    override fun changeClientRole(roleType: ClientRoleType) {
        if (roleType == ClientRoleType.ROLE_PULL) {
            start()
        } else {
            pause()
        }
    }
}