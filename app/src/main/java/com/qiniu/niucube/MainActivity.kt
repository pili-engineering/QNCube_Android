package com.qiniu.niucube

import android.view.KeyEvent
import androidx.viewpager.widget.ViewPager
import com.alibaba.android.arouter.facade.annotation.Route
import com.hipi.vm.backGround
import com.niucube.rtm.RtmCallBack
import com.qiniu.baseapp.BuildConfig
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.niucube.databinding.ActivityMainBinding
import com.qiniu.qnim.QNIMManager
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseActivity
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.web.WebFragment
import com.qiniudemo.baseapp.widget.CommonPagerAdapter
import com.qiniudemo.baseapp.widget.LoadingDialog
import com.qiniudemo.module.user.MineFragment
import com.qizhou.bzupdate.UpdateHelper

@Route(path = RouterConstant.App.MainActivity)
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val pages by lazy {
        listOf(
            WebFragment().apply { start("https://sol-introduce.qiniu.com/") },
            AppsListFragment(), MineFragment()
        )
    }

    override fun init() {
        binding.rgMain.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbTabSulotion -> {
                    if (binding.vpMain.currentItem != 0) {
                        binding.vpMain.currentItem = 0
                    }
                }
                R.id.rbTabApp -> {
                    if (binding.vpMain.currentItem != 1) {
                        binding.vpMain.currentItem = 1
                    }
                }
                R.id.rbTabMe -> {
                    if (binding.vpMain.currentItem != 2) {
                        binding.vpMain.currentItem = 2
                    }
                }
            }
        }
        binding.vpMain.adapter = CommonPagerAdapter(pages, supportFragmentManager)
        binding.vpMain.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> binding.rgMain.check(R.id.rbTabSulotion)
                    1 -> binding.rgMain.check(R.id.rbTabApp)
                    2 -> binding.rgMain.check(R.id.rbTabMe)
                }
            }
        })
        binding.rgMain.check(R.id.rbTabSulotion)
        UpdateHelper.init("$packageName.fileProvider")
        UpdateHelper.startCheck()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (binding.vpMain.currentItem == 0 &&
            (pages[0] as WebFragment).onKeyDown(keyCode, event)
        ) {
            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun isTranslucentBar(): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        //跳转到了低代码sdk 登陆了其他im
        if (!QNIMManager.mRtmAdapter.isLogin) {
            backGround {
                doWork {
                    QNIMManager.mRtmAdapter.suspendLoginOut()
                    val loginToken = UserInfoManager.mLoginToken!!
                    LoadingDialog.showLoading(supportFragmentManager)
                    QNIMManager.unInit()
                    QNIMManager.init(BuildConfig.QNIMAPPID, application)
                    QNIMManager.mRtmAdapter.login(
                        loginToken.accountId,
                        loginToken.imConfig.imUid,
                        loginToken.imConfig.imUsername,
                        loginToken.imConfig.imPassword,
                        object : RtmCallBack {
                            override fun onSuccess() {
                                LoadingDialog.cancelLoadingDialog()
                            }

                            override fun onFailure(code: Int, msg: String) {
                                LoadingDialog.cancelLoadingDialog()
                                "msg".asToast()
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }

}