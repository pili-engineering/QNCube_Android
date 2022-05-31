package com.qucube.uikitinput

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.kit_mv_room_input.view.*

class DarkInputView : FrameLayout, IInputView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.kit_mv_room_input, this, false)
        addView(view)
        init()
    }

    /**
     * 发消息拦截回调
     */
    override var sendPubCall: ((msg: String) -> Unit)? = null

    // private val faceFragment by lazy { FaceFragment() }
    private var mSoftKeyBoardListener: SoftKeyBoardListener? =
        null //  by lazy { SoftKeyBoardListener(context as Activity) }

    private var inputType = 0

    private fun init() {
        //表情暂时没写
        chat_message_input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.toString()?.isEmpty() == true) {
                    send_btn.visibility = View.GONE
                } else {
                    send_btn.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        chat_message_input.setOnClickListener {
            hideFace()
        }

        face_btn.setOnClickListener {
            checkShowFace()
        }

        send_btn.setOnClickListener {
            val msgEdit = chat_message_input.text.toString()
            sendPubCall?.invoke(msgEdit)
            chat_message_input.setText("")
            hideFace()
            SoftInputUtil.hideSoftInputView(chat_message_input)
        }

        emojiBoard.setItemClickListener { code ->
            if (code == "/DEL") {
                chat_message_input.dispatchKeyEvent(
                    KeyEvent(
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DEL
                    )
                )
            } else {
                chat_message_input.getText()?.insert(chat_message_input.selectionStart, code)
            }
        }
    }

    override fun requestEditFocus() {
        chat_message_input.requestFocus()
        chat_message_input.post {
            SoftInputUtil.showSoftInputView(chat_message_input)
        }
    }

    private var mOrientationDetector: OrientationDetector? = null
    override fun attachActivity(activity: Activity) {
        if (mSoftKeyBoardListener == null) {
            mSoftKeyBoardListener = SoftKeyBoardListener(activity)
        }
        if (mOrientationDetector == null) {
            mOrientationDetector = OrientationDetector(activity) {
                SoftInputUtil.hideSoftInputView(activity)
                chat_message_input.clearFocus()
                val lp = tempView.layoutParams
                lp.height = 0
                tempView.layoutParams = lp
            }
        }
        mSoftKeyBoardListener?.setOnSoftKeyBoardChangeListener(object :
            SoftKeyBoardListener.OnSoftKeyBoardChangeListener {
            override fun keyBoardShow(height: Int) {
                hideFace()
                if (isInputAutoChangeHeight && chat_message_input.isFocused) {
                    val lp = tempView.layoutParams
                    lp.height = height
                    tempView.layoutParams = lp
                }
            }

            override fun keyBoardHide(height: Int) {
                val lp = tempView.layoutParams
                lp.height = 0
                tempView.layoutParams = lp
            }
        })
        mSoftKeyBoardListener?.attach()
    }

    override fun getView(): View {
        return this
    }

    private var isInputAutoChangeHeight = false
    override fun setInputAutoChangeHeight(inputAutoChangeHeight: Boolean) {
        isInputAutoChangeHeight = inputAutoChangeHeight
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mSoftKeyBoardListener?.detach()
        mOrientationDetector?.isEnable = false
        mOrientationDetector?.disable()
    }

    private fun checkShowFace() {
        if (inputType == 0) {
            showFace()
        } else {
            hideFace()
        }
    }

    private fun hideFace() {
        emojiBoard.visibility = View.GONE
        inputType = 0
        face_btn.isSelected = false
    }

    private fun showFace() {
        SoftInputUtil.hideSoftInputView(chat_message_input)
        emojiBoard.visibility = View.VISIBLE
        inputType = 1
        face_btn.isSelected = true
    }
}