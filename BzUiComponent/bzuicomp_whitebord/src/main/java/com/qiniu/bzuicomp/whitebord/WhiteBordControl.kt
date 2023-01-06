package com.qiniu.bzuicomp.whitebord

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.PopupMenu
import com.qiniu.bzuicomp.whitebord.databinding.OverhaulWhiteboardControlBinding
import com.qiniu.droid.whiteboard.QNWhiteBoard
import com.qiniu.droid.whiteboard.listener.QNAutoRemoveWhiteBoardListener
import com.qiniu.droid.whiteboard.model.InputConfig

class WhiteBordControl : FrameLayout {

    private val TAG = "WhiteBordControl"
    private lateinit var binding: OverhaulWhiteboardControlBinding

    /**
     * 创建普通笔颜色选择面板
     */
    private val normalPenWindow by lazy {
        PalettePopup(context).apply {
            addColorSelection(NormalPenStyle.colors, normalPenStyle.colorIndex) {
                normalPenStyle.colorIndex = it
            }
            addSizeSelection(NormalPenStyle.sizes, normalPenStyle.sizeIndex) {
                normalPenStyle.sizeIndex = it
            }
        }
    }

    /**
     * 创建马克笔颜色选择面板
     */
    private val markPenWindow by lazy {
        PalettePopup(context).apply {
            addColorSelection(MarkPenStyle.colors, markPenStyle.colorIndex) {
                markPenStyle.colorIndex = it
            }
            addSizeSelection(MarkPenStyle.sizes, markPenStyle.sizeIndex) {
                markPenStyle.sizeIndex = it
            }
        }
    }

    /**
     * 创建橡皮大小选择面板
     */
    private val eraserWindow by lazy {
        PalettePopup(context).apply {
            addSizeSelection(EraserStyle.sizes, eraserStyle.sizeIndex) {
                eraserStyle.sizeIndex = it
            }
        }
    }

    /**
     * 创建激光笔图形选择面板
     */
    private val laserWindow by lazy {
        PalettePopup(context).apply {
            addIconSelection(LaserStyle.icons.keys, laserStyle.iconKey) {
                laserStyle.iconKey = it
            }
        }
    }

    /**
     * 创建几何图形选择面板
     */
    private val geometryWindow by lazy {
        PalettePopup(context).apply {
            addIconSelection(GeometryStyle.icons.keys, geometryStyle.iconKey) {
                geometryStyle.iconKey = it
            }
            addColorSelection(GeometryStyle.colors, geometryStyle.colorIndex) {
                geometryStyle.colorIndex = it
            }
            addSizeSelection(GeometryStyle.sizes, geometryStyle.sizeIndex) {
                geometryStyle.sizeIndex = it
            }
        }
    }

    /**
     * 插入文件/图片的选项弹窗
     */
    private val insertFilePopupMenu by lazy {
        PopupMenu(context, binding.insertFile).apply {
            inflate(R.menu.insert_file_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
//                    R.id.camera -> openCamera()
//                    R.id.gallery -> openGallery.launch("image/*")
//                    R.id.file -> openFile.launch("*/*")
                }
                true
            }
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        binding = OverhaulWhiteboardControlBinding.inflate(LayoutInflater.from(context), this, true)

        binding.select.setOnClickListener {

        }
        binding.pen.setOnClickListener {
            if (currentInputType != InputType.NORMAL) {
                changeInputType(InputType.NORMAL)
            } else {
                normalPenWindow.show(it)
            }

        }
        binding.mark.setOnClickListener {
            if (currentInputType != InputType.MARK) {
                changeInputType(InputType.MARK)
            } else {
                markPenWindow.show(it)
            }

        }
        binding.eraser.setOnClickListener {
            if (currentInputType != InputType.ERASE) {
                changeInputType(InputType.ERASE)
            } else {
                eraserWindow.show(it)
            }

        }
        binding.laser.setOnClickListener {
            if (currentInputType != InputType.LASER) {
                changeInputType(InputType.LASER)
            } else {
                laserWindow.show(it)
            }
        }
        binding.geometry.setOnClickListener {

            if (currentInputType != InputType.GEOMETRY) {
                changeInputType(InputType.GEOMETRY)
            } else {
                geometryWindow.show(it)
            }
        }
        binding.insertFile.setOnClickListener {
            changeInputType(InputType.SELECT)

        }
        binding.restore.setOnClickListener {
            QNWhiteBoard.recover()
        }

        QNWhiteBoard.addListener(object :
            QNAutoRemoveWhiteBoardListener {
            override fun onRecoveryStateChanged(isEmpty: Boolean) {
                Log.d(TAG, "onRecoveryStateChanged")
                if (isRestoreNeed) {
                    binding.restore.visibility = if (!isEmpty) View.VISIBLE else View.GONE
                }
            }
        })
    }

