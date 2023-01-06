package com.qiniudemo.baseapp

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.viewbinding.ViewBinding
import com.alibaba.android.arouter.launcher.ARouter
import com.hapi.baseframe.activity.BaseBindingActivity
import com.hapi.ut.StatusBarUtil
import com.hipi.vm.LoadingObserverView
import com.qiniu.baseapp.R
import com.qiniudemo.baseapp.widget.LoadingDialog

abstract class BaseActivity<T : ViewBinding>: BaseBindingActivity<T>() {

    open fun checkDefaultAutowired(){}
    override fun onCreate(savedInstanceState: Bundle?) {
        ARouter.getInstance().inject(this)
        checkDefaultAutowired()
        super.onCreate(savedInstanceState)
        if (isTranslucentBar()) {
            setTranslucentBarStyle()
        }
    }

    override fun getInittittleColor(): Int {
        return Color.WHITE
    }

    open fun setTranslucentBarStyle() {
        if (Build.VERSION.SDK_INT >= 21) {
            val decorView: View = window.decorView
            val option: Int = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            decorView.systemUiVisibility = option
            window.statusBarColor = Color.TRANSPARENT
        } else {
            StatusBarUtil.setColor(this, Color.parseColor("#000000"), 0x55)
        }
    }

    open fun isTranslucentBar(): Boolean {
        return true
    }

    override fun isToolBarEnable(): Boolean {
        return false
    }

    override fun requestNavigationIcon(): Int {
        return R.drawable.icon_return_xxhdpi
    }

    override fun showLoading(toShow: Boolean) {
            if (toShow) {
                LoadingDialog.showLoading(supportFragmentManager)
            } else {
                LoadingDialog.cancelLoadingDialog()
            }
    }

}
