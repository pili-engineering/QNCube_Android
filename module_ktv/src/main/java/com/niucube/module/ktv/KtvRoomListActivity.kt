package com.niucube.module.ktv

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.util.ParameterizedTypeImpl
import com.hapi.baseframe.adapter.QRecyclerViewBindHolder
import com.hapi.baseframe.dialog.FinalDialogFragment
import com.hapi.baseframe.smartrecycler.IAdapter
import com.hapi.baseframe.smartrecycler.SmartRecyclerView
import com.hipi.vm.backGround
import com.niucube.module.ktv.mode.Song
import com.niucube.qrtcroom.ktvkit.KTVMusic
import com.niucube.qrtcroom.ktvkit.KTVSerialPlayer.Companion.key_current_music
import com.qiniu.baseapp.databinding.ItemRoomListBinding
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.jsonutil.JsonUtils
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseRoomListActivity
import com.qiniudemo.baseapp.been.CreateRoomEntity
import com.qiniudemo.baseapp.been.RoomListItem
import com.qiniudemo.baseapp.been.findValueOfKey
import com.qiniudemo.baseapp.dialog.CommonCreateRoomDialog
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.service.RoomService

@Route(path = RouterConstant.KTV.KTVList)
class KtvRoomListActivity : BaseRoomListActivity() {

    override var defaultType: String = "ktv"

    override fun getLayoutId(): Int = R.layout.activity_ktv_room_list
    override val mSmartRecycler: SmartRecyclerView
            by lazy { findViewById(R.id.smartRecyclerView) }
    override val adapter: IAdapter<RoomListItem> by lazy {
        KTVAdapter().apply {
            setOnItemClickListener { _, view, position ->
                ARouter.getInstance().build(RouterConstant.KTV.KTVRoom)
                    .withString("solutionType", solutionType)
                    .withString("ktvRoomId", data[position].roomId)
                    .navigation(this@KtvRoomListActivity)
            }
        }
    }

    override fun init() {
        super.init()
        findViewById<TextView>(R.id.tvCreateRoom).setOnClickListener {

            CommonCreateRoomDialog().apply {
                setDefaultListener(object : FinalDialogFragment.BaseDialogListener() {
                    override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                        super.onDialogPositiveClick(dialog, any)
                        backGround {
                            showLoading(true)
                            doWork {
                                val room = RetrofitManager.create(RoomService::class.java)
                                    .createRoom(CreateRoomEntity().apply {
                                        title = any.toString()
                                        type = solutionType
                                    })
                                ARouter.getInstance().build(RouterConstant.KTV.KTVRoom)
                                    .withString("solutionType", solutionType)
                                    .withString("ktvRoomId", room.roomInfo!!.roomId)
                                    .navigation(this@KtvRoomListActivity)
                            }
                            catchError {
                                it.printStackTrace()
                                it.message?.asToast()
                            }
                            onFinally {
                                showLoading(false)
                            }
                        }
                    }
                }).show(supportFragmentManager, "")
            }
        }
    }

    class KTVAdapter : BaseRoomItemAdapter() {

        override fun getCoverLayout(parent: ViewGroup): View? {
            return LayoutInflater.from(mContext)
                .inflate(R.layout.item_ktv_room_list_cover, parent, false)
        }

        @SuppressLint("SetTextI18n")
        override fun convertViewBindHolder(
            helper: QRecyclerViewBindHolder<ItemRoomListBinding>,
            item: RoomListItem
        ) {
            val pt = ParameterizedTypeImpl(
                arrayOf(Song::class.java),
                KTVMusic::class.java,
                KTVMusic::class.java
            )
            val musicAttribute = JsonUtils.parseObject<KTVMusic<Song>>(
                item.attrs?.findValueOfKey(key_current_music),
                pt
            )
            if (TextUtils.isEmpty(musicAttribute?.musicInfo?.name)) {
                helper.itemView.findViewById<TextView>(R.id.tvKtvMusicPlaying).text = ""
            } else {
                helper.itemView.findViewById<TextView>(R.id.tvKtvMusicPlaying).text =
                    (musicAttribute?.musicInfo?.name
                        ?: "") + "+" + (musicAttribute?.musicInfo?.author ?: "")
            }
        }
    }

}