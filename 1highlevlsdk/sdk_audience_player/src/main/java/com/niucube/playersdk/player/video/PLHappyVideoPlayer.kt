package com.niucube.playersdk.player.video

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.niucube.playersdk.player.*
import com.niucube.playersdk.player.PlayerStatus.*
import com.niucube.playersdk.player.engine.EngineType
import com.niucube.playersdk.player.engine.HappyEngineFactor
import com.niucube.playersdk.player.utils.LogUtil
import com.niucube.playersdk.player.utils.PalyerUtil
import com.niucube.playersdk.player.video.contronller.IController
import com.niucube.playersdk.player.video.floating.Floating
import com.niucube.playersdk.player.video.floating.TinyFloatView
import com.qiniu.comp.playersdk.R


open class PLHappyVideoPlayer : FrameLayout, IVideoPlayer, TextureView.SurfaceTextureListener {


    /**
     * wrap content 高度下的 宽高比
     */
    companion object {
        private const val DEFAULT_HEIGHT_RATIO = (36F / 64)
    }

    /*
     wrap content 高度下的 宽高比
     */
    private var defaultHeightRatio = 0f

    /**
     * 获取到视频宽高后的　宽高比
     */
    private var videoHeightRatio = 0f

    private var tagNam = ""

    private lateinit var mContainer: TinyFloatView
    private lateinit var mFloating: Floating
    private lateinit var mTextureView: HappyTextureView
    private var mSurface: Surface? = null


