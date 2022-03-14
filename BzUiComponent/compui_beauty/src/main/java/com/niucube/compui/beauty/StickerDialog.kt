package com.niucube.compui.beauty

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.Gravity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hapi.happy_dialog.FinalDialogFragment
import com.niucube.compui.beauty.Constants.*
import com.niucube.compui.beauty.model.EffectState
import com.niucube.compui.beauty.model.StickerItem
import com.niucube.compui.beauty.util.FileUtils
import com.qiniu.sensetimeplugin.QNSenseTimePlugin
import kotlinx.android.synthetic.main.dialog_sticker.*
import java.io.File
import java.util.*

class StickerDialog : FinalDialogFragment() {

    private var mNewStickers: ArrayList<StickerItem> = ArrayList<StickerItem>()
    private val mStickerAdapter by lazy { StickerAdapter(mNewStickers, requireContext()) }
    private var lastPosition = 0
    private val mSenseTimePlugin: QNSenseTimePlugin
        get() = SenseTimePluginManager.mSenseTimePlugin!!

    init {
        applyGravityStyle(Gravity.BOTTOM)
    }

    override fun getViewLayoutId(): Int {
        return R.layout.dialog_sticker
    }

    fun loadRes(context: Context) {
        FileUtils.copyStickerFiles(context, NEW_ENGINE)
        mNewStickers.clear()
        mNewStickers.addAll(FileUtils.getStickerFiles(context, NEW_ENGINE))
        val iconNone = BitmapFactory.decodeResource(context.resources, R.drawable.none)
        mNewStickers.add(0, StickerItem("清空", iconNone, "").apply {
            state = EffectState.DONE_STATE
        })
        Log.d("StickerDialog", "loadRes ${mNewStickers.size}")
    }

    override fun init() {
        recSticker.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recSticker.adapter = mStickerAdapter
        mStickerAdapter.setSelectedPosition(lastPosition)
        mStickerAdapter.setClickStickerListener { v ->
            val position: Int = v.getTag().toString().toInt()
            val stickerItem: StickerItem = mStickerAdapter.getItem(position)
            mStickerAdapter.setSelectedPosition(position)
            lastPosition = position
            SenseTimePluginManager.mGLHandler?.post {
                val file = File(stickerItem.path)
                Log.d("StickerDialog", "loadRes ${file.exists()}")
                // mSenseTimePlugin.setBeauty(Constants.BEAUTY_TYPES[Constants.ST_BEAUTIFY_WHITEN_STRENGTH], Math.random().toFloat());
                mSenseTimePlugin.setSticker(stickerItem.path)
            }
        }
    }
}