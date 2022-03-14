// Created by qiniu on 2021/4/26.

package com.qiniu.bzuicomp.whitebord


import android.graphics.Color
import androidx.core.graphics.ColorUtils

import com.qiniu.droid.whiteboard.QNWhiteBoard
import com.qiniu.droid.whiteboard.model.InputConfig
import com.qiniu.droid.whiteboard.type.GeometryType
import com.qiniu.droid.whiteboard.type.LaserType
import kotlin.math.pow

/**
 * 普通笔配置
 */
class NormalPenStyle  {

    /**
     * 当前使用的颜色序号
     */

    var colorIndex = 0
        set(value) {
            field = value
            QNWhiteBoard.setInputMode(inputConfig)
        }

    /**
     * 当前使用的粗细序号
     */
    var sizeIndex = 0
        set(value) {
            field = value
            QNWhiteBoard.setInputMode(inputConfig)
        }

    /**
     * 生成当前配置
     */
    val inputConfig get() = InputConfig.pen(colors[colorIndex], sizes[sizeIndex])

     fun notifyPropertyChanged(fieldId: Int) {
        QNWhiteBoard.setInputMode(inputConfig)
    }

    companion object {
        /**
         * 可选颜色
         */
        val colors = intArrayOf(
            0xFF000000.toInt(),
            0xFFFFA726.toInt(),
            0xFFFFFF00.toInt(),
            0xFF388E3C.toInt(),
            0xFF2962FF.toInt(),
            0xFFD500F9.toInt(),
            0xFFFF0000.toInt(),
            0xFFFFFFFF.toInt(),
            0xFF8D6E63.toInt(),
        )

        /**
         * 可选粗细
         */
        val sizes = floatArrayOf(
            0.5f,
            1f,
            3f,
            5f,
            8f,
        )
    }
}

/**
 * 马克笔配置
 */
class MarkPenStyle  {

    /**
     * 当前使用的颜色序号
     */

    var colorIndex = 0
        set(value) {
            field = value
            QNWhiteBoard.setInputMode(inputConfig)
        }

    /**
     * 当前使用的粗细序号
     */
    var sizeIndex = 0
        set(value) {
            field = value
            QNWhiteBoard.setInputMode(inputConfig)
        }

    /**
     * 生成当前配置
     */
    val inputConfig get() = InputConfig.pen(colors[colorIndex] and 0x6FFFFFFF, sizes[sizeIndex])

    companion object {
        /**
         * 可选颜色
         */
        val colors = intArrayOf(
            0xFF000000.toInt(),
            0xFFFFA726.toInt(),
            0xFFFFFF00.toInt(),
            0xFF388E3C.toInt(),
            0xFF2962FF.toInt(),
            0xFFD500F9.toInt(),
            0xFFFF0000.toInt(),
            0xFFFFFFFF.toInt(),
            0xFF8D6E63.toInt(),
        )

        /**
         * 可选粗细
         */
        val sizes = floatArrayOf(
            0.5f,
            1f,
            3f,
            5f,
            8f,
        )
    }
}

/**
 * 橡皮配置
 */
class EraserStyle {

    /**
     * 当前使用的面积序号
     */
    var sizeIndex = 0
        set(value) {
            field = value
            QNWhiteBoard.setInputMode(inputConfig)
        }

    /**
     * 生成当前配置
     */
    val inputConfig get() = InputConfig.erase(sizes[sizeIndex])

    companion object {

        /**
         * 可选面积（在白板虚拟尺寸中的大小）
         */
        val sizes = floatArrayOf(
            60f,
            100f,
            160f,
            240f,
        )
    }
}

/**
 * 激光笔样式
 */
class LaserStyle {
    /**
     * 当前使用的图形res（作为key）
     */
    var iconKey = R.drawable.ic_laser_point
        set(value) {
            field = value
            QNWhiteBoard.setInputMode(inputConfig)
        }

    /**
     * 生成当前配置
     */
    val inputConfig get() = InputConfig.laserPen(icons[iconKey]!!)

    companion object {

        /**
         * 可选图形
         */
        val icons = mapOf(
            R.drawable.ic_laser_point to LaserType.LASER_DOT,
            R.drawable.ic_laser_hand to LaserType.LASER_HAND,
            R.drawable.ic_laser_arrow_outline to LaserType.LASER_ARROWS_WHITE,
            R.drawable.ic_laser_arrow_filled to LaserType.LASER_ARROWS_BLACK,
        )
    }
}

