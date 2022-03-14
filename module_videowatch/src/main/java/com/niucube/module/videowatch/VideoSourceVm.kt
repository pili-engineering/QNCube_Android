package com.niucube.module.videowatch

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.hipi.vm.BaseViewModel
import com.hipi.vm.backGround
import com.hipi.vm.bgDefault
import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import com.niucube.comproom.RoomManager
import com.niucube.module.videowatch.core.MovieSignaler
import com.niucube.module.videowatch.mode.Movie
import com.niucube.module.videowatch.service.MovieService
import com.niucube.module.videowatch.core.RtcPubService
import com.niucube.playersdk.player.PlayerStatus
import com.qiniu.bzcomp.network.HttpListData
import com.qiniu.comp.network.RetrofitManager
import com.qiniudemo.baseapp.been.isRoomHost
import com.qiniudemo.baseapp.ext.asToast
import com.qiniudemo.baseapp.widget.CommonTipDialog
import java.lang.Exception

class VideoSourceVm(application: Application, bundle: Bundle?) :
    BaseViewModel(application, bundle) {
    var mCurrentMovieOptionChangeCall: (movie: Movie) -> Unit = {}
    val mCurrentMovieLiveData by lazy { MutableLiveData<Movie>() }
    private val mRoomLifecycleMonitor = object : RoomLifecycleMonitor {
        override fun onRoomJoined(roomEntity: RoomEntity) {
            super.onRoomJoined(roomEntity)
            (bundle?.getSerializable("movie") as Movie?)?.let {
                backGround {
                    doWork {
                        RetrofitManager.create(MovieService::class.java).movieOperation(
                            "add",
                            it.movieId,
                            RoomManager.mCurrentRoom?.provideRoomId() ?: ""
                        )
                    }
                    onFinally {
                        changeMovie(it)
                    }
                }
            }
        }
    }

    init {
        RoomManager.addRoomLifecycleMonitor(mRoomLifecycleMonitor)
    }

    private fun onChangeMovie(movie: Movie) {
        mCurrentMovieOptionChangeCall.invoke(movie)
        mCurrentMovieLiveData.value = movie
    }

    private val selectMovies = ArrayList<Movie>()

    suspend fun refreshSelected(): HttpListData<Movie> {
        val list = RetrofitManager.create(MovieService::class.java)
            .selectedMovieList(
                100,
                1,
                RoomManager.mCurrentRoom?.provideRoomId() ?: ""
            )
        selectMovies.clear()
        selectMovies.addAll(list.list)
        return list
    }

    fun nextMovie(currentMovie: Movie?) {

        backGround {
            doWork {
                if (selectMovies.isEmpty()) {
                    try {
                        refreshSelected()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (selectMovies.isEmpty()) {
                    getFragmentManagrCall?.invoke()?.let {
                        CommonTipDialog.TipBuild()
                            .setContent("播放列表空空的，快去添加")
                            .buildDark()
                            .show(it, "")
                    }
                } else {
                    if (currentMovie == null) {
                        onChangeMovie(selectMovies[0])
                    } else {
                        var currentIndex = 0
                        selectMovies.forEachIndexed { index, movie ->
                            if (currentMovie.movieId == movie.movieId) {
                                currentIndex = index
                                return@forEachIndexed
                            }
                        }
                        currentIndex = ++currentIndex % selectMovies.size
                        onChangeMovie(selectMovies[currentIndex])
                    }
                }
            }
            catchError {
                it.printStackTrace()
                it.message?.asToast()
            }
        }
    }

    suspend fun deleteMovie(movieId: String) {
        try {
            RetrofitManager.create(MovieService::class.java).movieOperation(
                "delete", movieId, RoomManager.mCurrentRoom?.provideRoomId() ?: ""
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun changeMovie(movie: Movie) {
        onChangeMovie(movie)
    }

    override fun onCleared() {
        super.onCleared()
        mCurrentMovieOptionChangeCall={}
    }
}