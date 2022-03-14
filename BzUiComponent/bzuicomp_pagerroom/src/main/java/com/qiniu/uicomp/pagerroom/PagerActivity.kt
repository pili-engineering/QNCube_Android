package com.qiniu.uicomp.pagerroom

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.OrientationHelper
import com.hapi.base_mvvm.mvvm.BaseVmActivity
import kotlinx.android.synthetic.main.activity_abs_audio_room.*

abstract class PagerActivity <T:ShowCoverAble> : BaseVmActivity() {

    /**
     * 默认房间适配器
     */
    open var adapter: RoomAdapter<T> = RoomAdapter()

    abstract var currentPosition: Int
    abstract var listData: ArrayList<T>
    /**
     * 真正房间布局
     */
    abstract val mRoomContentView: ViewGroup
    /**
     * 覆盖层
     */
    open fun getCoverLayout(parent: ViewGroup?): View? {
        return null
    }

    /**
     * 切换
     */
    private val mLayoutManager by lazy {
        PagerLayoutManager(this, OrientationHelper.VERTICAL)
            .apply {
                setViewGroup(mRoomContentView, object : PagerLayoutManager.IreloadInterface {
                    override fun onDestroyPage(isNext: Boolean, position: Int, view: View?) {
                        onPageLeave(isNext,position,view)
                    }
                    override fun onReloadPage(position: Int, isBottom: Boolean, view: View?) {
                        var p = position
                        if (p == -1000) {
                            p = currentPosition
                            currentPosition = position
                            onSelect(p, view, true)
                            return
                        }
                        if (p != currentPosition) {
                            currentPosition = position
                            onSelect(p, view, false)
                        }
                    }
                })
            }
    }

    /**
     * @param isFirstTime 首次进入
     */
    abstract fun onSelect(position: Int, v: View?, isFist: Boolean)
    abstract fun onPageLeave(isNext: Boolean, position: Int, view: View?)

    override fun isToolBarEnable(): Boolean {
        return false
    }

    abstract fun initPagerData();
    override fun initViewData() {
        initPagerData()
        getCoverLayout(flCoverContent)?.let {
            flCoverContent.addView(it)
            flCoverContent.visibility = View.VISIBLE
        }
        initRecyView()
        initOtherView()
    }
    open fun initOtherView() {}

    private fun initRecyView() {
        recyclerView.layoutManager = mLayoutManager
        recyclerView.animation = null
        recyclerView.adapter = adapter
        adapter.setNewData(listData)
        recyclerView.scrollToPosition(currentPosition)
        Log.d("hhq", " scrollToPosition   position---$currentPosition")
        mLayoutManager.scrollToPositionWithOffset(currentPosition, 0)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_abs_audio_room
    }

}