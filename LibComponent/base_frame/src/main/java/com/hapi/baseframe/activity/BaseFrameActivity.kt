package com.hapi.baseframe.activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.hapi.base_mvvm.R
import com.hapi.baseframe.uitil.ClickUtil
import com.hapi.baseframe.uitil.SoftInputUtil
import com.hipi.vm.LoadingObserverView


/**
 * activity 基础类  封装toolbar
 *
 */
abstract class BaseFrameActivity : AppCompatActivity(), Toolbar.OnMenuItemClickListener ,LoadingObserverView{

    /**
     * 根布局
     */
    private lateinit var mRootView: ViewGroup

    var mToolbar: Toolbar? = null
        private set
    private var centerTitle: TextView? = null

    private var mContentView: View? = null

    @SuppressLint("PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mInflater = getLayInflater()
        mContentView = createContainerView()
        if (mContentView == null) {
            finish()
            return
        }
        onActivityCreateStart()
        changeTheme()
        setContentView(mContentView)
        mRootView = findViewById(android.R.id.content)
        if (isToolBarEnable()) {
            var toolbarLayoutId = 0
            toolbarLayoutId = if (isTitleCenter()) {
                R.layout.frame_base_toolbar_center
            } else {
                R.layout.frame_base_toolbar_normal
            }
            mToolbar = mInflater.inflate(toolbarLayoutId, null) as Toolbar
            val barHeight =
                resources.getDimensionPixelOffset(R.dimen.abc_action_bar_default_height_material)
            mRootView.addView(
                mToolbar, ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    barHeight
                )
            )
            if (isTitleCenter()) {
                centerTitle = mToolbar?.findViewById(R.id.toolbar_title_tv)
            }
            val mode = requestToolbarMode()
            if (mode == ToolbarMode.Layer) {
                val params = mContentView!!.layoutParams as FrameLayout.LayoutParams
                params.let {
                    it.setMargins(
                        params.leftMargin,
                        barHeight,
                        params.rightMargin,
                        params.bottomMargin
                    )
                    mContentView!!.layoutParams = it
                }
            }

            setToolbarTitle(getInitToolBarTitle())
            setupToolbar()
        }
        init()
    }

    abstract fun init()
    abstract fun getLayoutId(): Int

    @ToolbarMode
    open fun requestToolbarMode(): Int {
        return ToolbarMode.Layer
    }

    open fun getInitToolBarTitle(): String {
        return title as String
    }

    open fun getInittittleColor(): Int {
        return -100
    }

    open fun initTitleTextAppearance(): Int {
        return 0;
    }

    fun setTittleColor(color: Int) {
        if (isTitleCenter()) {
            centerTitle?.setTextColor(color)
        } else {
            if (mToolbar != null) {
                mToolbar?.setTitleTextColor(color)
            }
        }
    }

    open fun getInitCenterTittleSize(): Float {
        return -1f
    }

    fun setCenterTittleSize(px: Float) {
        if (isTitleCenter()) {
            centerTitle?.textSize = px
        } else {

        }
    }

    open fun setTitleTextAppearance(
        context: Context?,
        @StyleRes resId: Int
    ) {
        if (isTitleCenter()) {
            centerTitle?.setTextAppearance(context, resId)
        } else {
            if (mToolbar != null) {
                mToolbar?.setTitleTextAppearance(context, resId)
            }
        }
    }

    /**
     * 是否展示toolbar
     *
     * @return
     */
    open fun isToolBarEnable(): Boolean {
        return true
    }

    /**
     * 设置是否toolbar标题居中
     *
     * @return
     */
    open fun isTitleCenter(): Boolean {
        return false
    }

    /**
     * 是否显示返回键。默认显示
     *
     * @return
     */
    open fun homeAsUpEnable(): Boolean {
        return true
    }

    /**
     * 开始创建activity界面
     */
    open fun onActivityCreateStart() {}

    /**
     * menu layout id
     *
     * @return
     */
    open fun requestMenuId(): Int {
        return 0
    }

    open fun changeTheme() {}

    /**
     * 返回键
     *
     * @return
     */
    @DrawableRes
    open fun requestNavigationIcon(): Int {
        return -1
    }

    /**
     * 设置toolbar 背景样式
     */
    open fun requestToolBarBackground(): Drawable? {
        return null
    }

    private fun getLayInflater(): LayoutInflater {
        return LayoutInflater.from(this)
    }

    /**
     * 创建containerview
     *
     * @param inflater
     * @param layoutResID 你自己的布局id
     * @return
     */
    open fun createContainerView(): View? {
        val layoutResID = getLayoutId()
        return if (layoutResID <= 0) {
            null
        } else getLayInflater().inflate(layoutResID, null)
    }

    /**
     * replace the inner toolbar, if the new toolbar is same as inner's , it will do nothing
     */
    protected fun setToolbar(toolbar: Toolbar) {
        if (mToolbar !== toolbar) {
            if (mToolbar != null) {
                mToolbar?.visibility = View.GONE
            }
            this.mToolbar = toolbar
            setupToolbar()
        }
    }

    /**
     * setup  toolbar
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupToolbar() {
        if (mToolbar != null) {
            setSupportActionBar(mToolbar)
            //显示返回键
            supportActionBar?.setDisplayHomeAsUpEnabled(homeAsUpEnable())
            //可响应返回键点击事件
            supportActionBar?.setDisplayShowHomeEnabled(true)
            //返回键
            requestNavigationIcon().let {
                if (it > 0) {
                    supportActionBar?.setHomeAsUpIndicator(it)
                }
            }

            //是否展示默认标题
            if (isTitleCenter()) {
                supportActionBar?.setDisplayShowTitleEnabled(false)
                if (getInitCenterTittleSize() > 0) {
                    setCenterTittleSize(getInitCenterTittleSize())
                }
            } else {
                if (initTitleTextAppearance() > 0) {
                    setTitleTextAppearance(this, initTitleTextAppearance())
                }
            }
            requestToolBarBackground()?.let {
                mToolbar?.background = it
            }

            if (getInittittleColor() != -100) {
                setTittleColor(getInittittleColor())
            }
            mToolbar?.setOnTouchListener(toolbarTouch)
            mToolbar?.setOnMenuItemClickListener(this)
            mToolbar?.setNavigationOnClickListener(View.OnClickListener {
                if (ClickUtil.isClickAvalible()) {
                    onBackPressed()
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuId = requestMenuId()
        if (menuId > 0) {
            menuInflater.inflate(menuId, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * 监听menu的点击事件
     */
    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> {
            }
        }
        return false
    }

    /**
     * 监听toolbar的touch事件，隐藏键盘
     */
    @SuppressLint("ClickableViewAccessibility")
    private val toolbarTouch = View.OnTouchListener { v, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
            hideSoftInputView()
        }
        false
    }

    fun setToolbarTitle(title: CharSequence) {
        if (mToolbar != null) {
            if (isTitleCenter()) {
                centerTitle?.text = title
            } else {
                if (mToolbar != null) {
                    mToolbar?.title = title
                }
            }
        }
    }

    /**
     * 隐藏软键盘
     */
    fun hideSoftInputView() {
        SoftInputUtil.hideSoftInputView(this)
    }
}