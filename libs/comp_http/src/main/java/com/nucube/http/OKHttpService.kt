package com.nucube.http

import com.alibaba.fastjson.util.ParameterizedTypeImpl
import com.qiniu.jsonutil.JsonUtils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.lang.reflect.Type
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object OKHttpService {

    private val baseUrl = "http://10.200.20.28:8099"
    var token = ""
    val bzCodeNoError = 0
    private val mExecutorService = ThreadPoolExecutor(
        8, 100,
        60L, TimeUnit.MILLISECONDS,
        LinkedBlockingQueue<Runnable>()
    );

    private val logInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    var okHttp: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logInterceptor)
        .build()
        private set

    val josnType = "application/json;charset=utf-8".toMediaType()


    private fun <T> request(
        method: String,
        path: String,
        jsonString: String,
        clazz: Class<T>? = null,
        type: Type? = null
    ): T {
        val body = jsonString.toRequestBody(josnType)
        val requestOrigin = Request.Builder()
            .url(baseUrl + path)
            .addHeader("Authorization", token)
            .addHeader("Content-Type", "application/json")

        val request = when (method) {
            "post" -> {
                requestOrigin.post(body).build()
            }
            "put" -> {
                requestOrigin.put(body).build()
            }
            "delete" -> {
                requestOrigin.delete(body).build()
            }
            else -> {
                requestOrigin.get().build()
            }
        }

        val call = okHttp.newCall(request);
        val resp = call.execute()
        val code = resp.code
        if (code == 200) {
            val bodyStr = resp.body?.string()
            val p = ParameterizedTypeImpl(
                arrayOf(clazz ?: type),
                HttpResp::class.java,
                HttpResp::class.java
            )
            val obj = JsonUtils.parseObject<HttpResp<T>>(bodyStr, p)
            if (code != bzCodeNoError && obj != null) {
                return (obj.data)
            } else {
                throw (NetBzException(code, resp.message))
            }
        } else {
            throw (NetBzException(code, resp.message))
        }
    }


    suspend fun <T> put(
        path: String, jsonString: String, clazz: Class<T>? = null,
        type: Type? = null
    ) = suspendCoroutine<T> { contine ->
        mExecutorService.execute {
            try {
                val resp = request("put", path, jsonString, clazz, type)
                contine.resume(resp)
            } catch (e: Exception) {
                contine.resumeWithException(e)
            }
        }
    }

    suspend fun <T> post(
        path: String, jsonString: String, clazz: Class<T>? = null,
        type: Type? = null
    ) = suspendCoroutine<T> { contine ->

        mExecutorService.execute {
            try {
                val resp = request("post", path, jsonString, clazz, type)
                contine.resume(resp)
            } catch (e: Exception) {
                contine.resumeWithException(e)
            }
        }
    }

    suspend fun <T> delete(
        path: String, jsonString: String, clazz: Class<T>? = null,
        type: Type? = null
    ) = suspendCoroutine<T> { contine ->

        mExecutorService.execute {
            try {
                val resp = request("delete", path, jsonString, clazz, type)
                contine.resume(resp)
            } catch (e: Exception) {
                contine.resumeWithException(e)
            }
        }
    }


    suspend fun <T> get(
        path: String,
        map: Map<String, String>?,
        clazz: Class<T>? = null,
        type: Type? = null
    ) = suspendCoroutine<T> { contine ->
        var params = ""
        map?.let {
            params += "?"
            it.entries.forEachIndexed { index, it ->
                params += if (index == 0) {
                    ""
                } else {
                    "&"
                } + it.key + "=" + it.value
            }
        }
        val path2 = path + params

        mExecutorService.execute {
            try {
                val resp = request("get", path2, "{}", clazz, type)
                contine.resume(resp)
            } catch (e: Exception) {
                contine.resumeWithException(e)
            }
        }
    }

}