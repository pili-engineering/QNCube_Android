package com.qiniu.comp.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


object RetrofitManager {

    var okHttp: OkHttpClient = OkHttpClient.Builder()
        .build()
        private set

    var retrofit = Retrofit.Builder()
        .client(okHttp)
        .baseUrl("https://api.github.com/")
        .build()
        private set
    private val scheduler = Schedulers.from(Executors.newFixedThreadPool(10));

    var baseUrl = ""
    fun resetConfig(config: NetConfig) {
        baseUrl = config.base
        okHttp = config.okBuilder.callTimeout(10 * 1000L, TimeUnit.MILLISECONDS).build()
        retrofit = Retrofit.Builder()
            .client(okHttp)
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(scheduler))
            .addConverterFactory(config.converterFactory)
            .baseUrl(config.base)
            .build()
    }

    fun <T> create(service: Class<T>?): T {
        return retrofit.create(service)
    }

    fun post(url: String, body: RequestBody): Response {
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build();
        val call = okHttp.newCall(request);
        return call.execute()
    }

    private val okHttpExt by lazy { OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }).build() }
    fun postFormUserExtraClient(
        url: String,
        body: RequestBody,
        headerKey: String,
        headerValue: String
    ): Response {
        val request = Request.Builder()
            .url(url)
            .addHeader(headerKey, headerValue)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .post(body)
            .build();
        val call = okHttpExt.newCall(request);
        return call.execute()
    }

    fun postJsonUserExtraClient(url: String, body: RequestBody): Response {
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build();
        val call = okHttpExt.newCall(request);
        return call.execute()
    }

    fun delete(url: String): Response {
        val request = Request.Builder()
            .url(url)
            .delete()
            .build();
        val call = okHttpExt.newCall(request);
        return call.execute()
    }


    fun get(url: String): Response {
        val request = Request.Builder()
            .url(url)
            .get()
            .build();
        val call = okHttp.newCall(request);
        return call.execute()
    }
}