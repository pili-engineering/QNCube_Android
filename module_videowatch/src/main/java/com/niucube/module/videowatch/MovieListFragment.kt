package com.niucube.module.videowatch

import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hapi.base_mvvm.refresh.SmartRecyclerView
import com.hipi.vm.activityVm
import com.hipi.vm.backGround
import com.niucube.comproom.RoomManager
import com.niucube.module.videowatch.mode.Movie
import com.niucube.player.utils.PalyerUtil
import com.qiniudemo.baseapp.RecyclerFragment
import com.qiniudemo.baseapp.been.isRoomHost
import com.qiniudemo.baseapp.ext.asToast
import kotlinx.android.synthetic.main.fragment_movie_list.*
import kotlinx.android.synthetic.main.item_movie_selected.view.*


class MovieListFragment : RecyclerFragment<Movie>() {

    companion object {
        @JvmStatic
        fun newInstance() =
            MovieListFragment()
    }

    private val roomVm by activityVm<VideoRoomVm>()
    private val videoSourceVm by activityVm<VideoSourceVm>()
    override val mSmartRecycler: SmartRecyclerView by lazy {
        smartRecyclerView
    }
    override val adapter: BaseQuickAdapter<Movie, *> by lazy { MovieSelectedAdapter() }

    override val layoutManager: RecyclerView.LayoutManager by lazy {
        LinearLayoutManager(
            requireContext()
        )
    }
    override val fetcherFuc: (page: Int) -> Unit = {
        backGround {
            doWork {
                val movies =videoSourceVm.refreshSelected()
                mSmartRecycler.onFetchDataFinish(movies.list, true, movies.isEndPage)
                tvSize.text = "播放列表 (${adapter.data.size})"
            }
            catchError {
                mSmartRecycler.onFetchDataError()
                it.printStackTrace()
            }
        }
    }

    var backCall = {}

    override fun isRefreshAtOnStart(): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        mSmartRecycler.startRefresh()
    }

    override fun initViewData() {
        super.initViewData()
        videoSourceVm.mCurrentMovieLiveData.observe(this, Observer {
            mSmartRecycler.startRefresh()
        })
        ivClose.setOnClickListener {
            backCall.invoke()
        }
        tvAdd.setOnClickListener {
            val i = Intent(requireContext(), MovieEditActivity::class.java)
            startActivity(i)
        }
        if(RoomManager.mCurrentRoom?.isRoomHost()==true){
            tvAdd.visibility = View.VISIBLE
        }else{
            tvAdd.visibility = View.GONE
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_movie_list
    }

    inner class MovieSelectedAdapter :
        BaseQuickAdapter<Movie, BaseViewHolder>(R.layout.item_movie_selected, ArrayList<Movie>()) {
        override fun convert(helper: BaseViewHolder, item: Movie) {
            Glide.with(mContext)
                .load(item.image)
                .into(helper.itemView.ivMovieCover)
            helper.itemView.tvMovieDuration.text = PalyerUtil.formatTime(item.duration).toString()
            helper.itemView.tvMovieName.text = item.name
            val isPlaying =
                videoSourceVm.mCurrentMovieLiveData.value?.movieId== item.movieId
            helper.itemView.tvPlaying.isVisible = isPlaying
            helper.itemView.ivPlaying.isVisible = isPlaying
            helper.itemView.tvMovieName.setTextColor(
                if (isPlaying) {
                    Color.parseColor("#68C7F5")
                } else {
                    Color.parseColor("#ffffff")
                }
            )

            helper.itemView.setOnClickListener {
                if (isPlaying) {
                    return@setOnClickListener
                }
                if(RoomManager.mCurrentRoom?.isRoomHost()==false){
                   return@setOnClickListener
                }
                backGround {
                    doWork {
                        videoSourceVm.changeMovie(item)
                    }
                    catchError {
                        it.printStackTrace()
                        it.message?.asToast()
                    }
                }
            }
        }
    }
}