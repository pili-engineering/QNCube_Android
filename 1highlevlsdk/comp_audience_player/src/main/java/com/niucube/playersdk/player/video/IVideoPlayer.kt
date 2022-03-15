package com.niucube.playersdk.player.video

import android.net.Uri
import com.niucube.playersdk.player.IPlayer
import com.niucube.playersdk.player.video.contronller.IController

interface IVideoPlayer : IPlayer {


    /**
     * @param cover 封面
     * @param preLoading   预加载　　提前异步装载视频　　如果true 装载完成将等待　　startPlay
     */
    fun setUp(
        uir: Uri,
        headers: Map<String, String>? = null,
        cover: Uri,
        preLoading: Boolean = false
    )

    /**
     * 锁定屏幕
     * @return　成功失败
     */
    fun lockScreen(toLock: Boolean): Boolean

    fun isLock(): Boolean

    /**
     * 设置背景
     */
    fun setCover(uir: Uri?)

    /**
     * 添加控制器
     */
    fun addController(controller: IController)

    /**
     * 窗口模式
     */
    fun getPlayerWindowStatus(): Int


    /**
     * 进入全屏模式
     */
    fun enterFullScreen()

    /**
     * 退出全屏模式
     *
     * @return true 退出
     */
    fun exitFullScreen(): Boolean

    /**
     * 进入小窗口模式
     */
    fun enterTinyWindow()

    /**
     * 退出小窗口模式
     *
     * @return true 退出小窗口
     */
    fun exitTinyWindow(): Boolean

    fun isFullScreen(): Boolean
    fun isTinyWindow(): Boolean
    fun isNormal(): Boolean

}