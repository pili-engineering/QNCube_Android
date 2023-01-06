package com.hapi.baseframe.smartrecycler

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.hapi.baseframe.refresh.QRefreshLayout
import com.hapi.baseframe.smartrecycler.IEmptyView.*

/**
 * 上拉下拉帮助类
 *
 */
open class SmartRefreshHelper<T>(
    val context: Context,
    val adapter: IAdapter<T>,
    private val recycler_view: RecyclerView,
    private val refresh_layout: QRefreshLayout,
    private val emptyCustomView: IEmptyView?,
    private val isNeedLoadMore: Boolean = true,
    private val refreshNeed: Boolean = true,
    /**
     * 刷新回调
     */
    private val fetcherFuc: (page: Int) -> Unit
) {

    private var currentPage = 0
    private var eachPageSize: Int = 0

    val isRefreshing: Boolean
        get() = refresh_layout.isRefreshing
    val isLoadMoreing: Boolean
        get() = refresh_layout.isLoading

    init {
        refresh_layout.isLoadMoreEnable = (isNeedLoadMore)
        refresh_layout.isReFreshEnable = (refreshNeed)
        refresh_layout.setRefreshListener(object : QRefreshLayout.OnRefreshListener {
            override fun onStartRefresh() {
                startRefresh()
            }

            override fun onStartLoadMore() {
                loadMore()
            }
        })
    }

    private fun startRefresh() {
        if (adapter.isCanShowEmptyView()) {
            emptyCustomView?.setStatus(START_REFREASH_WHEN_EMPTY)
        }
        fetcherFuc(0)
    }

    /**
     * 获取到分页数据 设置下拉刷新和上拉的状态
     */
    fun onFetchDataFinish(data: MutableList<T>?, goneIfNoData: Boolean) {
        onFetchDataFinish(data, goneIfNoData, sureLoadMoreEnd = false)
    }

    private var listSize = 0
    fun onFetchDataFinish(data: MutableList<T>?, goneIfNoData: Boolean, sureLoadMoreEnd: Boolean) {
        if (data != null) {
            if (currentPage == 0 && isRefreshing) {
                eachPageSize = data.size
            }
            if (isLoadMoreing) {
                currentPage++
                adapter.addDataList(data)
                listSize += data.size
            } else {
                adapter.setNewDataList((data))
                listSize = 0
                currentPage = 0
            }
        }
        if (isRefreshing) {
            refresh_layout.finishRefresh(data?.isEmpty() ?: true)
        } else {
            val isNeedLoadMore = if (sureLoadMoreEnd) {
                true
            } else {
                data?.isEmpty() ?: true
            }
            refresh_layout.finishLoadMore(isNeedLoadMore, goneIfNoData, true)
        }
        refreshEmptyView(NODATA)
    }

    /**
     * 加载数据失败
     */
    fun onFetchDataError() {
        if (isRefreshing) {
            refresh_layout.finishRefresh(false)
        } else {
            refresh_layout.finishLoadMore(noMore = false, true, scrollToNextPageVisibility = false)
        }
        val disconnected = !NetUtil.isNetworkAvailable(recycler_view.context)
        if (disconnected) {
            refreshEmptyView(NETWORK_ERROR)
        } else {
            refreshEmptyView(NODATA)
        }
    }

    private fun refreshEmptyView(type: Int) {
        if (adapter.isCanShowEmptyView()) {
            //if (adapter.data.isEmpty() && adapter.headerLayoutCount + adapter.footerLayoutCount == 0) {
            emptyCustomView?.setStatus(type)
        } else {
            emptyCustomView?.setStatus(HIDE_LAYOUT)
        }
    }

    private fun loadMore() {
        fetcherFuc(currentPage + 1)
    }

    fun refresh() {
        refresh_layout.startRefresh()
    }
}