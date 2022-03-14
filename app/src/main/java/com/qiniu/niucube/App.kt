package com.qiniu.niucube

import android.util.Log
import com.orhanobut.logger.Logger
import com.qiniudemo.baseapp.BaseApplication

class App : BaseApplication() {

    override fun onCreate() {
        super.onCreate()
        Logger.t("mjl").d("app start")
    }
}