package com.niucube.module.videowatch

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.TextUtils
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.hapi.baseframe.adapter.QRecyclerViewBindHolder
import com.hapi.baseframe.smartrecycler.QSmartViewBindAdapter
import com.hapi.baseframe.smartrecycler.SmartRecyclerView
import com.niucube.module.videowatch.databinding.ItemVideoRoomListCoverBinding

import com.niucube.module.videowatch.mode.MovieSignal
import com.qiniu.jsonutil.JsonUtils
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.BaseRoomListActivity
import com.qiniudemo.baseapp.been.RoomListItem
import com.qiniudemo.baseapp.been.findValueOfKey

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
            by lazy { findViewById(R.id.smartRecyclerView) }

    override val adapter by lazy {
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

    class VideoRoomItemAdapter :
        QSmartViewBindAdapter<RoomListItem, ItemVideoRoomListCoverBinding>() {

        override fun convertViewBindHolder(
            helper: QRecyclerViewBindHolder<ItemVideoRoomListCoverBinding>,
            item: RoomListItem
        ) {
            Glide.with(mContext)
                .load(item.image)
                .into(helper.binding.ivMovieCover)
            helper.binding.tvMember.text = item.totalUsers

            val movieAttribute = JsonUtils.parseObject<MovieSignal>(
                item.attrs?.findValueOfKey(key_current_movie),
                MovieSignal::class.java
            )
            if (TextUtils.isEmpty(movieAttribute?.movieInfo?.name)) {
                helper.binding.tvMovingPlaying.text = item.title
            } else {
                helper.binding.tvMovingPlaying.text = movieAttribute?.movieInfo?.name
            }
        }
    }
}