package com.niucube.module.videowatch

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.hapi.baseframe.adapter.QRecyclerViewBindHolder
import com.hapi.baseframe.smartrecycler.QSmartViewBindAdapter
import com.hapi.baseframe.smartrecycler.SmartRecyclerView
import com.hipi.vm.activityVm
import com.hipi.vm.backGround
import com.niucube.comproom.RoomManager
import com.niucube.module.videowatch.databinding.ItemMovieSelectedBinding
import com.niucube.module.videowatch.mode.Movie
import com.niucube.player.utils.PalyerUtil
import com.qiniudemo.baseapp.RecyclerFragment
import com.qiniudemo.baseapp.been.isRoomHost
import com.qiniudemo.baseapp.ext.asToast

class MovieListFragment : RecyclerFragment<Movie>() {

    companion object {
        @JvmStatic
        fun newInstance() =
            MovieListFragment()
    }

    private val roomVm by activityVm<VideoRoomVm>()
    private val videoSourceVm by activityVm<VideoSourceVm>()
    override val mSmartRecycler: SmartRecyclerView by lazy {
        view?.findViewById(R.id.smartRecyclerView)!!
    }
    override val adapter by lazy { MovieSelectedAdapter() }

    override val layoutManager: RecyclerView.LayoutManager by lazy {
        LinearLayoutManager(
            requireContext()
        )
    }

    @SuppressLint("SetTextI18n")
    override val fetcherFuc: (page: Int) -> Unit = {
        backGround {
            doWork {
                val movies = videoSourceVm.refreshSelected()
                mSmartRecycler.onFetchDataFinish(movies.list, true, movies.isEndPage)
                view?.findViewById<TextView>(R.id.tvSize)?.text = "播放列表 (${adapter.data.size})"
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoSourceVm.mCurrentMovieLiveData.observe(this.viewLifecycleOwner, Observer {
            mSmartRecycler.startRefresh()
        })
        view.findViewById<View>(R.id.ivClose).setOnClickListener {
            backCall.invoke()
        }
        view.findViewById<View>(R.id.tvAdd).setOnClickListener {
            val i = Intent(requireContext(), MovieEditActivity::class.java)
            startActivity(i)
        }
        if (RoomManager.mCurrentRoom?.isRoomHost() == true) {
            view.findViewById<View>(R.id.tvAdd).visibility = View.VISIBLE
        } else {
            view.findViewById<View>(R.id.tvAdd).visibility = View.GONE
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_movie_list
    }

    inner class MovieSelectedAdapter : QSmartViewBindAdapter<Movie, ItemMovieSelectedBinding>() {
        override fun convertViewBindHolder(
            helper: QRecyclerViewBindHolder<ItemMovieSelectedBinding>,
            item: Movie
        ) {
            Glide.with(mContext)
                .load(item.image)
                .into(helper.binding.ivMovieCover)
            helper.binding.tvMovieDuration.text = PalyerUtil.formatTime(item.duration).toString()
            helper.binding.tvMovieName.text = item.name
            val isPlaying =
                videoSourceVm.mCurrentMovieLiveData.value?.movieId == item.movieId
            helper.binding.tvPlaying.isVisible = isPlaying
            helper.binding.ivPlaying.isVisible = isPlaying
            helper.binding.tvMovieName.setTextColor(
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
                if (RoomManager.mCurrentRoom?.isRoomHost() == false) {
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