    private var isFromLastPosition = false
    private var loop = false
    protected val mPlayerEngine: AbsPlayerEngine by lazy {
        val player = HappyEngineFactor.newPlayer(context!!.applicationContext, engineType)
        player.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener)
        player.addPlayStatusListener(mediaPlayerListener, true)
        val config = player.getPlayerConfig()
        config.setFromLastPosition(isFromLastPosition)
            .setLoop(loop)
        player.setPlayerConfig(config)
        player
    }

    protected lateinit var coverImg: CoverImgView
    private var mSurfaceTexture: SurfaceTexture? = null
    private var isFirstFrameAsCover = false

    protected var mController: IController? = null

    /**
     * 当视频宽和高和控件宽和高比例　有一点误差　当作centerCrop（例如某些竖屏播放器　播放的视频都是全屏没有点点黑边）　处理　否则适应处理
    　　*
     */
    private var centerCropError = 0f

    /**
     * 自动旋转
     */
    private var autoChangeOrientation = false

    private var mOrientationDetector: OrientationDetector? = null

    /**
     * 播放引擎
     */
    protected var engineType = EngineType.MEDIA_PLAYER

    //全屏時候是否添加到activity根节点
    private var whenFullAddToActivityTop = true

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        attrs?.let {
            val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.happyVideo)
            typedArray?.apply {
                val type = getInt(R.styleable.happyVideo_engine, EngineType.MEDIA_PLAYER.type)
                when (type) {
                    EngineType.MEDIA_PLAYER.type -> {
                        engineType = EngineType.MEDIA_PLAYER
                    }

                    EngineType.QN_PLAYER.type -> {
                        engineType = EngineType.QN_PLAYER
                    }
                }
                whenFullAddToActivityTop =
                    getBoolean(R.styleable.happyVideo_whenFullAddToActivityTop, true)
                defaultHeightRatio = getFloat(R.styleable.happyVideo_heightRatio, 0f)
                isFromLastPosition =
                    getBoolean(R.styleable.happyVideo_isFromLastPosition, false)
                loop = getBoolean(R.styleable.happyVideo_loop, false)
                autoChangeOrientation =
                    getBoolean(R.styleable.happyVideo_autoChangeOrientation, false)
                isFirstFrameAsCover = getBoolean(R.styleable.happyVideo_isFirstFrameAsCover, false)
                centerCropError = getFloat(R.styleable.happyVideo_centerCropError, 0f)
            }
            typedArray?.recycle()
        }
        init()
    }

    private var mCurrentMode = MODE_NORMAL

    fun setTagName(tagName: String) {
        tagNam = tagName
        mPlayerEngine.tagNam = tagName
    }

    private lateinit var textureContainer: TextureContainer
    private fun init() {
        mContainer = TinyFloatView(context)
        (mContainer as TinyFloatView).parentWindType = { mCurrentMode }

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        this.addView(mContainer, params)
        mContainer.setBackgroundColor(Color.parseColor("#000000"))
        textureContainer =
            LayoutInflater.from(context)
                .inflate(R.layout.layout_texture_container, mContainer, false) as TextureContainer
        mContainer.addView(textureContainer)
        mFloating = Floating(PalyerUtil.scanForActivity(context))
        mTextureView = textureContainer.findViewById(R.id.mTextureView)
        mTextureView.surfaceTextureListener = this
        coverImg = textureContainer.findViewById(R.id.coverImg)

        if (autoChangeOrientation) {
            mOrientationDetector = OrientationDetector(PalyerUtil.scanForActivity(context)) {

                if (it == 0 || it == 180) {
                    if (isFullScreen()) {
                        beExitFullScreen(false)
                    }
                }

                if (it == 90 || it == 270) {
                    if (isNormal()) {
                        beEnterFullScreen(false)
                    }
                }
            }
            mOrientationDetector?.isEnable = true
        }
        mTextureView.parentWindType = { mCurrentMode }
        mTextureView.tagNam = tagNam + "textureContainer"
        mTextureView.setCenterCropError(centerCropError)

        coverImg.parentWindType = { mCurrentMode }
        coverImg.tagNam = tagNam + "textureContainer"
        coverImg.setCenterCropError(centerCropError)
    }

    private var mediaPlayerListener = object : PlayerStatusListener {
        val animator: ObjectAnimator by lazy { ObjectAnimator.ofFloat(coverImg, "alpha", 1f, 0f).apply {
            if(engineType==EngineType.QN_PLAYER){
                duration = 1500
            }else{
                duration = 500
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {
                    coverImg?.visibility = View.GONE
                    coverImg?.alpha=1f
                }
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
            })
        } }
        override fun onPlayStateChanged(status: Int) {
            when (status) {
                STATE_PREPARING -> {
                    mContainer.keepScreenOn = true
                }
                STATE_COMPLETED -> {
                    mContainer.keepScreenOn = false
                }

                STATE_ERROR -> {
                    mContainer.keepScreenOn = false
                }

            }
            if (status == STATE_PLAYING) {
                Log.d("animator.isRunn"," animator.isRunning ()")
                if (coverImg.visibility == VISIBLE && !animator.isRunning) {
                    Log.d("animator.isRunn"," animator.start()")
                    animator.start()
                }
            }
        }

        override fun onPlayModeChanged(model: Int) {
        }
    }

    override fun addController(controller: IController) {
        mController = controller
        controller.attach(this)
        mContainer.addView(controller.getView())
        addPlayStatusListener(controller, true)

    }

    override fun lockScreen(toLock: Boolean): Boolean {
        if (isTinyWindow()) {
            return false
        }

        return mOrientationDetector?.lockScreen(toLock) ?: false
    }

    override fun isLock(): Boolean {
        return mOrientationDetector?.mIsLock ?: false
    }

    override fun setCover(uri: Uri?) {
        coverImg.setImageDrawable(null)
        coverImg.visibility = View.VISIBLE
        Glide.with(context)
            .asBitmap()
            .load(uri)
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    LogUtil.d(tagNam + "coverImg h " + resource.width + "     " + resource.height)
                    if (resource.width > 0 && resource.height > 0) {
                        val tempVideoHeightRatio = resource.height / resource.width.toFloat()
                        if (tempVideoHeightRatio != videoHeightRatio) {
                            videoHeightRatio = tempVideoHeightRatio

                            coverImg.adaptVideoSize(resource.width, resource.height)
                            mTextureView.adaptVideoSize(resource.width, resource.height)
                        }
                    }
                    coverImg.setImageBitmap(resource)
                }
            })
    }

    override fun setUp(uir: Uri, headers: Map<String, String>?, cover: Uri, preLoading: Boolean) {
        coverImg.isFirstFragme = false
        setCover(cover)
        onSetUp(uir, headers, preLoading)
    }

    private fun onSetUp(uir: Uri, headers: Map<String, String>?, preLoading: Boolean) {
        videoHeightRatio = 0f
        mController?.reset()
        mPlayerEngine.setUp(uir, headers, preLoading)
       // PalyerUtil.logVideoInfo(uir.toString())
        LogUtil.d(tagNam + "onSetUp ok thread" + Thread.currentThread().id)

    }

    override fun setUp(uir: Uri, headers: Map<String, String>?, preLoading: Boolean) {
        if (isFirstFrameAsCover) {
            coverImg.isFirstFragme = true
            setCover(uir)
        }
        onSetUp(uir, headers, preLoading)
    }

    override fun startPlay() {
        mOrientationDetector?.isEnable = true
        mPlayerEngine.startPlay()
    }

    override fun setPlayerConfig(playerConfig: PlayerConfig) {
        mPlayerEngine.setPlayerConfig(playerConfig)
    }

    override fun getPlayerConfig(): PlayerConfig {
        return mPlayerEngine.getPlayerConfig()
    }

    override fun addPlayStatusListener(lister: PlayerStatusListener, add: Boolean) {
        mPlayerEngine.addPlayStatusListener(lister, add)
    }

    override fun getPlayerWindowStatus(): Int {
        return mCurrentMode
    }

    override fun enterFullScreen() {
        Log.d("requestedOrientation", "enterFullScreen")
        beEnterFullScreen(true)
    }

    private fun beEnterFullScreen(changeOrientation: Boolean) {
        Log.d("requestedOrientation", "beEnterFullScreen")
        if (mCurrentMode == MODE_FULL_SCREEN) {
            return
        }

        if (isTinyWindow()) {
            exitTinyWindow()
        }

        // 隐藏ActionBar、状态栏，并横屏
        PalyerUtil.hideActionBar(context)
        if (changeOrientation) {
            PalyerUtil.scanForActivity(context).requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }

        if (whenFullAddToActivityTop) {
            val contentView = PalyerUtil.scanForActivity(context)
                .findViewById(android.R.id.content) as ViewGroup
            if (mCurrentMode == MODE_TINY_WINDOW) {
                contentView.removeView(mContainer)
            } else {
                this.removeView(mContainer)
            }
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            contentView.addView(mContainer, params)
        }

        mCurrentMode = MODE_FULL_SCREEN
        mPlayerEngine.mPlayerStatusListener.onPlayModeChanged(mCurrentMode)
        LogUtil.d(tagNam + "MODE_FULL_SCREEN")
    }

    override fun exitFullScreen(): Boolean {
        Log.d("requestedOrientation", "exitFullScreen")
        return beExitFullScreen(true)
    }

    private fun beExitFullScreen(changeOrientation: Boolean): Boolean {
        Log.d("requestedOrientation", "beExitFullScreen")
        if (mCurrentMode == MODE_FULL_SCREEN) {
            PalyerUtil.showActionBar(context)
            //   if (changeOrientation) {
            PalyerUtil.scanForActivity(context).requestedOrientation = SCREEN_ORIENTATION_NOSENSOR
            if (changeOrientation) {
                Thread.sleep(300)//某些手机直接切换切不过来 暂时方案 切换屏幕方向时候暂停不影响体验
            }
            PalyerUtil.scanForActivity(context).requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            // }
            if (whenFullAddToActivityTop) {
                val contentView = PalyerUtil.scanForActivity(context)
                    .findViewById(android.R.id.content) as ViewGroup
                contentView.removeView(mContainer)
                val params = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                this.addView(mContainer, params)
            }
            mCurrentMode = MODE_NORMAL
            mPlayerEngine.mPlayerStatusListener.onPlayModeChanged(mCurrentMode)
            LogUtil.d(tagNam + "MODE_NORMAL")
            return true
        }
        return false
    }

    override fun enterTinyWindow() {
        if (mCurrentMode != MODE_NORMAL) {
            return
        }
        enterTinyWindowSmooth(true)
    }

    private fun enterTinyWindowSmooth(flag: Boolean) {

        if (mCurrentMode == MODE_TINY_WINDOW) {
            return
        }
        mOrientationDetector?.isEnable = false
        val ratio = if (videoHeightRatio == 0f) {
            height / width.toFloat()
        } else {
            videoHeightRatio
        }
        if (flag) {

            mFloating.start(ratio, mContainer, object : Floating.OnFloatingDelegate {
                override fun onAninmationEnd() {
                    mCurrentMode = MODE_TINY_WINDOW
                    mPlayerEngine.mPlayerStatusListener.onPlayModeChanged(mCurrentMode)
                    LogUtil.d(tagNam + "MODE_TINY_WINDOW")
                }

                override fun onAnchorRectReach() {
                    this@PLHappyVideoPlayer.removeView(mContainer)
                }
            })
        } else {
            this.removeView(mContainer)
            val contentView = PalyerUtil.scanForActivity(context)
                .findViewById(Window.ID_ANDROID_CONTENT) as ViewGroup

            // 小窗口的宽度为屏幕宽度的60%，长宽比默认为16:9，右边距、下边距为8dp。
            val w = (PalyerUtil.getScreenWidth(context) * 0.6f).toInt()
            val params = FrameLayout.LayoutParams(
                w,
                (w * ratio).toInt()
            )
            params.gravity = Gravity.BOTTOM or Gravity.END
            params.rightMargin = PalyerUtil.dp2px(context, 8f)
            params.bottomMargin = PalyerUtil.dp2px(context, 8f)

            contentView.addView(mContainer, params)

            mCurrentMode = MODE_TINY_WINDOW
            mPlayerEngine.mPlayerStatusListener.onPlayModeChanged(mCurrentMode)
            LogUtil.d(tagNam + "MODE_TINY_WINDOW")
        }
    }

    override fun exitTinyWindow(): Boolean {
        if (mCurrentMode == MODE_TINY_WINDOW) {
            mOrientationDetector?.isEnable = true
            mFloating.clear()
            val contentView = PalyerUtil.scanForActivity(context)
                .findViewById(android.R.id.content) as ViewGroup
            contentView.removeView(mContainer)
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            this.addView(mContainer, params)
            mContainer.resetTranslation()
            mCurrentMode = MODE_NORMAL
            mPlayerEngine.mPlayerStatusListener.onPlayModeChanged(mCurrentMode)
            LogUtil.d(tagNam + "MODE_NORMAL")
            return true
        }
        return false
    }

    override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {
    }

    override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
    }

    override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
        return mSurfaceTexture == null
    }

    private val mOnVideoSizeChangedListener = object : AbsPlayerEngine.OnVideoSizeChangedListener {
        override fun onRotationInfo(rotation: Float) {
            mTextureView.setRotation(rotation)
            coverImg.setRotation(rotation)
        }

        override fun onVideoSizeChanged(mp: AbsPlayerEngine, width: Int, height: Int) {
            if (width > 0 && height > 0) {

                val tempVideoHeightRatio = height.toFloat() / width
                videoHeightRatio = tempVideoHeightRatio
                mTextureView.adaptVideoSize(width, height)
                coverImg.adaptVideoSize(width, height)
            }
            LogUtil.d(tagNam + "onVideoSizeChanged ——> width：$width， height：$height")
        }
    }

    override fun stop() {
        mPlayerEngine.stop()
    }

    override fun pause() {
        mPlayerEngine.pause()
    }

    override fun resume() {
        mPlayerEngine.resume()
    }

    override fun seekTo(pos: Int) {
        mPlayerEngine.seekTo(pos)
    }

    override fun setVolume(volume: Int) {
        mPlayerEngine.setVolume(volume)
    }

    override fun getCurrentPlayStatus(): Int {
        return mPlayerEngine.getCurrentPlayStatus()
    }

    override fun getMaxVolume(): Int {
        return mPlayerEngine.getMaxVolume()
    }

    override fun getVolume(): Int {
        return mPlayerEngine.getVolume()
    }

    override fun getDuration(): Long {
        return mPlayerEngine.getDuration()
    }

    override fun getCurrentPosition(): Long {
        return mPlayerEngine.getCurrentPosition()
    }

    override fun releasePlayer() {
        mOrientationDetector?.disable()
        mPlayerEngine.releasePlayer()
        mSurfaceTexture?.release()
        mController?.detach()
    }


    override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
        mSurfaceTexture = p0
        mSurface = Surface(mSurfaceTexture)
        mPlayerEngine.setSurface(mSurface!!)
    }

    override fun getBufferPercentage(): Int {
        return mPlayerEngine.getBufferPercentage()
    }


    override fun isFullScreen(): Boolean {
        return mCurrentMode == MODE_FULL_SCREEN
    }

    override fun isTinyWindow(): Boolean {
        return mCurrentMode == MODE_TINY_WINDOW
    }

    override fun isNormal(): Boolean {
        return mCurrentMode == MODE_NORMAL
    }

    override fun isIdle(): Boolean {

        return mPlayerEngine.isIdle()
    }

    override fun isPreparing(): Boolean {
        return mPlayerEngine.isPreparing()
    }

    override fun isPrepared(): Boolean {
        return mPlayerEngine.isPrepared()
    }

    override fun isBufferingPlaying(): Boolean {
        return mPlayerEngine.isBufferingPlaying()
    }

    override fun isBufferingPaused(): Boolean {
        return mPlayerEngine.isBufferingPaused()
    }

    override fun isPlaying(): Boolean {
        return mPlayerEngine.isPlaying()
    }

    override fun isPaused(): Boolean {
        return mPlayerEngine.isPaused()
    }

    override fun isError(): Boolean {
        return mPlayerEngine.isError()
    }

    override fun isPreLoading(): Boolean {
        return mPlayerEngine.isPreLoading()
    }

    override fun isPreLoaded(): Boolean {
        return mPlayerEngine.isPreLoaded()
    }

    override fun isCompleted(): Boolean {
        return mPlayerEngine.isCompleted()
    }

    override fun getCurrentUrl(): Uri? {
        return mPlayerEngine.getCurrentUrl()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 获取宽-测量规则的模式和大小
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)

        // 获取高-测量规则的模式和大小
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        if (heightMode == MeasureSpec.AT_MOST && isNormal()) {
            if (defaultHeightRatio != 0f) {
                val sizeH = widthSize * defaultHeightRatio
                super.onMeasure(
                    widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(sizeH.toInt(), MeasureSpec.EXACTLY)
                )
                return
            }

            if (videoHeightRatio != 0f) {
                val sizeH = widthSize * videoHeightRatio
                super.onMeasure(
                    widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(
                        Math.min(heightSize, sizeH.toInt()),
                        MeasureSpec.EXACTLY
                    )
                )
                return
            } else {
                val sizeH = widthSize * DEFAULT_HEIGHT_RATIO

                super.onMeasure(
                    widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(
                        Math.min(heightSize, sizeH.toInt()),
                        MeasureSpec.EXACTLY
                    )
                )
                //setMeasuredDimension(widthSize, sizeH.toInt())
                return
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}