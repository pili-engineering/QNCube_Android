// Created by qiniu on 2021/4/26.

package com.qiniu.bzuicomp.whitebord

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.widget.ViewUtils
import androidx.core.view.isNotEmpty
import androidx.core.view.setMargins
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import splitties.dimensions.dip

/**
 * 通用调色盘弹窗，选择颜色和大小
 */
class PalettePopup(private val context: Context) {

    /**
     * 根布局
     */
    private val parent: ViewGroup

    /**
     * 弹窗
     */
    val popupWindow: PopupWindow

    init {
        val binding =LayoutInflater.from(context).inflate(R.layout.popup_card,null)
        parent = binding as ViewGroup
        popupWindow = PopupWindow(
            binding,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isTouchable = true
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable())
        }
    }

    /**
     * 增加颜色选择面板
     *
     * @param colors 待选颜色
     * @param selected 初始选中的序号
     * @param changedListener 颜色选择事件参数为选中序号
     */
    fun addColorSelection(
        colors: IntArray,
        selected: Int,
        changedListener: (selected: Int) -> Unit
    ) {
        loadChipGroup().apply {
            colors.forEachIndexed { index, i ->
                loadChip(this).apply {
                    chipIconTint = ColorStateList.valueOf(i)
                    id = index
                }
            }
            check(selected)
            setOnCheckedChangeListener { _, checkedId ->
                changedListener(checkedId)
            }
        }
    }

    /**
     * 增加大小选择面板
     *
     * @param sizes 待选大小
     * @param selected 初始选中的序号
     * @param changedListener 大小选择事件参数为选中序号
     */
    fun addSizeSelection(
        sizes: FloatArray,
        selected: Int,
        changedListener: (selected: Int) -> Unit
    ) {
        loadChipGroup().apply {
            val minSize = context.dip(16)
            val weight = context.dip(16).toFloat() / sizes.size

            sizes.forEachIndexed { index, _ ->
                loadChip(this).apply {
                    chipIconSize = weight * index + minSize
                    id = index
                }
            }
            check(selected)
            setOnCheckedChangeListener { _, checkedId ->
                changedListener(checkedId)
            }
        }
    }

    /**
     * 增加图形选择面板
     *
     * @param icons 待选图形
     * @param selected 初始选中的图形资源id
     * @param changedListener 图形选择事件参数为选中的图形资源id
     */
    fun addIconSelection(icons: Set<Int>, selected: Int, changedListener: (selected: Int) -> Unit) {
        loadChipGroup().apply {
            icons.forEach {
                loadChip(this).apply {
                    setChipIconResource(it)
                    id = it
                }
            }
            check(selected)
            setOnCheckedChangeListener { _, checkedId ->
                changedListener(checkedId)
            }
        }
    }

    /**
     * 显示弹窗
     *
     * @param anchor 锚点
     */
    fun show(anchor: View) {
        popupWindow.showAsDropDown(anchor, 0, context.dip(8))
    }

    /**
     * 加载一个[ChipGroup]
     */
    private fun loadChipGroup(): ChipGroup {
        if (parent.isNotEmpty()) {
            parent.addView(
                View(context).apply {
                    setBackgroundColor(Color.DKGRAY)
                },
                ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, context.dip(1))
                    .apply {
                        setMargins(context.dip(8))
                    })
        }

        return LayoutInflater.from(context).inflate(R.layout.palette_chip_group, parent, false)
            .let {
                parent.addView(it)
                it as ChipGroup
            }
    }

    /**
     * 加载一个[Chip]
     */
    private fun loadChip(root: ViewGroup): Chip {
        return LayoutInflater.from(context).inflate(R.layout.palette_chip, root, false).let {
            root.addView(it)
            it as Chip
        }
    }


}