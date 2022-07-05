package com.niucube.basemutableroom.absroom

import java.lang.Exception

//操作回调
interface RtcOperationCallback {
    companion object{
        const val error_seat_status = 1 //麦位状态错误
        const val error_room_not_join = 2 //房间没有加入
        const val error_room_role_no_permission = 3 //角色没有权限
    }
    fun onSuccess()

    /**
     * @param errorCode  0 - 100 highlevelsdk报错  负数 im报错 -imErrorCode ,else rtc错误码
     */
    fun onFailure(errorCode:Int,msg:String)
}
class RtcOperationException(val code: Int, val msg: String) : Exception(msg)