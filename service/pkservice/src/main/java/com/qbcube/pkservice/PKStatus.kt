package com.qbcube.pkservice

enum class PKStatus(val intValue: Int) {
    RelaySessionStatusWaitAgree(0),//等待接收方同意
    RelaySessionStatusAgreed(1),//接收方已同意
    RelaySessionStatusInitSuccess(2),//发起方已经完成跨房，等待对方完成
    RelaySessionStatusRecvSuccess(3),//接收方已经完成跨房，等待对方完成
    RelaySessionStatusSuccess(4),//两方都完成跨房
    RelaySessionStatusRejected(5),//接收方拒绝
    RelaySessionStatusStopped(6),//结束
}