package com.niucube.playersdk.player

interface PlayerStatusListener {


    fun onPlayStateChanged(status:Int)

    fun onPlayModeChanged(model:Int)

}