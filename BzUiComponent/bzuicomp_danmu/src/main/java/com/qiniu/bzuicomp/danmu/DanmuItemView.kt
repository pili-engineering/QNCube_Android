package com.qiniu.bzuicomp.danmu

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.qiniu.compui.trackview.TrackView

interface IDanmuItemView {

    var endCall: (() -> Unit)?

    // 可以开始下一个弹幕了
    var nextAvalibeCall: (() -> Unit)?
    fun clear()
    fun start()
    fun getView(): View
}

class DanmuItemView : FrameLayout, IDanmuItemView {

    val animatorTime = 6000L
    override var endCall: (() -> Unit)? = null

    // 可以开始下一个弹幕了
    override var nextAvalibeCall: (() -> Unit)? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, mAttributeSet: AttributeSet?) : super(context, mAttributeSet) {
        val view = LayoutInflater.from(context).inflate(R.layout.item_danmu, this, false)
        addView(view)
    }

    private val tansAni by lazy {
        val transX = ObjectAnimator.ofFloat(
            this,
            "translationX",
            parentWidth.toFloat(),
            -this.measuredWidth.toFloat()
        )
        transX.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                endCall?.invoke()
            }

            override fun onAnimationCancel(animation: Animator) {
            }

            override fun onAnimationStart(animation: Animator) {
            }
        })
        transX.addUpdateListener {}
        transX.duration = animatorTime
        transX
    }

    private var danmuke: DanmuEntity? = null
    private var parentWidth: Int = 0

    fun setDamukeAniml(danmuke: DanmuEntity, parentWidth: Int) {
        this.danmuke = danmuke
        this.parentWidth = parentWidth
        translationX = parentWidth.toFloat()
        Glide.with(context)
            .load(danmuke.senderAvatar)
            .into(findViewById(R.id.ivSenderAvatar))
        findViewById<TextView>(R.id.tvSenderName).text = danmuke.senderName
        findViewById<TextView>(R.id.tvContent).text = (danmuke.content)
    }

    override fun clear() {
        nextAvalibeCall = null
        endCall = null
        tansAni.cancel()
    }


    override fun start() {
        post { tansAni.start() }
        postDelayed({
            nextAvalibeCall?.invoke()
        }, (animatorTime * 0.5).toLong())
    }

    override fun getView(): View {
        return this
    }
}


/**
 * 弹幕轨道
 */
class DanmuTrackView : FrameLayout, TrackView<DanmuEntity> {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, mAttributeSet: AttributeSet?) : super(context, mAttributeSet) {}

    private var mDanmuItemViews = ArrayList<IDanmuItemView>()
    override var finishedCall: (() -> Unit)? = {}

    override fun showInSameTrack(trackMode: DanmuEntity): Boolean {
        return false
    }

    private var nextAble = true;

    override fun onNewModel(mode: DanmuEntity) {
        val mDanmuItemView =
            LayoutInflater.from(context).inflate(R.layout.view_danmu, this, false) as DanmuItemView
        mDanmuItemViews.add(mDanmuItemView)
        addView(mDanmuItemView)
        nextAble = false
        mDanmuItemView.nextAvalibeCall = {
            nextAble = true
        }
        mDanmuItemView.endCall = {
            removeView(mDanmuItemView)
            mDanmuItemViews.remove(mDanmuItemView)
        }
        mDanmuItemView.setDamukeAniml(mode, getScreenSize())
        mDanmuItemView.start()
    }

    private fun getScreenSize(): Int {
        val displayMetrics: DisplayMetrics =
            Resources.getSystem().displayMetrics
        return intArrayOf(displayMetrics.widthPixels, displayMetrics.heightPixels)[0]
    }

    override fun isShow(): Boolean {
        return !nextAble
    }

    override fun clear(isRoomChange: Boolean) {
        mDanmuItemViews.forEach {
            it.clear()
            removeView(it.getView())
        }
        mDanmuItemViews.clear()
    }
}