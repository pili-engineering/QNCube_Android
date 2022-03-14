package com.qiniu.niucube
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.launcher.ARouter
import com.hipi.vm.LifecycleUiCall
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseStartActivity

class SplashActivity : BaseStartActivity() {

    override fun getDefaultImg(): Int {
        return R.drawable.spl_bg
    }
    override val onLoginFinishPostcard: Postcard
        get() =  ARouter.getInstance().build(RouterConstant.App.MainActivity)

}