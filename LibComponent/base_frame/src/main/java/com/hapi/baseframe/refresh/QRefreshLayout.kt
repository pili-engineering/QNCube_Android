package com.hapi.baseframe.refresh

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.AbsListView
import android.widget.FrameLayout
import android.widget.OverScroller
import androidx.core.view.*
import androidx.customview.widget.ViewDragHelper
import kotlin.math.abs

class QRefreshLayout : FrameLayout, NestedScrollingParent, NestedScrollingChild {
    interface OnRefreshListener {
        fun onStartRefresh()
        fun onStartLoadMore()
    }

    private val TAG = "QSwipeRefreshLayout"
    private val mNestedScrollingChildHelper by lazy { NestedScrollingChildHelper(this) }
    private val mNestedScrollingParentHelper by lazy { NestedScrollingParentHelper(this) }
    private val configuration by lazy { ViewConfiguration.get(context) }
    private val mMinimumVelocity: Int by lazy { configuration.scaledMinimumFlingVelocity }
    private val mScroller: OverScroller by lazy { OverScroller(context) }
    private lateinit var mScrollView: View
    private var mUpTotalUnconsumed = 0f//下拉距离
    private var mDownTotalUnconsumed = 0f
    private var isNestedScrollInProgress = false
    var isLoadMoreEnable = true
    var isReFreshEnable = true
    var isRefreshing: Boolean = false
    var onRefreshListener: OnRefreshListener? = null
        private set
    var isLoading: Boolean = false
        private set

    var refreshView: IRefreshView
        private set

    var loadMoreView: ILoadView //上拉距离
        private set

    fun setRefreshListener(refreshListener: OnRefreshListener) {
        onRefreshListener = refreshListener;
    }

    fun startRefresh() {
        if (isRefreshing || isLoading) {
            return
        }
        isRefreshing = true
        refreshView.recoverAnimator?.cancel()
        if (refreshView.isFloat()) {
            refreshView.recoverAnimator =
                ObjectAnimator.ofFloat(
                    refreshView.getAttachView(),
                    "translationY",
                    refreshView.getAttachView().translationY,
                    refreshView.getFreshTopHeight().toFloat()
                )
            refreshView.recoverAnimator?.duration = 300
            refreshView.recoverAnimator?.start()

        } else {
            scrollTo(0, (refreshView.getFreshTopHeight()))
        }
        refreshView.onPointUp(true)
        onRefreshListener?.onStartRefresh()
    }

    fun finishRefresh(isEmpty: Boolean) {
        if (!isRefreshing) {
            return
        }
        isRefreshing = false
        refreshView.onFinishRefresh()
        if (refreshView.isFloat()) {
            refreshView.recoverAnimator?.cancel()
            refreshView.recoverAnimator =
                ObjectAnimator.ofFloat(
                    refreshView.getAttachView(),
                    "translationY",
                    refreshView.getAttachView().translationY,
                    -refreshView.getFreshHeight().toFloat()
                )
            refreshView.recoverAnimator?.duration = 300
            refreshView.recoverAnimator?.start()
        }
        if (!isEmpty) {
            loadMoreView.checkHideNoMore()
        }
        if (scrollY != 0) {
            scrollTo(0, 0)
        }
    }

    fun finishLoadMore(
        noMore: Boolean,
        goneIfNoData: Boolean,
        scrollToNextPageVisibility: Boolean
    ) {
        if (!isLoading) {
            return
        }
        isLoading = false
        if (noMore) {
            //没有更多了
            if (goneIfNoData) {
                //不可见
                loadMoreView.onFinishLoad(false)
                scrollTo(0, 0)
            } else {
                loadMoreView.onFinishLoad(true)
            }
        } else {
            //还有更多
            loadMoreView.onFinishLoad(false)
            if (scrollToNextPageVisibility) {
                mScrollView.scrollBy(0, scrollY)
            }
            scrollTo(0, 0)
        }
    }

    /**
     * Set refresh view
     *设置刷新头
     * @param refreshView
     */
    fun setRefreshHeader(refreshView: IRefreshView) {
        val originHeader = this.refreshView.getAttachView()
        val index = indexOfChild(originHeader)
        removeView(originHeader)
        this.refreshView = refreshView
        addView(
            this.refreshView.getAttachView().apply {
                translationY =
                    -this@QRefreshLayout.refreshView.getFreshHeight().toFloat()
            }, index,
            LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        )
        this.refreshView.getAttachView().bringToFront()
    }

