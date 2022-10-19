package com.niucube.overhaul.fbo

import android.graphics.*
import android.util.Base64
import android.util.Log
import com.hapi.ut.AppCache
import com.niucube.overhaul.R
import com.niucube.overhaul.mode.CircuiBoard
import com.qiniu.cdn.CdnManager
import com.qiniu.common.Constants
import com.qiniu.comp.network.RetrofitManager
import com.qiniu.droid.rtc.QNVideoFrameListener
import com.qiniu.droid.rtc.QNVideoFrameType
import com.qiniu.http.Client
import com.qiniu.http.Headers
import com.qiniu.jsonutil.JsonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.nio.ByteBuffer
import com.qiniu.util.Auth
import com.qiniu.util.Json
import com.qiniu.util.StringMap
import com.qiniu.util.StringUtils
import com.qiniudemo.baseapp.ext.asToast
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import okio.BufferedSink
import java.util.HashMap

/**
 * 电路板识别
 */
class AicCircuitBoardFrameListener : QNVideoFrameListener {

    var appKey = ""
    var appSecretKey = ""
    private var lastTimestampNs = 0L
    private var isCircuitBoard = false
    override fun onYUVFrameAvailable(
        data: ByteArray,
        type: QNVideoFrameType,
        width: Int,
        height: Int,
        rotation: Int,
        timestampNs: Long
    ) {
        val now = System.currentTimeMillis()
        if (now - lastTimestampNs < 5000) {
            return
        }
        isCircuitBoard = false
        lastTimestampNs = now
        Log.d("circuitboard", "circuitboard ${type.name}")

        val fos = ByteArrayOutputStream()
        val yuvImage = YuvImage(data, ImageFormat.NV21, width, height, null)
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 50, fos)
        val dataRGB = fos.toByteArray()

        val img = "data:application/octet-stream;base64,${
            Base64.encodeToString(
                dataRGB,
                Base64.DEFAULT
            )
        }"

        val body = FormBody.Builder()
            .add("data.uri", img)
            .build()
        val buffer = Buffer()
        body.writeTo(buffer)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val auth = Auth.create(appKey, appSecretKey)

                val url = "http://10.200.20.73:9001/v1/image/circuitboard"
                // val url = "http://cb-service.apistore.qiniu.com/v1/image/circuitboard"
                val header = Headers.of(HashMap<String, String>().apply {
                    put("Content-Type", Client.FormMime)
                })
                val authToken =
                    auth.signQiniuAuthorization(url, "POST", buffer.readByteArray(), header)
                //朵拉的电路板识别服务
                val resp =
                    RetrofitManager.postFormUserExtraClient(
                        url,
                        body,
                        "Authorization",
                        "Qiniu " + authToken
                    )

                val bodyString = resp.body!!.string()
                Log.d("circuitboard", "circuitboard ${bodyString}")
                val circuiBoard =
                    JsonUtils.parseObject(bodyString, CircuiBoard::class.java) ?: return@launch
                if (circuiBoard.code == 201 && circuiBoard.data.label == "positives") {
//                    if (markIdsIndex == markIds.size - 1) {
//                        markIdsIndex = 0
//
//                    }
                    GlobalScope.launch(Dispatchers.Main) {
                        "检测到此PCB疑似有【BGA球窝】问题，请联系专家确认".asToast()
                    }
                    isCircuitBoard = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var mTextureRenderer: TextureRenderer? = null
    private val coverBitmap90 by lazy {
        //前置 90 后置 270
        val m = Matrix()
        m.postRotate(-90f) //旋转-90度
        val options = BitmapFactory.Options()
        options.inScaled = false
        val bitmap1 = BitmapFactory.decodeResource(
            AppCache.getContext().resources,
            R.drawable.bg_board,
            options
        )
        val bitmap = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.width, bitmap1.height, m, true)
        bitmap
    }

    private val coverBitmap270 by lazy {
        //前置 90 后置 270
        val m = Matrix()
        m.postScale(-1f, 1f) //镜像水平翻转
        m.postRotate(-270f) //旋转-90度
        val options = BitmapFactory.Options()

        options.inScaled = false
        val bitmap1 = BitmapFactory.decodeResource(
            AppCache.getContext().resources,
            R.drawable.bg_board,
            options
        )
        val bitmap = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.width, bitmap1.height, m, true)
        bitmap
    }

    override fun onTextureFrameAvailable(
        textureID: Int,
        type: QNVideoFrameType?,
        width: Int,
        height: Int,
        rotation: Int,
        timestampNs: Long,
        transformMatrix: FloatArray?
    ): Int {
        //   return textureID
        if (!isCircuitBoard) {
            return textureID
        }

        var markedId = 0
        if (mTextureRenderer == null) {
            mTextureRenderer = TextureRenderer()
            mTextureRenderer!!.onSurfaceCreated()
            mTextureRenderer!!.onSurfaceChanged(width, height)

            TextureUtils.init()
        }
        val mark = TextureUtils.loadTexture(
            if (rotation == 270) {
                coverBitmap270
            } else {
                coverBitmap90
            }
        );
        markedId = mTextureRenderer!!.onDrawFrame(textureID, mark, rotation)
        Log.d("onTextureFrameAvailable", "onTextureFrameAvailable " + markedId)
        return markedId
    }
}
