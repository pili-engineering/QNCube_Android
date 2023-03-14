package com.qiniu.qnim

import android.content.Context
import android.os.Environment
import android.util.Log
import com.niucube.rtm.RtmManager
import im.floo.floolib.BMXClientType
import im.floo.floolib.BMXLogLevel
import im.floo.floolib.BMXPushEnvironmentType
import im.floo.floolib.BMXSDKConfig
import java.io.File

object QNIMManager {

    val mRtmAdapter by lazy { QNIMAdapter() }

    private fun getFilesPath(context: Context): String {
        val filePath: String =
            if ((Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable())
            ) {
                Log.d("data_dir", "外部存储可用")
                //外部存储可用
                context.getExternalFilesDir(null)!!.path
            } else {
                Log.d("data_dir", "外部存储不可用")
                //外部存储不可用
                context.filesDir.path
            }
        return filePath
    }

    fun init(appId: String, context: Context, config: BMXSDKConfig? = null) {
        mRtmAdapter.init(if (config == null) {
            val appPath = getFilesPath(context)
            val dataPath = File("$appPath/data_dir")
            val cachePath = File("$appPath/cache_dir")
            dataPath.mkdirs()
            cachePath.mkdirs()
            // 配置sdk config
            BMXSDKConfig(
                BMXClientType.Android, "1", dataPath.absolutePath,
                cachePath.absolutePath, "MaxIM"
            ).apply {
                consoleOutput = true
                logLevel = BMXLogLevel.Debug
                appID = appId
                setEnvironmentType(BMXPushEnvironmentType.Production)
            }
        } else {
            config
        }, context)
        RtmManager.setRtmAdapter(mRtmAdapter)
    }

    fun unInit() {
        mRtmAdapter.unInit()
    }
}