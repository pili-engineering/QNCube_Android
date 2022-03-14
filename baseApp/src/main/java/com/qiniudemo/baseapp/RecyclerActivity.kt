package com.qiniudemo.baseapp

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.hapi.base_mvvm.mvvm.BaseRecyclerActivity
import com.hapi.refresh.IEmptyView
import com.hapi.ut.StatusBarUtil
import com.hapi.ut.ViewUtil
import com.qiniu.baseapp.R
import com.qiniudemo.baseapp.widget.CommonEmptyView
import com.qiniudemo.baseapp.widget.LoadingDialog
import com.scwang.smartrefresh.header.MaterialHeader
import com.scwang.smartrefresh.layout.api.RefreshHeader


abstract class RecyclerActivity<T> : BaseRecyclerActivity<T>() {

    /**
     * 通用emptyView
     */
    /**
     * 通用emptyView
     */
    override fun getEmptyView(): IEmptyView? {
        return CommonEmptyView(this).apply {
            val temp = getRefreshTempView()
            if (temp != null) {
                setRefreshingView(temp)
                setStatus(IEmptyView.START_REFREASH_WHEN_EMPTY)
            }
        }
    }

    override fun requestNavigationIcon(): Int {
        return R.drawable.icon_return_xxhdpi
    }

    override fun getInittittleColor(): Int {
        return Color.WHITE
    }

    override fun initViewData() {
        super.initViewData()
        adapter.loadMoreEnd()
    }

    override fun observeLiveData() {

    }

    open fun getRefreshTempView(): View? {
        return null
    }

    override fun getGetRefreshHeader(): RefreshHeader {
        return MaterialHeader(this)
    }
    open fun checkDefaultAutowired(){}
    override fun onCreate(savedInstanceState: Bundle?) {
        ARouter.getInstance().inject(this)
        checkDefaultAutowired()
        super.onCreate(savedInstanceState)
        if (isTranslucentBar()) {
            setTranslucentBarStyle()
        }
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
        return false
    }

    override fun isToolBarEnable(): Boolean {
        return false
    }

    override fun showLoading(toShow: Boolean) {
        if (toShow) {
            LoadingDialog.showLoading(supportFragmentManager)
        } else {
            LoadingDialog.cancelLoadingDialog()
        }
    }

}