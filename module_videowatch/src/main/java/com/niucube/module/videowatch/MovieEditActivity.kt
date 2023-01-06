package com.niucube.module.videowatch

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hapi.baseframe.adapter.QRecyclerViewBindHolder
import com.hapi.baseframe.smartrecycler.QSmartViewBindAdapter
import com.hapi.baseframe.smartrecycler.SmartRecyclerView
import com.hipi.vm.backGround
import com.niucube.comproom.RoomManager
import com.niucube.module.videowatch.databinding.ItemMovieBinding
import com.niucube.module.videowatch.mode.Movie
import com.niucube.module.videowatch.service.MovieService
import com.niucube.player.utils.PalyerUtil
import com.qiniu.comp.network.RetrofitManager
import com.qiniudemo.baseapp.RecyclerActivity
import com.qiniudemo.baseapp.ext.asToast

class MoveWrap(var movie: Movie, var added: Boolean, var checked: Boolean)

//编辑电影
class MovieEditActivity : RecyclerActivity<MoveWrap>() {

    override fun getLayoutId(): Int {
        return R.layout.activity_movie_edit
    }

    override fun isToolBarEnable(): Boolean {
        return true
    }

    override fun isTitleCenter(): Boolean {
        return true
    }

    override fun getInitToolBarTitle(): String {
        return "选择视频"
    }

    override fun getInittittleColor(): Int {
        return Color.parseColor("#ffffff")
    }

    override fun requestToolBarBackground(): Drawable? {
        return ColorDrawable(Color.parseColor("#000000"))
    }

    override val mSmartRecycler: SmartRecyclerView by lazy { findViewById(R.id.smartRecyclerView) }
    override val adapter: MovieListAdapter by lazy { MovieListAdapter() }
    override val layoutManager: RecyclerView.LayoutManager by lazy { GridLayoutManager(this, 2) }
    override val fetcherFuc: (page: Int) -> Unit = {
        backGround {
            doWork {
                val movieAdd = RetrofitManager.create(MovieService::class.java)
                    .selectedMovieList(20, it + 1, RoomManager.mCurrentRoom?.provideRoomId() ?: "")
                val movies = RetrofitManager.create(MovieService::class.java)
                    .movieList(20, it + 1, RoomManager.mCurrentRoom?.provideRoomId() ?: "")
                val warps = ArrayList<MoveWrap>()
                movies.list?.forEach { toAdd ->
                    var isAdd = false
                    movieAdd.list?.forEach {
                        if (toAdd.movieId == it.movieId) {
                            isAdd = true
                            return@forEach
                        }
                    }
                    warps.add(MoveWrap(toAdd, isAdd, false))
                }
                mSmartRecycler.onFetchDataFinish(warps, true, movies.isEndPage)
            }
            catchError {
                mSmartRecycler.onFetchDataError()
                it.printStackTrace()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun refreshSelectedSize(): Int {
        var count = 0
        adapter.data.forEach {
            if (it.checked) {
                count++
            }
        }
        findViewById<TextView>(R.id.tvSelectedCount).text = "已经选择 (${count})"
        return count
    }

    override fun init() {
        super.init()
        refreshSelectedSize()
        findViewById<TextView>(R.id.tvAdd).setOnClickListener {
            if (refreshSelectedSize() == 0) {
                return@setOnClickListener
            }
            backGround {
                showLoading(true)
                doWork {
                    adapter.data.forEach {
                        if (it.checked) {
                            RetrofitManager.create(MovieService::class.java).movieOperation(
                                "add",
                                it.movie.movieId,
                                RoomManager.mCurrentRoom?.provideRoomId() ?: ""
                            )
                        }
                    }
                    mSmartRecycler.post { finish() }
                }
                catchError {
                    it.printStackTrace()
                    it.message?.asToast()
                }
                onFinally {
                    showLoading(false)
                    mSmartRecycler.startRefresh()
                }
            }
        }
    }

    inner class MovieListAdapter : QSmartViewBindAdapter<MoveWrap, ItemMovieBinding>() {

        override fun convertViewBindHolder(
            helper: QRecyclerViewBindHolder<ItemMovieBinding>,
            item: MoveWrap
        ) {
            Glide.with(mContext)
                .load(item.movie.image)
                .into(helper.binding.ivMovieCover)
            helper.binding.tvMovieDuration.text = PalyerUtil.formatTime(item.movie.duration)
            helper.binding.tvMovieName.text = item.movie.name
            if (item.added) {
                helper.binding.checkbox.visibility = View.GONE
                helper.itemView.isClickable = false
            } else {
                helper.binding.checkbox.visibility = View.VISIBLE
                helper.itemView.isClickable = false
            }

            helper.binding.checkbox.isChecked = item.checked
            helper.itemView.setOnClickListener {
                helper.binding.checkbox.performClick()
            }
            helper.binding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                item.checked = isChecked
                helper.binding.tvMovieDuration.isVisible = !isChecked
                refreshSelectedSize()
            }
        }
    }
}