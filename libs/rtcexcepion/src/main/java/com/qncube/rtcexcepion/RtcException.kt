package com.qncube.rtcexcepion

import java.lang.Exception

class RtcException(val code: Int, val msg: String) : Exception(msg)