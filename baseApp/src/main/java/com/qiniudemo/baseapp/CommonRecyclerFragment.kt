package com.qiniudemo.baseapp

import com.hapi.refresh.SmartRecyclerView
import kotlinx.android.synthetic.main.act_smart_recy.*

abstract class CommonRecyclerFragment<T> : RecyclerFragment<T>() {
    override val mSmartRecycler: SmartRecyclerView
            by lazy { smartRecyclerView }

    override fun getLayoutId(): Int {
        return com.qiniu.baseapp.R.layout.act_smart_recy
    }

}