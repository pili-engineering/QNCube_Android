package com.hapi.baseframe.ext

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.ParameterizedType

object ViewBindingExt {

    fun <T : ViewBinding> create(
        sup: ParameterizedType,
        viewGroup: ViewGroup?,
        context: Context,
        attach: Boolean
    ): T {
        var binding: T? = null
        val cls = (sup as ParameterizedType).actualTypeArguments[0] as Class<*>
        try {
            val mInflate = cls.getDeclaredMethod(
                "inflate",
                LayoutInflater::class.java,
                ViewGroup::class.java,
                Boolean::class.java
            )
            binding = mInflate.invoke(null, LayoutInflater.from(context), viewGroup, attach) as T
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        return binding!!
    }

    fun <T : ViewBinding> create2(
        sup: ParameterizedType,
        viewGroup: ViewGroup?,
        context: Context
    ): T {
        var binding: T? = null
        val cls = (sup as ParameterizedType).actualTypeArguments[0] as Class<*>
        try {
            val mInflate = cls.getDeclaredMethod(
                "inflate",
                LayoutInflater::class.java,
                ViewGroup::class.java
            )
            binding = mInflate.invoke(null, LayoutInflater.from(context), viewGroup) as T
           
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        return binding!!
    }

}