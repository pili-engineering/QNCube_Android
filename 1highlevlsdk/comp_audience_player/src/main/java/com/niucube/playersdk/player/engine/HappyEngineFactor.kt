package com.niucube.playersdk.player.engine

import android.content.Context
import com.niucube.playersdk.player.AbsPlayerEngine

object HappyEngineFactor {

    fun newPlayer(context: Context, engineType: EngineType): AbsPlayerEngine {
        return when (engineType) {
            EngineType.QN_PLAYER -> {
                PLEngine(context)
            }
            EngineType.MEDIA_PLAYER -> {
                NativeEngine(context)
            }
        }

    }

}