package com.qizhou.bzupdate


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.hapi.baseframe.dialog.FinalDialogFragment
import com.hapi.ut.helper.ActivityManager
import com.qiniu.comp.network.RetrofitManager
import com.qizhou.bzupdate.dialog.UpdateDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * 更新检查　和当前app业务有关
 * 主要表现　更新接口和更新弹窗
 *
 * 更新下载和当前当前业务无关的
 */
object UpdateHelper {

    /**
     * 开始检查更新
     */
    private var fileProviderName: String = ""

    fun init(fileProviderName: String) {
        this.fileProviderName = fileProviderName
    }

    fun startCheck() {

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val context = ActivityManager.get().currentActivity()
                if (context == null && !(context is AppCompatActivity)) {
                    return@launch
                }
                val code = getVersionCode(context) ?: "0"
                val upDataModel: UpDataModel = RetrofitManager.create(UpDataService::class.java)
                    .updates(code, "android")

//                val upDataModel = UpDataModel().apply {
//                    version = "10.0.1"
//                    packageUrl =
//                        "https://b2ac81c1d0.cainiaoqr.com/fcaaae91eee6706d4cd11e418d37f25c4d02ff9b.apk?auth_key=1646730648-0-0-3b0e282b75e3a6e731f9c8c5295c2d2b"
//                    packagePage = "http://fir.qnsdk.com/s6py"
//                    msg = "ssss"
//                    // status = "1"
              //  }
                val diff = upDataModel.versionCode - code.toInt()
                if (diff <= 0) {
                    Log.d("UpdateHelper", "当前版本--> " + code + "  后台的最新版本--> " + upDataModel.version)
                    return@launch
                }
                startCheck(upDataModel, diff > 5)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun startCheck(upDataModel: UpDataModel, enforcement: Boolean) {
        Log.e("Update---", "Update--111---")

        val updateDialog: UpdateDialog
        if (enforcement) {
            updateDialog = UpdateDialog.newInstance(upDataModel, true)
            startCheck(updateDialog, upDataModel.version, upDataModel.packagePage, true)

        } else {
            updateDialog = UpdateDialog.newInstance(upDataModel, false)
            startCheck(updateDialog, upDataModel.version, upDataModel.packagePage, false)
        }
    }

    private fun startCheck(
        updateDialog: UpdateDialog,
        versionCode: String,
        url: String,
        isForce: Boolean
    ) {

        Log.e("Update---", "Update--444---")
        val context = ActivityManager.get().currentActivity()
        if (context == null && !(context is AppCompatActivity)) {
            return
        }

        updateDialog.setDefaultListener(object : FinalDialogFragment.BaseDialogListener() {
            override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                super.onDialogPositiveClick(dialog, any)
                val uri: Uri = Uri.parse(url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            }
        })
        updateDialog.show((context as AppCompatActivity).supportFragmentManager, "updateDialog")
        Log.e("Update---", "Update--555---")
    }


    /**
     * [获取应用程序build称信息]
     *
     * @param context
     * @return 当前应用的版本名称
     */
    private fun getVersionCode(context: Context): String? {
        try {
            val packInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_CONFIGURATIONS
            )
            return packInfo.versionCode.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }
}

