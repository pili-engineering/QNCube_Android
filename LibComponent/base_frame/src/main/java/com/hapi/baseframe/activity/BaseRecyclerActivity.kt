package com.hapi.baseframe.activity

import androidx.recyclerview.widget.RecyclerView
import com.hapi.base_mvvm.R
import com.hapi.baseframe.smartrecycler.IAdapter
import com.hapi.baseframe.smartrecycler.IEmptyView
import com.hapi.baseframe.smartrecycler.SmartRecyclerView

abstract class BaseRecyclerActivity<T> : BaseFrameActivity() {

    protected val preLoadNumber: Int = 1
    open val mSmartRecycler: SmartRecyclerView by lazy {
        findViewById(R.id.smartRecycler)
    }

    override fun getLayoutId(): Int {
        return R.layout.layout_smartrecycler
    }

    abstract val adapter: IAdapter<T>
    abstract val layoutManager: RecyclerView.LayoutManager
    /**
     * 刷新回调
     */
    abstract val fetcherFuc: ((page: Int) -> Unit)

    open fun loadMoreNeed(): Boolean {
        return true
    }

    open fun reFreshNeed(): Boolean {
        return true
    }

    open fun isRefreshAtOnStart():Boolean{
        return true
    }

    open fun isRefreshAtOnResume():Boolean{
        return false
    }

    override fun init() {
        mSmartRecycler.recyclerView.layoutManager = layoutManager
        mSmartRecycler.setUp(
            adapter,
            loadMoreNeed(),
            reFreshNeed(),
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