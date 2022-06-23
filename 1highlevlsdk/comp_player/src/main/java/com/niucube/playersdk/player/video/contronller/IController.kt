package com.niucube.playersdk.player.video.contronller

import android.view.View
import com.niucube.playersdk.player.PlayerStatusListener
import com.niucube.playersdk.player.video.IVideoPlayer

interface IController : PlayerStatusListener {



    fun getView(): View
    fun attach(player: IVideoPlayer){}
    fun detach()
    /**
     * 重置控制器，将控制器恢复到初始状态。
     */
    fun reset()
}