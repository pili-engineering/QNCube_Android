package com.qiniudemo.baseapp

import com.hapi.base_mvvm.refresh.SmartRecyclerView
import kotlinx.android.synthetic.main.act_smart_recy.*

abstract class ComonRecyActivity<R> : RecyclerActivity<R>() {

    override val mSmartRecycler: SmartRecyclerView
            by lazy { smartRecyclerView }


    override fun getLayoutId(): Int {
        return com.qiniu.baseapp.R.layout.act_smart_recy
    }
}