package com.nucube.module.lowcodeliving;

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.hapi.baseframe.adapter.QRecyclerViewBindHolder
import com.hapi.baseframe.dialog.BaseVmBindingDialogFragment
import com.hapi.baseframe.dialog.FinalDialogFragment
import com.hapi.baseframe.smartrecycler.QSmartViewBindAdapter
import com.hipi.vm.backGround
import com.nucube.module.lowcodeliving.databinding.ActivityDemoLiveFinishedBinding
import com.nucube.module.lowcodeliving.databinding.DialogConnectUsBinding
import com.nucube.module.lowcodeliving.databinding.ItemStatisticsSmallBinding
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.jsonutil.JsonUtils
import com.qiniudemo.baseapp.BaseActivity
import com.qiniudemo.baseapp.BaseFragment
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.widget.CommonTipDialog
import com.qlive.core.QLiveCallBack
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveStatistics
import com.qlive.sdk.QLive
import com.qlive.uikitcore.dialog.LoadingDialog
import jp.wasabeef.glide.transformations.BlurTransformation
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class DemoLiveFinishedActivity : BaseActivity<ActivityDemoLiveFinishedBinding>() {


    companion object {
        fun checkStart(
            context: Context,
            room: QLiveRoomInfo,
        ) {
            val intent = Intent(context, DemoLiveFinishedActivity::class.java)
            intent.putExtra("QLiveRoomInfo", room)
            context.startActivity(intent)
        }
    }

    private val mStatisticsAdapter by lazy { StatisticsAdapter() }

    override fun init() {

        binding.ivClose.setOnClickListener {
            onBackPressed()
        }
        binding.recyLiveData.layoutManager = GridLayoutManager(this, 3)
        binding.recyLiveData.adapter = mStatisticsAdapter
        (intent.getSerializableExtra("QLiveRoomInfo") as QLiveRoomInfo?)?.let { roomInfo ->
            binding.llLiveData.setOnClickListener {
                DemoStatisticsActivity.checkStart(this@DemoLiveFinishedActivity, roomInfo)
            }
            Glide.with(this)
                .load(roomInfo.coverURL)
                .transform(MultiTransformation(CenterCrop(), BlurTransformation(25, 3)))
                .into(binding.ivRoomCover)
            Glide.with(this)
                .load(roomInfo.anchor?.avatar)
                .into(binding.ivAnchorAvatar)
            binding.tvAnchorName.text = roomInfo.anchor?.nick ?: ""
            binding.tvAnchorID.text = "主播ID：${roomInfo.anchor.userId}"
            QLive.getRooms()
                .getLiveStatistics(roomInfo.liveID, object : QLiveCallBack<QLiveStatistics> {
                    override fun onError(code: Int, msg: String?) {
                    }

                    override fun onSuccess(data: QLiveStatistics) {
                        mStatisticsAdapter.setNewData(data.toQLiveStatisticsWrap())
                    }
                })
        }
        binding.flUnRegistered.setOnClickListener {
            applyOpening(false)
        }
        binding.flRegistered.setOnClickListener {
            applyOpening(true)
        }
        binding.flUnRegistered.visibility = View.GONE
        binding.flRegistered.visibility = View.GONE
        backGround {
            LoadingDialog.showLoading(supportFragmentManager)
            doWork {
                val isRegister = RetrofitManager.create(LiveSdkService::class.java)
                    .isRegister()
                if (isRegister) {
                    binding.flUnRegistered.visibility = View.GONE
                    binding.flRegistered.visibility = View.GONE
                } else {
                    binding.flUnRegistered.visibility = View.VISIBLE
                    binding.flRegistered.visibility = View.VISIBLE
                }
            }
            catchError {
                it.message?.asToast()
            }
            onFinally {
                LoadingDialog.cancelLoadingDialog()
            }
        }
    }

    private fun applyOpening(isRegistered: Boolean) {
        ConnectUsDialog(isRegistered).show(supportFragmentManager, "")
    }

    class ConnectUsDialog(private val isRegistered: Boolean) :
        BaseVmBindingDialogFragment<DialogConnectUsBinding>() {
        init {
            applyGravityStyle(Gravity.BOTTOM)
        }

        override fun initViewData() {
            binding.ivCloseDialog.setOnClickListener {
                dismiss()
            }
            if (isRegistered) {
                binding.tvHitCount.text = "七牛云账号"
                binding.etCount.hint = "请输您的注册邮箱或手机号"
            } else {
                binding.tvHitCount.text = "手机号"
                binding.etCount.hint = "请输您的手机号"
            }
            binding.btnConfirm.setOnClickListener {
                val str = binding.etCount.text.toString()
                if (TextUtils.isEmpty(str)) {
                    return@setOnClickListener
                }
                backGround {
                    LoadingDialog.showLoading(childFragmentManager)
                    doWork {
                        val type = "application/json;charset=utf-8".toMediaType()
                        val body = JsonUtils.toJson(Statistics().apply {
                            isQiniuUser = isRegistered
                            userName = str
                        }).toRequestBody(type)
                        RetrofitManager.create(LiveSdkService::class.java).statistics(body)
                    }
                    catchError {
                        it.message?.asToast()
                    }
                    onFinally {
                        LoadingDialog.cancelLoadingDialog()
                        CommonTipDialog.TipBuild()
                            .setTittle("提交成功")
                            .setContent(
                                "我们将尽快联系您"
                            )
                            .setPositiveText("确定")
                            .isNeedCancelBtn(false)
                            .build()
                            .apply {
                                mDefaultListener =
                                    object : FinalDialogFragment.BaseDialogListener() {
                                        override fun onDismiss(dialog: DialogFragment) {
                                            super.onDismiss(dialog)
                                            requireActivity().finish()
                                        }
                                    }
                            }
                            .show(childFragmentManager, "")
                    }
                }
            }
        }

        override fun showLoading(toShow: Boolean) {
        }
    }

    fun QLiveStatistics.toQLiveStatisticsWrap(): List<QLiveStatisticsWrap> {
        val wraps = ArrayList<QLiveStatisticsWrap>()
        this.info.forEach {
            if (it.type == QLiveStatistics.TYPE_LIVE_WATCHER_COUNT) {
                wraps.add(QLiveStatisticsWrap("浏览次数", it.pageView.toFormatNumber()))
                wraps.add(QLiveStatisticsWrap("观看人数", it.uniqueVisitor.toFormatNumber()))
            }
            if (it.type == QLiveStatistics.TYPE_PUBCHAT_COUNT) {
                wraps.add(QLiveStatisticsWrap("聊天互动", it.pageView.toFormatNumber()))
            }
            if (it.type == QLiveStatistics.TYPE_LIKE_COUNT) {
                wraps.add(QLiveStatisticsWrap("点赞", it.pageView.toFormatNumber()))
            }
            if (it.type == QLiveStatistics.TYPE_GIFT_COUNT) {
                wraps.add(QLiveStatisticsWrap("观众打赏", it.pageView.toFormatNumber()))
            }
        }
        return wraps
    }

    class StatisticsAdapter :
        QSmartViewBindAdapter<QLiveStatisticsWrap, ItemStatisticsSmallBinding>(
        ) {
        override fun convertViewBindHolder(
            helper: QRecyclerViewBindHolder<ItemStatisticsSmallBinding>,
            item: QLiveStatisticsWrap
        ) {
            helper.binding.tvKey.text = item.key
            helper.binding.tvValues.text = item.value
        }
    }
}