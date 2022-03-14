package com.qiniu.niucube


import android.util.Log
import androidx.viewpager.widget.ViewPager
import com.alibaba.android.arouter.facade.annotation.Route
import com.hipi.vm.bgDefault
import com.niucube.rtm.RtmCallBack
import com.niucube.rtm.RtmManager
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseActivity
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.widget.CommonPagerAdapter
import com.qiniudemo.module.user.MineFragment
import com.qizhou.bzupdate.UpdateHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


@Route(path = RouterConstant.App.MainActivity)
class MainActivity : BaseActivity() {

    private val pages by lazy {
        listOf(AppsListFragment(), MineFragment())
    }

    override fun initViewData() {
        rgMain.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbTabMain -> {
                    if (vpMain.currentItem != 0) {
                        vpMain.currentItem = 0
                    }
                }
                R.id.rbTabMe -> {
                    if (vpMain.currentItem != 1) {
                        vpMain.currentItem = 2
                    }
                }
            }
        }
        vpMain.adapter = CommonPagerAdapter(pages, supportFragmentManager)
        vpMain.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> rgMain.check(R.id.rbTabMain)
                    1 -> rgMain.check(R.id.rbTabMe)
                }
            }
        })
        rgMain.check(R.id.rbTabMain)

        UpdateHelper.init("$packageName.fileProvider")
        UpdateHelper.startCheck()
    }

    override fun isTranslucentBar(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

}