package com.qncube.uikitcore.dialog

import androidx.annotation.StyleRes
import android.view.ViewGroup
import android.view.Window


fun Window.applyGravityStyle(gravity: Int, @StyleRes resId: Int?, width: Int =  ViewGroup.LayoutParams.MATCH_PARENT,
                             height: Int =  ViewGroup.LayoutParams.WRAP_CONTENT, x: Int = 0, y: Int = 0) {
    val attributes = this.attributes
    attributes.gravity = gravity
    attributes.width =  width
    attributes.height = height
    attributes.x = x
    attributes.y = y
    this.attributes = attributes
    resId?.let { this.setWindowAnimations(it) }
}