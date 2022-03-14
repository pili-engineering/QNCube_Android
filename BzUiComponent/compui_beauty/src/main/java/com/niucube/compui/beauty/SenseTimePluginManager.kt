package com.niucube.compui.beauty

import android.content.Context
import android.os.Handler
import android.widget.Toast
import com.qiniu.sensetimeplugin.QNSenseTimePlugin
import com.niucube.compui.beauty.SenseTimePluginManager

object SenseTimePluginManager {
    var mSenseTimePlugin: QNSenseTimePlugin? = null
        private set
    var mGLHandler: Handler? = null
        private set
    val LICENSE_FILE = "SenseME.lic"

    private var isDestory = false

    fun initEffect(context: Context) {
        mSenseTimePlugin = QNSenseTimePlugin.Builder(context)
            .setLicenseAssetPath(LICENSE_FILE)
            .setModelActionAssetPath("M_SenseME_Face_Video_5.3.3.model")
            .setCatFaceModelAssetPath("M_SenseME_CatFace_3.0.0.model")
            .setDogFaceModelAssetPath("M_SenseME_DogFace_2.0.0.model")
            .build()
        val isAuthorized = mSenseTimePlugin?.checkLicense()
        if (isAuthorized == false) {
            Toast.makeText(context, "鉴权失败，请检查授权文件", Toast.LENGTH_SHORT).show()
        } else {
           // Toast.makeText(context, "SenseME_Face鉴权成功", Toast.LENGTH_SHORT).show()
        }
        isDestory = false
    }

    fun onCaptureStarted() {
        if(isDestory){
            return
        }
        if (mGLHandler == null) {
            mGLHandler = Handler()
        }
        mSenseTimePlugin!!.init()
        mSenseTimePlugin!!.addSubModelFromAssetsFile("M_SenseME_Face_Extra_5.23.0.model")
        mSenseTimePlugin!!.addSubModelFromAssetsFile("M_SenseME_Iris_2.0.0.model")
        mSenseTimePlugin!!.addSubModelFromAssetsFile("M_SenseME_Hand_5.4.0.model")
        mSenseTimePlugin!!.addSubModelFromAssetsFile("M_SenseME_Segment_4.10.8.model")
        mSenseTimePlugin!!.addSubModelFromAssetsFile("M_SenseAR_Segment_MouthOcclusion_FastV1_1.1.1.model")
        mSenseTimePlugin!!.addSubModelFromAssetsFile("M_SenseME_Avatar_Core_2.0.0.model")
        mSenseTimePlugin!!.addSubModelFromAssetsFile("M_SenseME_Avatar_Help_2.0.0.model")
        mSenseTimePlugin!!.recoverEffects()
    }
    fun updateDirection(rotation:Int){

        // 前置摄像头返回的图像是横向镜像的
        mSenseTimePlugin!!.updateDirection(rotation, true, false)
    }

    fun onRenderingFrame(
        isOES: Boolean,
        getTextureId: Int,
        width: Int,
        height: Int
    ): Int {
        if(isDestory){
            return getTextureId
        }
        return mSenseTimePlugin!!.processTexture(getTextureId, width, height, isOES)
    }

    fun release() {
        isDestory = true
        mGLHandler = null
        mSenseTimePlugin?.destroy()
    }
}