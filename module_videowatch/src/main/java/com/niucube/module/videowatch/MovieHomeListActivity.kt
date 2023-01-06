package com.niucube.module.videowatch

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hapi.baseframe.adapter.QRecyclerViewBindHolder
import com.hapi.baseframe.smartrecycler.QSmartViewBindAdapter
import com.hapi.baseframe.smartrecycler.SmartRecyclerView
import com.hapi.ut.StatusBarUtil
import com.hipi.vm.backGround
import com.niucube.comproom.RoomManager
import com.niucube.module.videowatch.databinding.ItemMoiveHomeListBinding
import com.niucube.module.videowatch.mode.Movie
import com.niucube.module.videowatch.service.MovieService
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.router.RouterConstant
import com.qiniudemo.baseapp.RecyclerActivity
import com.qiniudemo.baseapp.been.CreateRoomEntity
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.service.RoomService

@Route(path = RouterConstant.VideoRoom.VideoListHome)
class MovieHomeListActivity : RecyclerActivity<Movie>() {

    @Autowired
    @JvmField
    var solutionType = ""
    var defaultType: String = "movie"

    override fun init() {
        super.init()
        StatusBarUtil.setStatusBarTextColor(this, true)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_moive_home_list
    }

    override val mSmartRecycler: SmartRecyclerView by lazy {
        findViewById(R.id.smartRecyclerView)
    }

    override val adapter by lazy { MovieHomeListAdapter() }
    override val layoutManager: RecyclerView.LayoutManager by lazy { LinearLayoutManager(this) }
    override val fetcherFuc: (page: Int) -> Unit = {
        backGround {
            doWork {
                val movies = RetrofitManager.create(MovieService::class.java)
                    .movieList(20, it + 1, RoomManager.mCurrentRoom?.provideRoomId() ?: "")
                mSmartRecycler.onFetchDataFinish(movies.list, true, movies.isEndPage)
            }
            catchError {
                mSmartRecycler.onFetchDataError()
                it.printStackTrace()
            }
        }
    }

    inner class MovieHomeListAdapter : QSmartViewBindAdapter<Movie, ItemMoiveHomeListBinding>() {
        override fun convertViewBindHolder(
            helper: QRecyclerViewBindHolder<ItemMoiveHomeListBinding>,
            item: Movie
        ) {
            Glide.with(mContext)
                .load(item.image)
                .into(helper.binding.ivMovieCover)
            helper.binding.tvMvName.text = item.name
            helper.itemView.setOnClickListener {
                ARouter.getInstance().build(RouterConstant.VideoRoom.VideoHome)
                    .withString("solutionType", solutionType)
                    .withSerializable("movie", item)
                    .navigation(mContext)
            }
            helper.binding.ivUseMv.setOnClickListener {
                backGround {
                    showLoading(true)
                    doWork {
                        val room = RetrofitManager.create(RoomService::class.java)
                            .createRoom(CreateRoomEntity().apply {
                                title = item.name
                                type = solutionType
                            })
                        ARouter.getInstance().build(RouterConstant.VideoRoom.VideoRoom)
                            .withString("solutionType", solutionType)
                            .withString("roomId", room.roomInfo!!.roomId)
                            .withSerializable("movie", item)
                            .navigation(mContext)
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
        }

    }
}