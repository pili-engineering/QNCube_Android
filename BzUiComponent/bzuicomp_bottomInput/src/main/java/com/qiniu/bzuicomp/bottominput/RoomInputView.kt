package com.qiniu.bzuicomp.bottominput

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.hapi.ut.SoftInputUtil
import com.qiniu.bzuicomp.bottominput.databinding.ViewRoomInputBinding

class RoomInputView : FrameLayout, IInputView {

    private lateinit var binding: ViewRoomInputBinding

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        binding = ViewRoomInputBinding.inflate(LayoutInflater.from(context), this, true)
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
        binding.  chatMessageInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.toString()?.isEmpty() == true) {
                    binding.   sendBtn.visibility = View.GONE
                } else {
                    binding.   sendBtn.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        binding.   chatMessageInput.setOnClickListener {
            hideFace()
        }

        binding.  faceBtn.setOnClickListener {
            checkShowFace()
        }

        binding.  sendBtn.setOnClickListener {
            val msgEdit =   binding.  chatMessageInput.text.toString()
            sendPubCall?.invoke(msgEdit)
            binding.   chatMessageInput.setText("")
            hideFace()
            SoftInputUtil.hideSoftInputView(   binding. chatMessageInput)
        }

        binding.  emojiBoard.setItemClickListener { code ->
            if (code == "/DEL") {
                binding.   chatMessageInput.dispatchKeyEvent(
                    KeyEvent(
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DEL
                    )
                )
            } else {
                binding.   chatMessageInput.getText()?.insert(   binding. chatMessageInput.selectionStart, code)
            }
        }
    }

    override fun requestEditFocus() {
        binding.   chatMessageInput.requestFocus()
        binding.  chatMessageInput.post {
            SoftInputUtil.showSoftInputView(   binding. chatMessageInput)
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
                binding.     chatMessageInput.clearFocus()
                val lp = binding.tempView.layoutParams
                lp.height = 0
                binding.   tempView.layoutParams = lp
            }
        }
        mSoftKeyBoardListener?.setOnSoftKeyBoardChangeListener(object :
            SoftKeyBoardListener.OnSoftKeyBoardChangeListener {
            override fun keyBoardShow(height: Int) {
                hideFace()
                if (isInputAutoChangeHeight &&    binding. chatMessageInput.isFocused) {
                    val lp =    binding. tempView.layoutParams
                    lp.height = height
                    binding.   tempView.layoutParams = lp
                }
            }

            override fun keyBoardHide(height: Int) {
                val lp =    binding. tempView.layoutParams
                lp.height = 0
                binding.  tempView.layoutParams = lp
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
        binding.  emojiBoard.visibility = View.GONE
        inputType = 0
        binding.  faceBtn.isSelected = false
    }

    private fun showFace() {
        SoftInputUtil.hideSoftInputView(   binding. chatMessageInput)
        binding. emojiBoard.visibility = View.VISIBLE
        inputType = 1
        binding.  faceBtn.isSelected = true
    }
}