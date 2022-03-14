package com.niucube.module.videowatch

import com.danikula.videocache.HttpProxyCacheServer
import com.hapi.ut.AppCache


val key_current_movie = "watch_movie_together"
val key_green_matting_pull_uri = "greenMattingPullUri"

val videoCacheProxy: HttpProxyCacheServer by lazy {
    HttpProxyCacheServer(AppCache.getContext())
}
