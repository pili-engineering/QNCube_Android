package com.nucube.module.lowcodeliving;

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.hapi.happy_dialog.FinalDialogFragment
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.dialog.CommonTipDialog
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_demo_live_finished.*
import kotlinx.android.synthetic.main.dialog_connect_us.*

class DemoLiveFinishedActivity : AppCompatActivity() {

    companion object {
        fun checkStart(
            context: QLiveUIKitContext,
            client: QLiveClient,
            room: QLiveRoomInfo,
            isAnchorActionCloseRoom: Boolean
        ) {
            if (isAnchorActionCloseRoom) {
                val intent = Intent(context.androidContext, DemoLiveFinishedActivity::class.java)
                intent.putExtra("QLiveRoomInfo", room)
                context.androidContext.startActivity(intent)
            } else {
                return
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_live_finished)
        ivClose.setOnClickListener {
            onBackPressed()
        }
        (intent.getSerializableExtra("QLiveRoomInfo") as QLiveRoomInfo?)?.let {
            Glide.with(this)
                .load(it.coverURL)
                .transform(MultiTransformation(CenterCrop(), BlurTransformation(25, 3)))
                .into(ivRoomCover)
            Glide.with(this)
                .load(it.anchor?.avatar)
                .into(ivAnchorAvatar)
            tvAnchorName.text = it.anchor?.nick ?: ""
            tvAnchorID.text = "主播ID：${it.anchor.userId}"
        }
        flUnRegistered.setOnClickListener {
            applyOpening(false)
        }
        flRegistered.setOnClickListener {
            applyOpening(true)
        }
    }

    private fun applyOpening(isRegistered: Boolean) {
        ConnectUsDialog(isRegistered).show(supportFragmentManager, "")
    }

    class ConnectUsDialog(private val isRegistered: Boolean) : FinalDialogFragment() {
        init {
            applyGravityStyle(Gravity.BOTTOM)
        }

        override fun getViewLayoutId(): Int {
            return R.layout.dialog_connect_us
        }

        override fun init() {
            ivCloseDialog.setOnClickListener {
                dismiss()
            }
            if (isRegistered) {
                tvHitCount.text = "七牛云账号"
                etCount.hint = "请输您的注册邮箱或手机号"
            } else {
                tvHitCount.text = "手机号"
                etCount.hint = "请输您的手机号"
            }
            btnConfirm.setOnClickListener {
                val str = etCount.text.toString()
                if (TextUtils.isEmpty(str)) {
                    return@setOnClickListener
                }
                CommonTipDialog.TipBuild()
                    .setTittle("提交成功")
                    .setContent(
                        "我们将尽快联系您"
                    )
                    .setPositiveText("确定")
                    .isNeedCancelBtn(false)
                    .build()
                    .show(childFragmentManager, "")
            }
        }
    }
}