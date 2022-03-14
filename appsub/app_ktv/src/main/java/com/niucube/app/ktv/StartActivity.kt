package com.niucube.app.ktv

import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.launcher.ARouter
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseStartActivity

class StartActivity  : BaseStartActivity() {

    override fun getDefaultImg(): Int {
        return R.drawable.spl_bg
    }
    override val onLoginFinishPostcard: Postcard
        get() =  ARouter.getInstance().build(RouterConstant.KTV.KTVList)

}