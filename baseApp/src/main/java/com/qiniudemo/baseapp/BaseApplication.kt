package com.qiniudemo.baseapp

import android.app.Application
import android.content.Intent
import android.os.Process
import com.alibaba.android.arouter.launcher.ARouter
import com.hapi.ut.AppCache
import com.hapi.ut.MainThreadHelper
import com.hapi.ut.constans.FileConstants
import com.hapi.ut.helper.ActivityManager
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.qiniu.baseapp.BuildConfig
import com.qiniu.bzcomp.user.*
import com.qiniu.comp.network.NetConfig
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.router.RouterConstant
import com.qiniu.bzcomp.network.QiniuJsonFactor
import com.qiniu.bzcomp.network.QiniuRequestInterceptor
import com.qiniu.comp.network.Form2JsonInterceptor
import com.qiniu.qnim.IMManager

import com.qiniudemo.baseapp.manager.swith.EnvType
import com.qiniudemo.baseapp.manager.swith.SwitchEnvHelper
import com.qiniudemo.baseapp.util.UniException
import com.tencent.bugly.crashreport.CrashReport


open class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FileConstants.initFileConfig(this)
        CrashReport.initCrashReport(this, BuildConfig.bugly, true);
        UniException.getInstance().init()
        //自定义activity栈
        ActivityManager.get().init(this)
        //全局context
        AppCache.setContext(this)
        //日志 初始化
        Logger.addLogAdapter(object : AndroidLogAdapter() {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })
        //路由
        if (BuildConfig.DEBUG) {
            ARouter.openDebug()
            ARouter.openLog()
        }
        ARouter.init(this);

        //环境切换
        SwitchEnvHelper.get().init(this, BuildConfig.DEBUG)
        SwitchEnvHelper.regist(true) {
            MainThreadHelper.postDelayed({

                UserInfoManager.clearUser()
                val i: Intent? =
                    packageManager.getLaunchIntentForPackage(packageName)
                i?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(i)

                Process.killProcess(Process.myPid())
            }, 200)
        }
        // 网络库
        RetrofitManager.resetConfig(NetConfig().apply {
            base = when (SwitchEnvHelper.get().envType) {
                EnvType.Release -> BuildConfig.base_url
                else -> BuildConfig.base_url_dev
            }
            converterFactory = QiniuJsonFactor.create()
            okBuilder.addInterceptor(QiniuRequestInterceptor())
                .addInterceptor(Form2JsonInterceptor())
                .addInterceptor(logInterceptor)

        })
        //
        UserInfoManager.init()

        IMManager.init(this,  BuildConfig.QNIMAPPID)

        UserLifecycleManager.addUserLifecycleInterceptor(object : UserLifecycleInterceptor {
            override fun onLogout(toastStr: String) {
                ARouter.getInstance().build(RouterConstant.Login.LOGIN)
                    .navigation(ActivityManager.get().currentActivity())
                ActivityManager.get().finishAllActivity()
                IMManager.loginOut()
            }

            override suspend fun onLogin(loginToken: LoginToken) {
                IMManager.loginSuspend(loginToken.accountId,loginToken.imConfig.imUid,loginToken.imConfig.imUsername,loginToken.imConfig.imPassword)
            }

            override fun onUserInfoRefresh(userInfo: UserInfo) {}
        })
    }
}