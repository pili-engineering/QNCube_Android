package com.qiniudemo.login

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.hipi.vm.LifecycleUiCall
import com.hipi.vm.lazyVm
import com.qiniu.login.R
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseActivity
import com.qiniudemo.baseapp.BaseStartActivity.Companion.loginFinishPostcard
import com.qiniudemo.baseapp.vm.LoginVm
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.manager.swith.EnvSwitchDialog
import com.qiniudemo.baseapp.manager.swith.SwitchEnvHelper
import com.qiniudemo.webview.WebActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Route(path = RouterConstant.Login.LOGIN)
class LoginActivity : BaseActivity() {

    private val loginVm by lazyVm<LoginVm>()
    private fun timeJob() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                tvSmsTime.isClickable = false
                repeat(60) {
                    tvSmsTime.text = (60 - it).toString()
                    delay(1000)
                }
                tvSmsTime.text = "获取验证码"
                tvSmsTime.isClickable = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun initViewData() {

        if (SwitchEnvHelper.get().isSwitchEnable) {
            clRoot.setOnLongClickListener {
                EnvSwitchDialog(this).show()
                true
            }
        }

        tvSmsTime.setOnClickListener {
            val phone = et_login_phone.text.toString() ?: ""
            loginVm.getVerificationCode(phone, LifecycleUiCall(lifecycle = this) {
                et_login_verification_code.requestFocus()
                timeJob()
            })
        }

        bt_login_login.setOnClickListener {
            val phone = et_login_phone.text.toString() ?: ""
            val code = et_login_verification_code.text.toString() ?: ""
            if (phone.isEmpty()) {
                "请输入手机号".asToast()
                return@setOnClickListener
            }
            if (code.isEmpty()) {
                "请输入验证码".asToast()
                return@setOnClickListener
            }

            if (!cbAgreement.isSelected) {
                "请同意 七牛云服务用户协议 和 隐私权政策".asToast()
                return@setOnClickListener
            }

            loginVm.login(phone, code, LifecycleUiCall(this) {
                if (it) {
                    loginFinishPostcard?.navigation(this)
                    finish()
                }
            })
        }

        cbAgreement.setOnClickListener {
            cbAgreement.isSelected = !cbAgreement.isSelected
        }
        val tips = "我已阅读并同意 七牛云服务用户协议 和 隐私权政策"
        val spannableString = SpannableString(tips)
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                WebActivity.start("https://www.qiniu.com/privacy-right", this@LoginActivity)
            }
        }, tips.length - 5, tips.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                WebActivity.start("https://www.qiniu.com/user-agreement", this@LoginActivity)
            }
        }, tips.length - 18, tips.length - 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            tips.length - 5,
            tips.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            tips.length - 18,
            tips.length - 7,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#007AFF")),
            tips.length - 5,
            tips.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#007AFF")),
            tips.length - 18,
            tips.length - 7,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        cbAgreement.setMovementMethod(LinkMovementMethod.getInstance());//设置可点击状态
        cbAgreement.text = spannableString
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_login
    }

}