package com.qiniudemo.baseapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.hapi.ut.SpUtil
import com.hapi.ut.helper.ActivityManager
import com.hipi.vm.LifecycleUiCall
import com.hipi.vm.lazyVm
import com.qiniu.baseapp.R
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.been.AppConfigModel
import com.qiniudemo.baseapp.config.SPConstant
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.service.AppConfigService
import com.qiniudemo.baseapp.vm.LoginVm
import com.qiniudemo.baseapp.web.WebViewActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_spalash.*
import kotlinx.coroutines.*

abstract class BaseStartActivity : BaseActivity() {

    protected val loginVm by lazyVm<LoginVm>()
    companion object{
        var loginFinishPostcard: Postcard? = null
    }
    abstract val onLoginFinishPostcard: Postcard

    open fun getDefaultImg(): Int {
        return -1
    }
    private val loginRequestCode = 101

    @SuppressLint("CheckResult")
    override fun initViewData() {
        loginFinishPostcard = onLoginFinishPostcard
        RxPermissions(this).request(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).subscribe {}
        lifecycleScope.launch(Dispatchers.Main) {

            var config: AppConfigModel? = null
            try {
                config = RetrofitManager.create(AppConfigService::class.java)
                    .appConfig()

                if (!TextUtils.isEmpty(config.welcome?.image)) {
                    // if(config.welcome?.image!=lastImg){
                    Glide.with(this@BaseStartActivity)
                        .load(Uri.parse(config.welcome?.image))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(ivSplash)
                    //  }
                    SpUtil.get(SPConstant.User.SpName).saveData("welcomeImg", config.welcome?.image)
                    if(isIvSplashClientAble()){
                        ivSplash.setOnClickListener {
                            if (config.welcome?.url?.startsWith("http") == true) {
                                WebViewActivity.start(config.welcome?.url ?: "", this@BaseStartActivity)
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                e.message?.asToast()
            }

            if (config?.welcome?.image?.isEmpty() == true) {
                delay(1000)
            } else {
                delay(2000)
            }

            login(LifecycleUiCall(this@BaseStartActivity) {
                if (it) {
                    val postcard = (onLoginFinishPostcard)
                    if (ActivityManager.get().currentActivity() == this@BaseStartActivity) {
                        postcard.navigation(this@BaseStartActivity)
                    } else {
                        lifecycleScope.launchWhenResumed {
                            postcard.navigation(this@BaseStartActivity)
                        }
                    }
                    finish()
                } else {
                    val postcard = ARouter.getInstance().build(RouterConstant.Login.LOGIN)
                    if (ActivityManager.get().currentActivity() == this@BaseStartActivity) {
                        postcard.navigation(this@BaseStartActivity, loginRequestCode)

                    } else {
                        lifecycleScope.launchWhenResumed {
                            postcard.navigation(this@BaseStartActivity, loginRequestCode)
                        }
                    }
                    finish()
                }
            })
        }
    }

    open fun login(call: LifecycleUiCall<Boolean>) {
        loginVm.login(call)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_spalash
    }

    open fun isIvSplashClientAble():Boolean{
        return true
    }

}