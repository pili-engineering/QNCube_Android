package com.hapi.base_mvvm.mvvm

import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.hapi.refresh.IEmptyView
import com.hapi.refresh.SmartRecyclerView

import com.scwang.smartrefresh.layout.api.RefreshHeader

abstract class BaseRecyclerFragment<T> : BaseVmFragment() {

    protected val preLoadNumber: Int = 1
    abstract val mSmartRecycler: SmartRecyclerView

    abstract val adapter: BaseQuickAdapter<T, *>
    abstract val layoutManager: RecyclerView.LayoutManager
    /**
     * 刷新回调
     */
    abstract val fetcherFuc: ((page: Int) -> Unit)

    open fun loadMoreNeed(): Boolean {
        return true
    }

    open fun refreashNeed(): Boolean {
        return true
    }

    /**
     * 通用emptyView
     */
    abstract fun getEmptyView(): IEmptyView?

    abstract fun getGetRefreshHeader(): RefreshHeader
    open fun isRefreshAtOnStart():Boolean{
        return true
    }

    override fun initViewData() {
        mSmartRecycler.recyclerView.layoutManager = layoutManager
        mSmartRecycler.setReFreshHearfer(getGetRefreshHeader())
        mSmartRecycler.setUp(
            adapter,
            getEmptyView(),
            preLoadNumber,
            loadMoreNeed(),
            refreashNeed(),
            fetcherFuc
        )
        if(isRefreshAtOnStart()){
            mSmartRecycler.startRefresh()
        }
    }
}