package com.qiniu.niucube

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hapi.base_mvvm.refresh.SmartRefreshHelper
import com.hapi.ut.ViewUtil
import com.hipi.vm.backGround
import com.qiniu.comp.network.RetrofitManager
import com.qiniudemo.baseapp.BaseFragment
import com.qiniudemo.baseapp.been.QiniuApp
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.manager.SchemaParser
import com.qiniudemo.baseapp.service.AppConfigService
import kotlinx.android.synthetic.main.fragment_app_list.*
import kotlinx.android.synthetic.main.item_qiniu_app.view.*


class AppsListFragment : BaseFragment() {

    private val adapter: BaseQuickAdapter<QiniuApp, *> by lazy { AppAdapter() }
    private val layoutManager: RecyclerView.LayoutManager
        get() = GridLayoutManager(
            requireContext(),
            2
        )
    private val smartRefreshHelper: SmartRefreshHelper<QiniuApp> by lazy {
        SmartRefreshHelper(
            adapter,
            mRecyclerView,
            refreshLayout,
            emptyView,
            5,
            false,
            refreshNeed = true,
            fetcherFuc = fetcherFuc
        )
    }
    private val fetcherFuc: (page: Int) -> Unit = {
        backGround {
            doWork {
                     val solutions   = RetrofitManager.create(AppConfigService::class.java)
                    .solutions()
                  smartRefreshHelper.onFetchDataFinish(solutions.list, true, true)
            }
            catchError {
                smartRefreshHelper.onFetchDataError()
            }
        }
    }

    override fun initViewData() {

        mRecyclerView.adapter = adapter
        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.addItemDecoration(SplitLine())
        smartRefreshHelper.refresh()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_app_list
    }

    inner class AppAdapter :
        BaseQuickAdapter<QiniuApp, BaseViewHolder>(R.layout.item_qiniu_app, ArrayList<QiniuApp>()) {
        override fun convert(helper: BaseViewHolder, item: QiniuApp) {
            helper.itemView.tvSolutionName.text = item.title
            Glide.with(mContext)
                .load(item.icon)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(helper.itemView.ivIcon)
            helper.itemView.setOnClickListener {
                 if(!SchemaParser.parseRouter(mContext, item.url+"?type=${item.type}")){
                     "敬请期待".asToast()
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
            for (i in 0 until childCount ) {
                val view = parent.getChildAt(i)
                val isLeft = (i % 2 == 0) || (!isEven&&i==childCount-1)

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

                if (isLeft ) {
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