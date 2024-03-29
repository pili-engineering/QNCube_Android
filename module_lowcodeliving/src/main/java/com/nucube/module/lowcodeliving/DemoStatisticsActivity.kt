package com.nucube.module.lowcodeliving

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Color.argb
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.hapi.baseframe.adapter.QRecyclerViewBindHolder
import com.hapi.baseframe.smartrecycler.QSmartViewBindAdapter
import com.nucube.module.lowcodeliving.databinding.ActivityDemoStatisticsBinding
import com.nucube.module.lowcodeliving.databinding.ItemStatisticsBigBinding
import com.qiniudemo.baseapp.BaseActivity
import com.qlive.core.QLiveCallBack
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveStatistics
import com.qlive.core.been.QLiveStatistics.*
import com.qlive.sdk.QLive
import com.qlive.uikitcore.ext.ViewUtil
import com.qlive.uikitcore.view.SimpleDividerDecoration
import java.math.BigDecimal

class QLiveStatisticsWrap(val key: String, val value: String)

val getNameCall: (QLiveStatistics.Info, Int) -> String = { info, index ->
    when (info.type) {
        TYPE_LIVE_WATCHER_COUNT -> {
            if (index == 0) {
                "直播间浏览次数"
            } else {
                "直播间浏览人数"
            }
        }
        TYPE_QItem_CLICK_COUNT -> {
            if (index == 0) {
                "商品点击次数"
            } else {
                "商品点击人数"
            }
        }
        TYPE_PUBCHAT_COUNT -> {
            if (index == 0) {
                "评论次数"
            } else {
                "评论人数"
            }
        }
        TYPE_PK_COUNT -> {
            if (index == 0) {
                "pk次数"
            } else {
                "pk人数"
            }
        }
        TYPE_LINK_MIC_COUNT -> {
            if (index == 0) {
                "连麦次数"
            } else {
                "连麦人数"
            }
        }
        TYPE_LIKE_COUNT -> {
            if (index == 0) {
                "点赞次数"
            } else {
                "点赞人数"
            }
        }
        TYPE_GIFT_COUNT -> {
            if (index == 0) {
                "礼物总额"
            } else {
                "礼物人数"
            }
        }
        else -> ""
    }
}

fun Int.toFormatNumber(): String {
    if (this >= 10000) {
        val b = BigDecimal(this.toDouble() / 10000.0)
        return b.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "万"
    } else {
        return this.toString()
    }
}

class DemoStatisticsActivity : BaseActivity<ActivityDemoStatisticsBinding>() {
    companion object {
        fun checkStart(
            context: Context,
            room: QLiveRoomInfo
        ) {
            val intent = Intent(context, DemoStatisticsActivity::class.java)
            intent.putExtra("QLiveRoomInfo", room)
            context.startActivity(intent)
        }
    }

    private val mStatisticsAdapter by lazy { StatisticsAdapter() }

    override fun init() {
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.recyLiveData.layoutManager = GridLayoutManager(this, 3)
        binding.recyLiveData.adapter = mStatisticsAdapter
        binding.recyLiveData.addItemDecoration(
            SimpleDividerDecoration(
                this,
                Color.parseColor("#4F5058"), ViewUtil.dip2px(0.5f)
            )
        )
        (intent.getSerializableExtra("QLiveRoomInfo") as QLiveRoomInfo?)?.let {
            binding.tvTitle.text = it.title
            QLive.getRooms().getLiveStatistics(it.liveID, object : QLiveCallBack<QLiveStatistics> {
                override fun onError(code: Int, msg: String?) {
                }

                override fun onSuccess(data: QLiveStatistics) {
                    mStatisticsAdapter.setNewData(data.toQLiveStatisticsWrap())
                }
            })
        }
    }

    fun QLiveStatistics.toQLiveStatisticsWrap(): List<QLiveStatisticsWrap> {
        val wraps = ArrayList<QLiveStatisticsWrap>()
        this.info.forEach {
            wraps.add(QLiveStatisticsWrap(getNameCall(it, 0), it.pageView.toFormatNumber()))
            wraps.add(QLiveStatisticsWrap(getNameCall(it, 1), it.uniqueVisitor.toFormatNumber()))
        }
        wraps.add(QLiveStatisticsWrap("流量消耗", this.flow))
        return wraps
    }

    class StatisticsAdapter :
        QSmartViewBindAdapter<QLiveStatisticsWrap, ItemStatisticsBigBinding>() {
        override fun convertViewBindHolder(
            helper: QRecyclerViewBindHolder<ItemStatisticsBigBinding>,
            item: QLiveStatisticsWrap
        ) {
            helper.binding.tvKey.text = item.key
            helper.binding.tvValues.text = item.value
        }
    }
}