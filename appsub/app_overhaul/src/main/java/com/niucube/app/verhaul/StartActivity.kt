package com.niucube.app.verhaul

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.compose.material.Text
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.launcher.ARouter
import com.hipi.vm.LifecycleUiCall
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseStartActivity

class StartActivity : BaseStartActivity() {

    override fun getDefaultImg(): Int {
        return R.drawable.spl_bg
    }

    override val onLoginFinishPostcard: Postcard

    by lazy {
        ARouter.getInstance().build(RouterConstant.Overhaul.OverhaulList)
            .withInt("deviceMode",BuildConfig.deviceMode)
    }
    override fun login(call: LifecycleUiCall<Boolean>) {
        if(BuildConfig.deviceMode==1){
            loginVm.login("10011","8888", LifecycleUiCall<Boolean>(call.lifecycle) {
                if(it){
                    call.onNext(true)
                }else{
                    finish()
                }
            })
        }else{
            super.login(call)
        }
    }

    override fun isIvSplashClientAble(): Boolean {
        return false
    }

}