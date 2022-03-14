package com.niucube.module.videowatch.core

import android.text.TextUtils
import android.util.Log
import com.niucube.channelattributes.AttributesCallBack
import com.niucube.channelattributes.RoomAttributesListener
import com.niucube.channelattributes.RoomAttributesManager
import com.niucube.comproom.RoomEntity
import com.niucube.comproom.RoomLifecycleMonitor
import com.niucube.comproom.RoomManager
import com.niucube.module.videowatch.key_current_movie
import com.niucube.module.videowatch.mode.Movie
import com.niucube.module.videowatch.mode.MovieSignal
import com.qiniu.bzcomp.user.UserInfoManager
import com.qiniu.jsonutil.JsonUtils
import com.qiniudemo.baseapp.been.isRoomHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MovieSignaler() {

    var mCurrentMovieSignal: MovieSignal? = null
        private set

    var mMovieListener: MovieListener? = null

    val maxTimeDifference = 3000

    private val syncMovieJob = {
        GlobalScope.launch(Dispatchers.Main) {
            val room = RoomManager.mCurrentRoom
            //  while (room != null) {
            try {
                val attr = RoomAttributesManager.getRoomAttributesByKeys(
                    room!!.provideRoomId(), key_current_movie
                ) ?: return@launch
                if (TextUtils.isEmpty(attr.key)) {
                    return@launch
                }
                mRoomAttributesListener.onAttributesChange(
                    room.provideRoomId(), attr.key, attr.value
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            //  delay(5000)
            // }
        }
    }


    private val mRoomLifecycleMonitor = object : RoomLifecycleMonitor {
        override fun onRoomEntering(roomEntity: RoomEntity) {
            super.onRoomEntering(roomEntity)
        }

        override fun onRoomJoined(roomEntity: RoomEntity) {
            super.onRoomJoined(roomEntity)
            syncMovieJob.invoke()
        }

        override fun onRoomClosed(roomEntity: RoomEntity?) {
            super.onRoomClosed(roomEntity)
            RoomAttributesManager.removeRoomAttributesListener(mRoomAttributesListener)
        }
    }

    private val mRoomAttributesListener = object : RoomAttributesListener {
        override fun onAttributesChange(roomId: String, key: String, values: String) {
            if (key == key_current_movie && roomId == RoomManager.mCurrentRoom?.provideRoomId()) {
                val movieSignal = JsonUtils.parseObject(values, MovieSignal::class.java) ?: return
                if (mCurrentMovieSignal == null ||
                    mCurrentMovieSignal?.videoId != movieSignal.videoId
                ) {
                    mCurrentMovieSignal = movieSignal
                    mMovieListener?.onMovieChange(movieSignal)
                    return
                }
                if (movieSignal.playStatus != mCurrentMovieSignal?.playStatus) {
                    mCurrentMovieSignal = movieSignal
                    mMovieListener?.onMoviePlayStatusChange(movieSignal)
                    Log.d("MovieSignaler", "onMoviePlayStatusChange " + movieSignal.playStatus)
                }

                if (Math.abs(movieSignal.currentPosition - mCurrentMovieSignal!!.currentPosition) > maxTimeDifference) {
                    mMovieListener?.onSyncMovieTime(movieSignal)
                    Log.d("MovieSignaler", "onSyncMovieTime " + movieSignal.currentPosition)
                }
            }
        }
    }

    init {
        RoomManager.addRoomLifecycleMonitor(mRoomLifecycleMonitor)
        RoomAttributesManager.addRoomAttributesListener(mRoomAttributesListener)
    }


    //跟新本地播放position
    fun updateLocalVideoTime(position: Long) {
        Log.d("MovieSignaler", "updateLocalVideoTime " + position)
        mCurrentMovieSignal?.let {
            it.currentPosition = position
            if (RoomManager.mCurrentRoom?.isRoomHost() == true) {
                it.currentTimeMillis = System.currentTimeMillis()
                RoomAttributesManager.putRoomAttributes(
                    RoomManager.mCurrentRoom?.provideRoomId() ?: "",
                    key_current_movie,
                    JsonUtils.toJson(it),
                    true,
                    false,
                    false,
                    object : AttributesCallBack<Unit> {
                        override fun onSuccess(data: Unit) {
                        }

                        override fun onFailure(errorCode: Int, msg: String) {
                        }
                    }
                )
            }
        }
    }

    fun updateLocalVideoStatus(status: Int) {
        mCurrentMovieSignal?.let {
            if (RoomManager.mCurrentRoom?.isRoomHost() == true) {
                it.playStatus = status
                it.currentTimeMillis = System.currentTimeMillis()
                RoomAttributesManager.putRoomAttributes(
                    RoomManager.mCurrentRoom?.provideRoomId() ?: "",
                    key_current_movie,
                    JsonUtils.toJson(it),
                    true,
                    false,
                    true,
                    object : AttributesCallBack<Unit> {
                        override fun onSuccess(data: Unit) {
                        }

                        override fun onFailure(errorCode: Int, msg: String) {
                        }
                    }
                )
            }
        }
    }

    suspend fun changeMovie(movie: Movie) = suspendCoroutine<Unit> { continuation ->
        if (RoomManager.mCurrentRoom?.isRoomHost() == true) {
            val movieSignal = MovieSignal().apply {
                videoId = movie.movieId
                videoUid = UserInfoManager.getUserId()
                startTimeMillis = System.currentTimeMillis()
                currentTimeMillis = System.currentTimeMillis()
                currentPosition = 0
                playStatus = 1
                movieInfo = movie
            }
            mCurrentMovieSignal = movieSignal
            RoomAttributesManager.putRoomAttributes(
                RoomManager.mCurrentRoom?.provideRoomId() ?: "",
                key_current_movie,
                JsonUtils.toJson(movieSignal),
                true,
                false,
                true,
                object : AttributesCallBack<Unit> {
                    override fun onSuccess(data: Unit) {
                        continuation.resume(Unit)
                    }

                    override fun onFailure(errorCode: Int, msg: String) {
                        continuation.resumeWithException(Exception(msg))
                    }
                }
            )
        }
    }

    interface MovieListener {
        fun onMovieChange(movieSignal: MovieSignal)
        fun onSyncMovieTime(movieSignal: MovieSignal)
        fun onMoviePlayStatusChange(movieSignal: MovieSignal)
    }
}