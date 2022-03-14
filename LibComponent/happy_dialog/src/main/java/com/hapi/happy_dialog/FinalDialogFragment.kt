package com.hapi.happy_dialog

import android.os.Bundle
import android.view.*
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import java.lang.reflect.Field


abstract class FinalDialogFragment : DialogFragment() {

    var mDefaultListener: BaseDialogListener? = null
    private val INVALID_LAYOUT_ID = -1

    private var mCancelable: Boolean = true
    private var mGravityEnum: Int = Gravity.CENTER

    private var mdimAmount = -1.0f

    @StyleRes
    private var animationStyleresId: Int? = null

    fun setDefaultListener(defaultListener: BaseDialogListener): FinalDialogFragment {
        mDefaultListener = defaultListener
        return this
    }

    fun applyCancelable(cancelable: Boolean): FinalDialogFragment {
        mCancelable = cancelable
        return this
    }

    fun applyGravityStyle(gravity: Int): FinalDialogFragment {
        mGravityEnum = gravity
        return this
    }

    fun applyAnimationStyle(@StyleRes resId: Int): FinalDialogFragment {
        animationStyleresId = resId
        return this
    }

    fun applyDimAmount(dimAmount: Float): FinalDialogFragment {
        mdimAmount = dimAmount
        return this
    }

    /** The system calls this to get the DialogFragment's layout, regardless
     * of whether it's being displayed as a dialog or an embedded fragment.  */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootView: View?
        // Inflate the layout to use as dialog or embedded fragment
        if (getViewLayoutId() != INVALID_LAYOUT_ID) {
            rootView = inflater.inflate(getViewLayoutId(), container, false)
        } else {
            rootView = super.onCreateView(inflater, container, savedInstanceState)
        }
        return rootView
    }

    override fun onStart() {
        super.onStart()
        //STYLE_NO_FRAME设置之后会调至无法自动点击外部自动消失，因此添加手动控制
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.window?.applyGravityStyle(mGravityEnum, animationStyleresId)
        if (mdimAmount >= 0) {
            val window = dialog!!.window
            val windowParams: WindowManager.LayoutParams = window!!.attributes
            windowParams.dimAmount = mdimAmount
            window.attributes = windowParams
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //取消系统对dialog样式上的干扰，防止dialog宽度无法全屏
        setStyle(androidx.fragment.app.DialogFragment.STYLE_NO_FRAME, R.style.dialogFullScreen)
    }

    override fun onResume() {
        super.onResume()
        dialog?.setCanceledOnTouchOutside(mCancelable)
        if (!mCancelable) {
            dialog?.setOnKeyListener { v, keyCode, event -> keyCode == KeyEvent.KEYCODE_BACK }
        }
    }

    override fun show(
        manager: FragmentManager,
        tag: String?
    ) {
        try {
            val mDismissed: Field = this.javaClass.superclass.getDeclaredField("mDismissed")
            val mShownByMe: Field = this.javaClass.superclass.getDeclaredField("mShownByMe")
            mDismissed.setAccessible(true)
            mShownByMe.setAccessible(true)
            mDismissed.setBoolean(this, false)
            mShownByMe.setBoolean(this, true)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        val ft: FragmentTransaction = manager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()
    }

    abstract fun getViewLayoutId(): Int


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        init()
    }

    abstract fun init()

    override fun dismiss() {
        super.dismiss()
        mDefaultListener?.onDismiss(this)
    }

    open class BaseDialogListener {
        /**
         * 点击确定，并携带指定类型参数
         */
        open fun onDialogPositiveClick(
            dialog: androidx.fragment.app.DialogFragment,
            any: Any = Any()
        ) {
        }

        open fun onDialogNegativeClick(
            dialog: androidx.fragment.app.DialogFragment,
            any: Any = Any()
        ) {
        }

        open fun onDismiss(dialog: androidx.fragment.app.DialogFragment) {}
    }
}