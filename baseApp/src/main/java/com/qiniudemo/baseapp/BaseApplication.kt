package com.qiniudemo.baseapp

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Process
import androidx.multidex.MultiDex
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
import com.qiniu.qnim.QNIMAdapter
import com.qiniu.qnim.QNIMManager
import com.qiniudemo.baseapp.ext.asToast

import com.qiniudemo.baseapp.manager.swith.EnvType
import com.qiniudemo.baseapp.manager.swith.SwitchEnvHelper
import com.qiniudemo.baseapp.util.UniException
import com.qlive.uiwidghtbeauty.QSenseTimeManager
import com.tencent.bugly.crashreport.CrashReport


open class BaseApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        //文件路径系统
        FileConstants.initFileConfig(this)
        //bugly
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
        QiniuRequestInterceptor.appVersionName = packageManager.getPackageInfo(
            packageName,
            PackageManager.GET_CONFIGURATIONS
        ).versionName
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
        //用户管理
        UserInfoManager.init()
        //初始化im
        QNIMManager.init(BuildConfig.QNIMAPPID, this)
        QNIMManager.mRtmAdapter.onKickCall = {
            "你的账号在其他设备登陆".asToast()
            UserInfoManager.clearUser()
            val i: Intent? =
                packageManager.getLaunchIntentForPackage(packageName)
            i?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
            Process.killProcess(Process.myPid())
        }
        //用户状态监听
        UserLifecycleManager.addUserLifecycleInterceptor(object : UserLifecycleInterceptor {
            override fun onLogout(toastStr: String) {
                //退出登陆
                ARouter.getInstance().build(RouterConstant.Login.LOGIN)
                    .navigation(ActivityManager.get().currentActivity())
                ActivityManager.get().finishAllActivity()
                QNIMManager.mRtmAdapter.loginOut()
            }

            override suspend fun onLogin(loginToken: LoginToken) {
                //登陆成功后
                QNIMManager.mRtmAdapter.loginSuspend(
                    loginToken.accountId,
                    loginToken.imConfig.imUid,
                    loginToken.imConfig.imUsername,
                    loginToken.imConfig.imPassword
                )
            }

            //用户信息变更
            override fun onUserInfoRefresh(userInfo: UserInfo) {}
        })
        QSenseTimeManager.initEffectFromLocalLicense(this, false)
    }
}