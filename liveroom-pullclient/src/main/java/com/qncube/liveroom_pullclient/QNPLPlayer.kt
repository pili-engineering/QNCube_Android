package com.qncube.liveroom_pullclient

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View
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
    ) {

    }

    /**
     * 开始播放
     * @param roomInfo
     */
    private var lastUrl = ""
    override fun start(roomInfo: QNLiveRoomInfo) {
        lastUrl = roomInfo.rtmpUrl
        setVideoURI(Uri.parse(lastUrl))
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
            setVideoURI(Uri.parse(lastUrl))
            start()
            Log.d("LinkerSlot", " 啦流 开始播放")
        } else {
            stop()
            Log.d("LinkerSlot", " 啦流 停止播放")
        }
    }

    override fun getView(): View {
        return this
    }
}