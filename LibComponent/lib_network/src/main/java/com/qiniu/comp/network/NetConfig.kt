package com.qiniu.comp.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NetConfig {

    var base = ""
    var converterFactory: Converter.Factory = GsonConverterFactory.create()
    var logInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    var okBuilder = OkHttpClient.Builder().connectTimeout(30000, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(true)
}