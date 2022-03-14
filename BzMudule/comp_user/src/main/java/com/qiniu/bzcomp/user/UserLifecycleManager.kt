package com.qiniu.bzcomp.user

import java.util.*

object UserLifecycleManager {

     var userLifecycleInterceptors = ArrayList<UserLifecycleInterceptor>()

    fun addUserLifecycleInterceptor(userLifecycleInterceptor: UserLifecycleInterceptor) {
        userLifecycleInterceptors.add(userLifecycleInterceptor)
    }

    fun removeUserLifecycleInterceptor(userLifecycleInterceptor: UserLifecycleInterceptor) {
        userLifecycleInterceptors.remove(userLifecycleInterceptor)
    }

    fun chain(action: (UserLifecycleInterceptor) -> Unit) {
        userLifecycleInterceptors.forEach(action)
    }
}