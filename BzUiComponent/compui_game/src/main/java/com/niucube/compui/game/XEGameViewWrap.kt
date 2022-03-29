package com.niucube.compui.game

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import com.momo.xeengine.IXEngine
import com.momo.xeengine.XEnginePreferences
import com.momo.xeengine.game.IXGameView
import com.momo.xeengine.game.XEGameView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Dispatcher

class XEGameViewWrap {

    companion object {
        private var isInit = false
        fun init(application: Context) {
            if (isInit) {
                return
            }
            isInit = true
            //设置引擎的上下文
            //设置引擎的上下文
            XEnginePreferences.setApplicationContext(application)
            //设置引擎license 与包名绑定，请联系陌陌获得。
            //设置引擎license 与包名绑定，请联系陌陌获得。
            XEnginePreferences.setLicense("DLaBTUcMwKejWMAQNW2JYH5tUMyGCg2ARB0fsg2utJCf/lMU5TJl9JAMFHWsQgBPV7J/7Y5MlAy19vrVJEG2O7IL+ko49OQ+LGX9tWUM7E18DwZJ6Ys6qoLJ0iG5BbRLbPmDwWrdy437o/BVkstHFUwkpH4kJ2LZpzSuE7kALxU=")
            //MainActivity.verifyStoragePermissions(this)
        }
    }

    var mGameUICall: GameUICall? = null
    lateinit var mXEGameView: XEGameView
    private var gameHandler: GameHandler? = null

    var isStart = false
        private set

    inner class GameHandler() {
        fun removeGame(msg: String): String? {
            isStart = false
            GlobalScope.launch(Dispatchers.Main) {
                //  mXEGameView.visibility = View.GONE
                mGameUICall?.onStop()
                mXEGameView.stop()
            }
            return null
        }
    }

    fun attach(xEGameView: XEGameView, activity: Activity) {
        mXEGameView = xEGameView
        gameHandler = GameHandler()
        mXEGameView.setRenderViewType(XEGameView.TYPE_TEXTURE_VIEW)
        mXEGameView.renderScale = 1f
        mXEGameView.preferredFramesPerSecond = 30
        mXEGameView.setCallback(object : IXGameView.Callback {
            override fun onRenderViewCreate(p0: View?) {}

            override fun onStart(engine: IXEngine) {
                isStart = true
                mGameUICall?.start()
                //  mXEGameView.visibility = View.VISIBLE
                engine.logger.setLogEnable(true)
                val gameDir = GameFileUtils.copyFileIfNeed(activity, "GameRes")
                engine.addLibraryPath(gameDir)
                engine.scriptBridge.regist(gameHandler, "LiveGameHandler")
                engine.scriptEngine.startGameScriptFile("app")
            }

            override fun onStartFailed(p0: String?) {
                Log.d("mXEGameView", "onStartFailed ${p0}")
            }

            override fun onRenderSizeChanged(p0: Int, p1: Int) {}

            override fun onEngineDynamicLinkLibraryDownloadProcess(p0: Int, p1: Double) {}
        })
    }

    fun start() {
        mXEGameView.start()
    }

    fun stop() {
        mXEGameView.stop()
    }

    interface GameUICall {
        fun start()
        fun onStop()
    }
}