    /**
     * 普通笔配置
     */
    val normalPenStyle = NormalPenStyle()

    /**
     * 马克笔配置
     */
    val markPenStyle = MarkPenStyle()

    /**
     * 橡皮配置
     */
    val eraserStyle = EraserStyle()

    /**
     * 激光笔配置
     */
    val laserStyle = LaserStyle()

    /**
     * 几何图形样式
     */
    val geometryStyle = GeometryStyle()

    /**
     * 当前输入模式
     */
    var currentInputType = (InputType.NORMAL)


    /**
     * 改变输入模式
     *
     * @param inputType 新的输入模式
     */
    fun changeInputType(inputType: InputType) {
        currentInputType = inputType
        val config = when (inputType) {
            InputType.NORMAL -> normalPenStyle.inputConfig
            InputType.MARK -> markPenStyle.inputConfig
            InputType.LASER -> laserStyle.inputConfig
            InputType.ERASE -> eraserStyle.inputConfig
            InputType.SELECT -> InputConfig.select()
            InputType.GEOMETRY -> geometryStyle.inputConfig
        }
        QNWhiteBoard.setInputMode(config)
        binding.select.isSelected = inputType == InputType.SELECT
        binding.pen.isSelected = inputType == InputType.NORMAL
        binding.mark.isSelected = inputType == InputType.MARK
        binding.eraser.isSelected = inputType == InputType.ERASE
        binding.laser.isSelected = inputType == InputType.LASER
        binding.geometry.isSelected = inputType == InputType.GEOMETRY
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    fun setIsSelectNeed(isNeed: Boolean) {
        binding.select.visibility = if (isNeed) View.VISIBLE else View.GONE
    }

    fun setIsPenNeed(isNeed: Boolean) {
        binding.pen.visibility = if (isNeed) View.VISIBLE else View.GONE
    }

    fun setIsMarkNeed(isNeed: Boolean) {
        binding.mark.visibility = if (isNeed) View.VISIBLE else View.GONE
    }

    fun setIsEraserNeed(isNeed: Boolean) {
        binding.eraser.visibility = if (isNeed) View.VISIBLE else View.GONE
    }

    fun setIsLaserNeed(isNeed: Boolean) {
        binding.laser.visibility = if (isNeed) View.VISIBLE else View.GONE
    }

    fun setIsGeometryNeed(isNeed: Boolean) {
        binding.geometry.visibility = if (isNeed) View.VISIBLE else View.GONE
    }

    fun setIsInsertFileNeed(isNeed: Boolean) {
        binding.insertFile.visibility = if (isNeed) View.VISIBLE else View.GONE
    }

    private var isRestoreNeed = true
    fun setIsRestoreNeed(isNeed: Boolean) {
        isRestoreNeed = isNeed
    }


    /**
     * 输入模式
     */
    enum class InputType {
        /**
         * 普通笔
         */
        NORMAL,

        /**
         * 马克笔
         */
        MARK,

        /**
         * 激光笔
         */
        LASER,

        /**
         * 橡皮
         */
        ERASE,

        /**
         * 选择
         */
        SELECT,

        /**
         * 几何图形
         */
        GEOMETRY,
    }

}