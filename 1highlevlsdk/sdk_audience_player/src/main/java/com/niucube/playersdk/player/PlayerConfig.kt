package com.niucube.playersdk.player

import com.pili.pldroid.player.AVOptions


class PlayerConfig {


    /**
     * 循环播放
     */
    internal var loop = false

    /**
     * 从上一次的位置继续播放
     */
    internal var isFromLastPosition = false

    fun setLoop(loop: Boolean): PlayerConfig {
        this.loop = loop
        return this
    }

    fun setFromLastPosition(fromLastPosition: Boolean): PlayerConfig {
        this.isFromLastPosition = fromLastPosition
        return this
    }

    var avOptions: AVOptions?=null
    fun setAVOptions(avOptions: AVOptions):PlayerConfig{
        this.avOptions =avOptions
        return this
    }
}