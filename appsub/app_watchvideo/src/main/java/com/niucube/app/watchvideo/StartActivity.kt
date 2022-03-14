package com.niucube.app.watchvideo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alibaba.android.arouter.facade.Postcard
import com.alibaba.android.arouter.launcher.ARouter
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseStartActivity

class StartActivity : BaseStartActivity() {
    override val onLoginFinishPostcard: Postcard
        get() = ARouter.getInstance().build(RouterConstant.VideoRoom.VideoHome)

}