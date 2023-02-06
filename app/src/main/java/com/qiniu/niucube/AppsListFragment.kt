package com.qiniu.niucube

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.hapi.baseframe.adapter.QRecyclerViewBindHolder
import com.hapi.baseframe.smartrecycler.QSmartViewBindAdapter
import com.hapi.baseframe.smartrecycler.SmartRefreshHelper
import com.hapi.ut.ViewUtil
import com.hipi.vm.backGround
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.niucube.databinding.FragmentAppListBinding
import com.qiniu.niucube.databinding.ItemQiniuAppBinding
import com.qiniudemo.baseapp.BaseFragment
import com.qiniudemo.baseapp.been.QiniuApp
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.manager.SchemaParser
import com.qiniudemo.baseapp.service.AppConfigService

class AppsListFragment : BaseFragment<FragmentAppListBinding>() {

    private val adapter by lazy { AppAdapter() }
    private val layoutManager: RecyclerView.LayoutManager
        get() = GridLayoutManager(requireContext(), 2)
    private val smartRefreshHelper: SmartRefreshHelper<QiniuApp> by lazy {
        SmartRefreshHelper(requireContext(),
            adapter,
            binding.mRecyclerView,
            binding.refreshLayout,
            binding.emptyView,
            false,
            refreshNeed = true,
            fetcherFuc = fetcherFuc
        )
    }

    private val fetcherFuc: (page: Int) -> Unit = {
        backGround {
            doWork {
                val solutions = RetrofitManager.create(AppConfigService::class.java)
                    .solutions()
                smartRefreshHelper.onFetchDataFinish(solutions.list, true, true)
            }
            catchError {
                smartRefreshHelper.onFetchDataError()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mRecyclerView.adapter = adapter
        binding.mRecyclerView.layoutManager = layoutManager
        binding.mRecyclerView.addItemDecoration(SplitLine())
        smartRefreshHelper.refresh()
    }

    inner class AppAdapter : QSmartViewBindAdapter<QiniuApp, ItemQiniuAppBinding>() {
        override fun convertViewBindHolder(
            helper: QRecyclerViewBindHolder<ItemQiniuAppBinding>,
            item: QiniuApp
        ) {
            helper.binding.tvSolutionName.text = item.title
            Glide.with(mContext)
                .load(item.icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(helper.binding.ivIcon)
            helper.itemView.setOnClickListener {
                if (!SchemaParser.parseRouter(
                        mContext,
                        childFragmentManager,
                        item.url + "?type=${item.type}",
                        false
                    )
                ) {
                    "${item.desc}".asToast()
                    val cm: ClipboardManager? =
                        mContext?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
                    val mClipData: ClipData = ClipData.newPlainText("Label", item.url)
                    cm?.setPrimaryClip(mClipData)
                    "链接已经拷贝至剪切板".asToast()
                }
            }
        }

    }

    inner class SplitLine : RecyclerView.ItemDecoration() {

        private val dividerPaint = Paint().apply { color = Color.parseColor("#EAEAEA") }

        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            val childCount = parent.childCount

            val dividerSize = ViewUtil.dip2px(0.6f)
            val dividerVMargin = ViewUtil.dip2px(32f)
            val dividerSizeMargin = ViewUtil.dip2px(19f)

            val isEven = childCount % 2 == 0
            for (i in 0 until childCount) {
                val view = parent.getChildAt(i)
                val isLeft = (i % 2 == 0) || (!isEven && i == childCount - 1)

                val isModel2 = childCount % 2 == 0
                val last = if (isModel2) childCount - 2 else childCount - 1
                if (i < last || childCount <= 2) {
                    // bottomVMargain = 0
                    //绘制下横线
                    val leftH = if (isLeft) (view.left + dividerSizeMargin) else view.left
                    val topH = view.bottom - dividerSize
                    val rightH = if (isLeft) view.right else view.right - dividerSizeMargin
                    val bottomH = view.bottom + dividerSize
                    c.drawRect(
                        leftH.toFloat(),
                        topH.toFloat(),
                        rightH.toFloat(),
                        bottomH.toFloat(),
                        dividerPaint
                    )
                }

                if (isLeft) {
                    val leftV = view.right - dividerSize
                    val topV = if (i == 0) view.top + dividerVMargin else view.top
                    val rightV = view.right + dividerSize
                    val bottomV =
                        if (i == childCount - 1 || i == childCount - 2) view.bottom - dividerVMargin else view.bottom
                    c.drawRect(
                        leftV.toFloat(),
                        topV.toFloat(),
                        rightV.toFloat(),
                        bottomV.toFloat(),
                        dividerPaint
                    )
                }
            }
        }
    }
}