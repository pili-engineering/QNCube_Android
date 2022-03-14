package com.niucube.module.videowatch.core

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.niucube.comproom.RoomManager
import com.niucube.module.videowatch.mode.Movie
import com.niucube.module.videowatch.mode.MovieSignal
import com.niucube.module.videowatch.videoCacheProxy
import com.niucube.playersdk.player.PlayerStatus
import com.niucube.playersdk.player.PlayerStatusListener
import com.niucube.playersdk.player.engine.EngineType
import com.niucube.playersdk.player.video.PLLifecycleVideoView
import com.niucube.playersdk.player.video.contronller.DefaultController
import com.qiniudemo.baseapp.been.isRoomHost
import com.qiniudemo.baseapp.ext.asToast
import kotlinx.coroutines.*

import java.lang.Exception

class MovieSignalerPlayer :
    PLLifecycleVideoView {//(context: Context,val roomToken:String, val videoView: PLLifecycleVideoView) {

    private val controller by lazy { DefaultController(context) }
    private var mMovieSignaler: MovieSignaler? = null
    private var mRtcPubService: RtcPubService? = null
    var isHost = false

    private val mPlayerStatusListener = object : PlayerStatusListener {
        override fun onPlayStateChanged(status: Int) {}
        override fun onPlayModeChanged(model: Int) {}
    }

    private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        Log.d("aaa", "exceptionHandler:${throwable}")
    }
    private val mMovieListener = object : MovieSignaler.MovieListener {
        override fun onMovieChange(movieSignal: MovieSignal) {
            controller.reset()
            controller.setTitle(movieSignal.movieInfo!!.name)
            setUp(movieSignal.movieInfo!!)
            if (movieSignal.playStatus == 1) {
                startPlay()
            }
            if (RoomManager.mCurrentRoom?.isRoomHost() == false) {
                if (movieSignal.playStatus == 0) {
                    controller.setPauseAble(false)
                }
            }
        }

        override fun onSyncMovieTime(movieSignal: MovieSignal) {
            if (!isPlaying() || isBufferingPlaying() || isPaused() || isPreparing() || getCurrentPosition() < 1000) {
                Log.d("onSyncMovieTime", "onSyncMovieTime isBufferingPlaying")
                return
            }

            seekTo(movieSignal.currentPosition.toInt() + 2000)
        }

        var isPausedBefore = false

        override fun onMoviePlayStatusChange(movieSignal: MovieSignal) {
            when (movieSignal.playStatus) {
                0 -> {
                    if (isPaused()) {
                        isPausedBefore = true
                    }
                    controller.setPauseAble(false)
                    pause()
                }
                1 -> {
                    controller.setPauseAble(true)
                    if (isPausedBefore) {
                        return
                    }
                    isPausedBefore = false
                    if (isPaused()) {
                        resume()
                    } else {
                        startPlay()
                    }
                }
                2 -> pause()
            }
        }
    }

    private val mProgressUpdateCall = DefaultController.ProgressUpdateCall {
        mMovieSignaler?.updateLocalVideoTime(it)
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        addController(controller)

        controller.setCompletedRestartAble(false)
        controller.mProgressUpdateCall = mProgressUpdateCall
        controller.onBackIconClickListener = OnClickListener {
            if (context is Activity) {
                context.finish()
            }
        }
        addPlayStatusListener(mPlayerStatusListener, true)
    }

    fun setMovieSignaler(movieSignaler: MovieSignaler) {
        mMovieSignaler = movieSignaler
        mMovieSignaler?.mMovieListener = mMovieListener
    }


    fun setRtcPubService(pubService: RtcPubService) {
        mRtcPubService = pubService;
    }

    private var movie: Movie? = null
    private var roomToken: String = ""

    fun init(isHost: Boolean, roomToken: String) {
        this.roomToken = roomToken
        this.isHost = isHost
        controller.setSeekAble(isHost)
    }

    fun setUp(movie: Movie) {
       // movie.playUrl="http://mirror.aarnet.edu.au/pub/TED-talks/911Mothers_2010W-480p.mp4"
        this.movie = movie
        controller.setTitle(movie.name)
        if (engineType == EngineType.QN_PLAYER) {
            setUp(Uri.parse(movie.playUrl), null, false)
        } else {
            val proxyurl = videoCacheProxy.getProxyUrl(movie.playUrl)
            setUp(Uri.parse(proxyurl), null, false)
        }
    }

    override fun startPlay() {
        if (movie == null) {
            return
        }
        if (isHost) {
            GlobalScope.launch(exceptionHandler + Dispatchers.Main) {
                try {

                    val job = async(Dispatchers.IO) {
                        if (mRtcPubService!!.mCurrentTrackID.isNotEmpty()) {
                            try {
                                mRtcPubService!!.deletePubJob(roomToken)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        mRtcPubService!!.createPubJob(
                            roomToken,
                            movie!!.playUrl
                        )
                    }
                    job.await()

                    mMovieSignaler!!.changeMovie(movie!!)
                    super.startPlay()
                } catch (e: Exception) {
                    e.printStackTrace()
                    e.message?.asToast()
                }
            }
        } else {
            super.startPlay()
        }
    }

    var waitRtcPubService = false

    override fun pause() {
        if (isHost) {
            GlobalScope.launch(exceptionHandler + Dispatchers.Main) {
                try {
                    val job = async(Dispatchers.IO) {
                        mRtcPubService!!.stopPubJob(roomToken)
                    }
                    job.await()

                    mMovieSignaler!!.updateLocalVideoStatus(0)
                    if (waitRtcPubService) {
                        super.pause()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    (e.message + "\n 同步rtcPub服务失败").asToast()
                }
            }
        } else {
            if (waitRtcPubService) {
                super.pause()
            }
        }
        if (!waitRtcPubService) {
            super.pause()
        }
    }

    //
    override fun resume() {
        if (isHost) {
            GlobalScope.launch(exceptionHandler + Dispatchers.Main) {
                try {
                    val job = async(Dispatchers.IO) {
                        mRtcPubService!!.startPubJob(roomToken)
                    }
                    job.await()
                    mMovieSignaler!!.updateLocalVideoStatus(1)
                    if (waitRtcPubService) {
                        super.resume()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    (e.message + "\n 同步rtcPub服务失败").asToast()
                }
            }
        } else {
            if (waitRtcPubService) {
                super.resume()
            }
        }
        if (!waitRtcPubService) {
            super.resume()
        }
    }

    override fun seekTo(pos: Int) {
        if (!isPlaying()) {
            return
        }
        if (isHost) {
            GlobalScope.launch(exceptionHandler + Dispatchers.Main) {
                try {
                    val job = async(Dispatchers.IO) {
                        mRtcPubService!!.seekPubJob(roomToken, pos / 1000)
                    }
                    job.await()
                    if (waitRtcPubService) {
                        super.seekTo(pos)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    (e.message + "\n 同步rtcPub服务失败").asToast()
                }
            }
        } else {
            if (waitRtcPubService) {
                super.seekTo(pos)
            }
            //
        }
        if (!waitRtcPubService) {
            super.seekTo(pos)
        }
    }

    override fun releasePlayer() {
        mMovieSignaler?.mMovieListener = null
        super.releasePlayer()
    }

}