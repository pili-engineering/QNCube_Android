package com.qiniudemo.baseapp.ext

import android.util.Log
import android.widget.Toast
import com.hapi.ut.AppCache

fun String.asToast(){
    Log.d("asToast","asToast ${this}")
    try {
        Toast.makeText(AppCache.getContext(),this,Toast.LENGTH_SHORT).show()
    }catch (e:Exception){
        e.printStackTrace()
        Log.d("asToast","catchcatchcatchcatchcatchcatchcatch ${this}")
    }

}