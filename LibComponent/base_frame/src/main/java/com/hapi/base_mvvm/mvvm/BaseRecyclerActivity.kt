package com.hapi.base_mvvm.mvvm

import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.hapi.base_mvvm.R
import com.hapi.base_mvvm.refresh.IEmptyView
import com.hapi.base_mvvm.refresh.SmartRecyclerView

abstract class BaseRecyclerActivity<T> : BaseVmActivity() {

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

    open fun isRefreshAtOnStart():Boolean{
        return true
    }

    open fun isRefreshAtOnResume():Boolean{
        return false
    }

    override fun initViewData() {
        mSmartRecycler.recyclerView.layoutManager = layoutManager
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

    override fun onResume() {
        super.onResume()
        if(isRefreshAtOnResume()){
            mSmartRecycler.startRefresh()
        }
    }
}