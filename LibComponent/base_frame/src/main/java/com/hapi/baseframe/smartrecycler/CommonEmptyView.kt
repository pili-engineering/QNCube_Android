package com.hapi.baseframe.smartrecycler

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.hapi.base_mvvm.R

/**
 * 通用empty 待替换ui设计
 */
class CommonEmptyView : FrameLayout, IEmptyView {
    /**
     * 获取当前错误状态
     *
     * @return
     */
    var errorState = 0
        private set
    private var strNoDataContent = ""
    var img: ImageView? = null
        private set
    var emptyText: TextView? = null
        private set

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private var emptyIcon = R.drawable.kit_pic_empty
    private var emptyNoNetIcon = R.drawable.kit_pic_empty_network
    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.view_custom_empty, this)
        visibility = GONE
        // setBackgroundColor(-1);
        img = findViewById(R.id.img)
        emptyText = findViewById(R.id.empty_text)
        setOnClickListener { }
    }

    /**
     * 設置背景
     */
    fun setEmptyIcon(imgResource: Int) {
        emptyIcon = imgResource
        img!!.setImageResource(imgResource)
    }

    fun setEmptyNoNetIcon(imgResource: Int) {
        emptyNoNetIcon = imgResource
    }

    /**
     * 設置内容
     */
    fun setEmptyTips(noDataContent: String) {
        strNoDataContent = noDataContent
        if (emptyText != null) {
            emptyText!!.text = strNoDataContent
        }
    }

    override fun getContentView(): View {
        return this
    }

    /**
     * 根据状态設置当前view
     *
     * @param i
     */
    override fun setStatus(i: Int) {
        if (refreshingView != null) {
            refreshingView!!.visibility = GONE
        }
        when (i) {
            IEmptyView.NETWORK_ERROR -> {
                visibility = VISIBLE
                errorState = IEmptyView.NETWORK_ERROR
                emptyText!!.text = "网络错误"
                img!!.setImageResource(emptyNoNetIcon)
                img!!.visibility = VISIBLE
            }
            IEmptyView.NODATA -> {
                visibility = VISIBLE
                errorState = IEmptyView.NODATA
                img!!.setImageResource(emptyIcon)
                img!!.visibility = VISIBLE
                refreshEmptyView()
            }
            IEmptyView.HIDE_LAYOUT -> visibility = GONE
            IEmptyView.START_REFREASH_WHEN_EMPTY -> if (refreshingView != null) {
                visibility = VISIBLE
                refreshingView!!.visibility = VISIBLE
            }
            else -> {}
        }
    }

    private var refreshingView: View? = null

    fun setRefreshingView(view: View?) {
        refreshingView = view
        refreshingView!!.visibility = GONE
        addView(refreshingView)
    }

    private fun refreshEmptyView() {
        emptyText!!.text = if (TextUtils.isEmpty(strNoDataContent)) "" else strNoDataContent
    }

    override fun setVisibility(visibility: Int) {
        if (visibility == GONE) {
            errorState = IEmptyView.HIDE_LAYOUT
        }
        super.setVisibility(visibility)
    }

}