/**
 * 几何图形样式
 */
class GeometryStyle {


    /**
     * 当前使用的图形res（作为key）
     */
    var iconKey = R.drawable.ic_geometry_rect
        set(value) {
            field = value
            QNWhiteBoard.setInputMode(inputConfig)
        }

    /**
     * 当前使用的颜色序号
     */
    var colorIndex = 0
        set(value) {
            field = value
            QNWhiteBoard.setInputMode(inputConfig)
        }

    /**
     * 当前使用的粗细序号
     */
    var sizeIndex = 0
        set(value) {
            field = value
            QNWhiteBoard.setInputMode(inputConfig)
        }

    /**
     * 生成当前配置
     */
    val inputConfig
        get() = InputConfig.geometry(
            icons[iconKey]!!,
            colors[colorIndex],
            sizes[sizeIndex]
        )

    companion object {

        /**
         * 可选图形
         */
        val icons = mapOf(
            R.drawable.ic_geometry_rect to GeometryType.RECTANGLE,
            R.drawable.ic_geometry_circle to GeometryType.CIRCLE,
            R.drawable.ic_geometry_arrow to GeometryType.ARROW,
            R.drawable.ic_geometry_line to GeometryType.LINE,
        )

        /**
         * 可选颜色
         */
        val colors = intArrayOf(
            0xFF000000.toInt(),
            0xFFFFFF00.toInt(),
            0xFF388E3C.toInt(),
            0xFFD500F9.toInt(),
            0xFFFF0000.toInt(),
        )

        /**
         * 可选粗细
         */
        val sizes = floatArrayOf(
            1f,
            3f,
            5f,
            8f,
        )
    }
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

/**
 * 白板背景主题
 *
 * @property color 当前使用的背景色
 */
class BoardTheme(val color: Int) {

    /**
     * 当前主题类型
     */
    val themeType by lazy {
        if (Color.alpha(color) <= 16) {
            return@lazy BoardThemeType.TRANSLUCENT
        }

        val hsl = FloatArray(3)

        ColorUtils.colorToHSL(color, hsl)

        if (hsl[2] in 0.0..0.18) {
            return@lazy BoardThemeType.BLACK
        }

        if (hsl[1] in 0.0..0.17) {
            return@lazy BoardThemeType.WHITE
        }

        if (hsl[0] in 70.0..180.0) {
            return@lazy BoardThemeType.GREEN
        }

        BoardThemeType.OTHER
    }

    /**
     * 是否是亮色主题
     */
    val isLight by lazy {
        (ColorUtils.calculateLuminance(color) + 0.05).pow(2) > 0.15
    }

    /**
     * 是否是暗色主题
     */
    val isDark get() = !isLight

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BoardTheme

        if (color != other.color) return false

        return true
    }

    override fun hashCode(): Int {
        return color
    }

    companion object {

        /**
         * 通过主题类型创建主题实例
         *
         * @param type 主题类型
         */
        fun fromType(type: BoardThemeType) = BoardTheme(type.color())

        /**
         * 白色主题
         */
        fun white() = fromType(BoardThemeType.WHITE)

        /**
         * 黑色主题
         */
        fun black() = fromType(BoardThemeType.BLACK)

        /**
         * 绿色主题
         */
        fun green() = fromType(BoardThemeType.GREEN)

        /**
         * 透明色主题
         */
        fun translucent() = fromType(BoardThemeType.TRANSLUCENT)
    }
}

/**
 * 几种预置主题类型
 */
enum class BoardThemeType {

    /**
     * 白色
     */
    WHITE,

    /**
     * 黑色
     */
    BLACK,

    /**
     * 绿色
     */
    GREEN,

    /**
     * 透明色
     */
    TRANSLUCENT,

    /**
     * 其它颜色
     */
    OTHER;

    /**
     * 获取类型对应的颜色
     */
    fun color() = when (this) {
        WHITE -> 0xFFF5F5F5.toInt()
        BLACK -> 0xFF212121.toInt()
        GREEN -> 0xFF1F795E.toInt()
        TRANSLUCENT -> 0x00000000
        OTHER -> 0xFFF5F5F5.toInt()
    }
}