    /**
     * Set load view
     * 设置加载控件
     * @param loadViewView
     */
    fun setLoadFooter(loadViewView: ILoadView) {
        val originFooter = loadMoreView.getAttachView()
        val index = indexOfChild(originFooter)
        removeView(originFooter)
        loadMoreView = loadViewView
        addView(
            loadMoreView.getAttachView(), index,
            LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        )
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        refreshView = DefaultRefreshView(context)
        addView(
            refreshView.getAttachView().apply {
                translationY =
                    -refreshView.getFreshHeight().toFloat()
            },
            LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        )
        refreshView.getAttachView().bringToFront()
        loadMoreView = DefaultLoadView(context)
        addView(
            loadMoreView.getAttachView(),
            LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        )
        isChildrenDrawingOrderEnabled = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        loadMoreView.getAttachView().translationY = measuredHeight.toFloat()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        mScrollView = getChildAt(2)
        this.refreshView.getAttachView().bringToFront()
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return ((isLoadMoreEnable || isReFreshEnable)
                && !isRefreshing
                //  && !isLoading
                && nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0)
    }

    override fun onNestedScrollAccepted(child: View, target: View, nestedScrollAxes: Int) {
        // Reset the counter of how much leftover scroll needs to be consumed.
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes)
        // Dispatch up to the nested parent
        startNestedScroll(nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL)
        mUpTotalUnconsumed = 0f
        mDownTotalUnconsumed = scrollY.toFloat()
        isNestedScrollInProgress = true
    }

    //ziview 滑动之后
    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int
    ) {
        //dy 《 0 下拉 dy 》0 上拉
        val mParentOffsetInWindow = IntArray(2)
        dispatchNestedScroll(
            dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
            mParentOffsetInWindow
        )
        //剩余没有消费的y
        val y = dyUnconsumed + mParentOffsetInWindow[1]
        if (isRefreshing) {
            return
        }
        //下拉 并且recyview刚到顶端
        if (y < 0 && !canChildScrollUp() && isReFreshEnable && !isLoading) {
            moveRefreshViewDown(y)
        } else if (y > 0 && !canChildScrollDown() && isLoadMoreEnable && canChildScrollUp()) {
            //上拉 刚到低端
            moveMoreViewUP(y)
        }
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        //dy 《 0 下拉 dy 》0 上拉
        if (isLoadMoreEnable || isReFreshEnable) {
            //子view滑动之前
            if (dy > 0 && mUpTotalUnconsumed < 0) {
                //  刷新已经出现 的时侯 上拉
                //dy 》0 上拉  刷新已经出现
                moveRefreshViewUp(dy, consumed)
            } else if (dy < -1 && mDownTotalUnconsumed > 0) {
                //dy < -1 下拉 mLoadViewController.currentHeight 》0 已经出现
                if (dy + mDownTotalUnconsumed < 0) {
                    // mDownTotalUnconsumed 上拉了5   dy -6 下拉-6
                    consumed[1] = -mDownTotalUnconsumed.toInt()
                    mDownTotalUnconsumed = 0f
                } else {
                    // mDownTotalUnconsumed 上拉了5   dy -4 下拉-4
                    mDownTotalUnconsumed += dy.toFloat()
                    consumed[1] = dy //消耗-4
                }
                val consumedByFooter =
                    loadMoreView.onPointMove(mDownTotalUnconsumed, consumed[1].toFloat())
                consumed[1] = consumedByFooter.toInt()
                scrollBy(0, consumed[1])
            }
        }
        val parentConsumed = IntArray(2)
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0]
            consumed[1] += parentConsumed[1]
        }
    }

    override fun onStopNestedScroll(target: View) {
        isNestedScrollInProgress = false
        mNestedScrollingParentHelper.onStopNestedScroll(target)
        //dy 《 0 下拉 dy 》0 上拉
        if (mUpTotalUnconsumed < 0) {
            //  y 《 0 下拉 刷新已经出现
            //下拉
            checkRefreshViewPointUp()
        }
        // dy 》0 上拉
        if (mDownTotalUnconsumed > 0 && !loadMoreView.isShowLoadMore) {

            if (mDownTotalUnconsumed >= loadMoreView.getFreshHeight()) {
                isLoading = true
                onRefreshListener?.onStartLoadMore()
                loadMoreView.onPointUp(true)
                scrollTo(0, loadMoreView.getFreshHeight())
            } else {
                isLoading = false
                scrollTo(0, 0)
                loadMoreView.onPointUp(false)
            }
            mDownTotalUnconsumed = 0f
        }
        stopNestedScroll()
    }

    private fun moveRefreshViewUp(dy: Int, consumed: IntArray) {
        if (dy > -mUpTotalUnconsumed) {
            // 已经下拉-5  上拉dy 6
            // y 》0 上拉
            consumed[1] = -mUpTotalUnconsumed.toInt() //消费 5
            mUpTotalUnconsumed = 0f
        } else {
            //已经 已经上拉5 上拉dy 4
            mUpTotalUnconsumed += dy.toFloat()
            consumed[1] = dy  //消费4
        }
        val dyNew = refreshView.onPointMove(mUpTotalUnconsumed, consumed[1].toFloat())
        consumed[1] = dyNew.toInt()
        //向上滑动
        if (refreshView.isFloat()) {
            refreshView.getAttachView().translationY -= consumed[1]
        } else {
            scrollBy(0, consumed[1])
        }
    }

    private fun checkRefreshViewPointUp() {
        if (-mUpTotalUnconsumed > refreshView.getFreshHeight()) {
            //开始刷新
            refreshView.onPointUp(true)
            isRefreshing = true
            onRefreshListener?.onStartRefresh()
        } else {
            isRefreshing = false
            //恢复UI
            refreshView.onPointUp(false)
        }
        mUpTotalUnconsumed = 0f
        refreshView.recoverAnimator?.cancel()
        if (refreshView.isFloat()) {
            refreshView.recoverAnimator =
                ObjectAnimator.ofFloat(
                    refreshView.getAttachView(),
                    "translationY",
                    refreshView.getAttachView().translationY,
                    if (!isRefreshing) {
                        -refreshView.getFreshHeight().toFloat()
                    } else {
                        refreshView.getFreshTopHeight().toFloat()
                    }
                )
            refreshView.recoverAnimator?.duration = 300
            refreshView.recoverAnimator?.start()

        } else {
            scrollTo(
                0, if (!isRefreshing) {
                    0
                } else {
                    refreshView.getFreshTopHeight()
                }
            )
        }
    }

    private fun moveRefreshViewDown(y: Int) {
        //下拉距离 <0
        // mUpTotalUnconsumed += dy
        //告诉子view开始下拉距离
        val moveY = refreshView.onPointMove(mUpTotalUnconsumed, y.toFloat())
        mUpTotalUnconsumed += moveY

        if (refreshView.isFloat()) {
            refreshView.getAttachView().translationY -= moveY
            Log.d(
                "moveRefreshViewDown",
                "${refreshView.getAttachView().translationY} $mUpTotalUnconsumed"
            )
        } else {
            scrollBy(0, moveY.toInt())
        }
    }

    //
    private fun moveMoreViewUP(y: Int) {
        // y 》0 上拉
        val dy = loadMoreView.onPointMove(mDownTotalUnconsumed, y.toFloat())
        mDownTotalUnconsumed += dy
        loadMoreView.onPointMove(mDownTotalUnconsumed, dy)
        scrollBy(0, dy.toInt())
        //Log.d(TAG, "moveMoreView  scrollBy(0, dy)${dy}")
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled
    }

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        mNestedScrollingChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return mNestedScrollingChildHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return mNestedScrollingChildHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?
    ): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedScroll(
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            offsetInWindow
        )
    }

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?
    ): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedFling(
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun onNestedFling(
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        if (!consumed) {
            flingWithNestedDispatch(velocityX, velocityY)
            return true
        }
        return dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        return flingWithNestedDispatch(velocityX, velocityY)
    }

    override fun getNestedScrollAxes(): Int {
        return mNestedScrollingParentHelper.nestedScrollAxes
    }

    private fun flingWithNestedDispatch(velocityX: Float, velocityY: Float): Boolean {
        val canFling = abs(velocityY) > mMinimumVelocity
        if (!dispatchNestedPreFling(velocityX, velocityY)) {
            dispatchNestedFling(velocityX, velocityY, canFling)
            if (canFling) {
                return fling(velocityY)
            }
        }
        return false
    }

    private fun fling(velocityY: Float): Boolean {
        //dy 《 0 下拉 dy 》0 上拉
        if (velocityY <= 0) {
            //下拉惯性
            if (scrollY > 0) {
                scrollTo(0, 0)
            }
            mScroller.abortAnimation()
            return false
        }
        mScroller.abortAnimation()
        mScroller.computeScrollOffset()
        if (canChildScrollUp() && isLoadMoreEnable) {
            mScroller.fling(
                0,
                scrollY,
                0,
                (velocityY).toInt(),
                0,
                0,
                Int.MIN_VALUE,
                Int.MAX_VALUE
            )
        }
        ViewCompat.postInvalidateOnAnimation(this)
        return false
    }

    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if ((isLoading || loadMoreView.isShowLoadMore) && canChildScrollUp() && !canChildScrollDown()) {
                if (mScroller.currY >= loadMoreView.getFreshHeight()) {
                    scrollTo(0, loadMoreView.getFreshHeight())
                    mScroller.abortAnimation()
                } else {
                    scrollTo(0, mScroller.currY)
                }
                return
            }
            if (!canChildScrollDown() && isLoadMoreEnable && canChildScrollUp()) {
                if (!loadMoreView.isShowLoadMore) {
                    loadMoreView.onPointMove(
                        loadMoreView.getFreshHeight().toFloat(),
                        loadMoreView.getFreshHeight().toFloat()
                    )
                    scrollBy(0, loadMoreView.getFreshHeight())
                    isLoading = true
                    loadMoreView.onPointUp(true)
                    onRefreshListener?.onStartLoadMore()
                }
                mScroller.abortAnimation()
            }
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    /**
     * Can child scroll up
     * 下拉
     * @return
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun canChildScrollUp(): Boolean {
        return if (Build.VERSION.SDK_INT < 14) {
            if (mScrollView is AbsListView) {
                val absListView = mScrollView as AbsListView
                (absListView.childCount > 0
                        && (absListView.firstVisiblePosition > 0 || absListView.getChildAt(0)
                    .top < absListView.paddingTop))
            } else {
                mScrollView.canScrollVertically(-1) || mScrollView.scrollY > 0
            }
        } else {
            mScrollView.canScrollVertically(-1)
        }
    }

    /**
     * target view 是否能向下滑动
     * 上拉
     * @return
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun canChildScrollDown(): Boolean {
        return if (Build.VERSION.SDK_INT < 14) {
            if (mScrollView is AbsListView) {
                val absListView = mScrollView as AbsListView
                val count = absListView.childCount
                val position = absListView.lastVisiblePosition
                count > position + 1 || absListView.getChildAt(position).bottom <= absListView.paddingBottom
            } else {
                mScrollView.canScrollVertically(1)
            }
        } else {
            mScrollView.canScrollVertically(1)
        }
    }

    private var lastTouchY = 0f
    private var pointDownY = 0f
    private val mTouchSlop by lazy { configuration.scaledTouchSlop }
    private var mActivePointerId = ViewDragHelper.INVALID_POINTER

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!isRefreshing && !isLoading && isReFreshEnable && !isNestedScrollInProgress && !canChildScrollUp()) {
            val y = ev.y
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    mActivePointerId = ev.getPointerId(0)
                    lastTouchY = y
                    pointDownY = y
                }
                MotionEvent.ACTION_MOVE -> {
                    val dy = lastTouchY - y
                    lastTouchY = y
                    if (dy < 0 && abs(pointDownY - y) > mTouchSlop) {
                        return true
                    }
                }
                MotionEvent.ACTION_UP -> {
                    lastTouchY = 0f
                    pointDownY = 0f
                    mActivePointerId = ViewDragHelper.INVALID_POINTER
                }
                MotionEvent.ACTION_CANCEL -> {
                    lastTouchY = 0f
                    pointDownY = 0f
                    mActivePointerId = ViewDragHelper.INVALID_POINTER
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (!isRefreshing && !isLoading && isReFreshEnable && !isNestedScrollInProgress && !canChildScrollUp()) {
            val y = ev.y
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    mActivePointerId = ev.getPointerId(0)
                    lastTouchY = y
                    pointDownY = y
                }
                MotionEvent.ACTION_MOVE -> {
                    val dy = lastTouchY - y
                    lastTouchY = y
                    if ((abs(y - pointDownY) > mTouchSlop) || mUpTotalUnconsumed != 0f) {
                        if (dy < 0) {
                            //下拉
                            moveRefreshViewDown(dy.toInt())
                        } else {
                            //上拉
                            if (dy > 0 && mUpTotalUnconsumed < 0) {
                                //  刷新已经出现 的时侯 上拉
                                //dy 》0 上拉  刷新已经出现
                                moveRefreshViewUp(dy.toInt(), IntArray(2))
                            }
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (mUpTotalUnconsumed < 0) {
                        //  y 《 0 下拉 刷新已经出现
                        //下拉
                        checkRefreshViewPointUp()
                    }
                    lastTouchY = 0f
                    pointDownY = 0f
                    mActivePointerId = ViewDragHelper.INVALID_POINTER
                }
                MotionEvent.ACTION_CANCEL -> {
                    if (mUpTotalUnconsumed < 0) {
                        //  y 《 0 下拉 刷新已经出现
                        //下拉
                        checkRefreshViewPointUp()
                    }
                    lastTouchY = 0f
                    pointDownY = 0f
                    mActivePointerId = ViewDragHelper.INVALID_POINTER
                }
            }
        }
        return if (mUpTotalUnconsumed != 0f) {
            super.onTouchEvent(ev)
            true
        } else {
            super.onTouchEvent(ev)
        }
    }
}