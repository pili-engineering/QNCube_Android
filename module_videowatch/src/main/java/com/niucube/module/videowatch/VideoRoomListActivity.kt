package com.niucube.module.videowatch

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.util.ParameterizedTypeImpl
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

import com.hapi.happy_dialog.FinalDialogFragment
import com.hapi.base_mvvm.refresh.SmartRecyclerView
import com.hipi.vm.backGround
import com.niucube.module.videowatch.mode.Movie
import com.niucube.module.videowatch.mode.MovieSignal
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
import kotlinx.android.synthetic.main.activity_video_room_list.*
import kotlinx.android.synthetic.main.item_video_room_list_cover.view.*

@Route(path = RouterConstant.VideoRoom.VideoRoomList)
class VideoRoomListActivity : BaseRoomListActivity() {

    override fun isToolBarEnable(): Boolean {
        return true
    }

    override fun getInitToolBarTitle(): String {
        return "房间广场"
    }

    override fun getInittittleColor(): Int {
        return Color.parseColor("#ffffff")
    }

    override fun requestToolBarBackground(): Drawable? {
        return ColorDrawable(Color.parseColor("#000000"))
    }
    override fun isTitleCenter(): Boolean {
        return true
    }

    override var defaultType: String = "movie"

    override val mSmartRecycler: SmartRecyclerView
            by lazy { smartRecyclerView }

    override val adapter: BaseQuickAdapter<RoomListItem, *>
            by lazy {
                VideoRoomItemAdapter().apply {
                    setOnItemClickListener { _, view, position ->
                        ARouter.getInstance().build(RouterConstant.VideoRoom.VideoPlayer)
                            .withString("solutionType", solutionType)
                            .withString("roomId", data[position].roomId)
                            .navigation(this@VideoRoomListActivity)
                    }
                    setOnItemLongClickListener { _, view, position ->
                        ARouter.getInstance().build(RouterConstant.VideoRoom.VideoRoom)
                            .withString("solutionType", solutionType)
                            .withString("roomId", data[position].roomId)
                            .navigation(this@VideoRoomListActivity)
                        true
                    }
                }
            }

    override fun getLayoutId(): Int {
        return R.layout.activity_video_room_list
    }

    override fun initViewData() {
        super.initViewData()
    }

    class VideoRoomItemAdapter : BaseQuickAdapter<RoomListItem,BaseViewHolder>(R.layout.item_video_room_list_cover,ArrayList<RoomListItem>()) {

        override fun convert(helper: BaseViewHolder, item: RoomListItem) {

            Glide.with(mContext)
                .load(item.image)
                .into(helper.itemView.ivMovieCover)
            helper.itemView.tvMember.text = item.totalUsers

            val movieAttribute = JsonUtils.parseObject<MovieSignal>(
                item.attrs?.findValueOfKey(key_current_movie),
                MovieSignal::class.java
            )
            if(TextUtils.isEmpty(movieAttribute?.movieInfo?.name)){
                helper.itemView.tvMovingPlaying.text =item.title
            }else{
                helper.itemView.tvMovingPlaying.text = movieAttribute?.movieInfo?.name
            }
        }
    }

}