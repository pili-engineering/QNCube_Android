package com.qiniudemo.baseapp.been

import com.niucube.comproom.RoomEntity
import com.qiniu.bzcomp.user.ImConfig
import java.io.Serializable


open class BaseRoomEntity : Serializable, RoomEntity {
    var rtcInfo: RtcInfo? = null
    var roomInfo: RoomInfo? = null
    var imConfig: ImConfig? = null
    var allUserList:List<RoomMember> = listOf()

    class RtcInfo : Serializable {
        var roomToken = "";
        var publishUrl = ""
        var rtmpPlayUrl = ""
        var flvPlayUrl = ""
        var hlsPlayUrl = ""
    }

    class RoomInfo : Serializable {
        var title = ""
        var status = ""
        var roomId = ""
        var image = ""
        var type = ""
        var desc = ""
        var creator = ""
        var totalUsers = ""
        var params:List<Attribute> = ArrayList<Attribute>()
    }

    override var isJoined = false

    override fun provideRoomId(): String {
        return roomInfo?.roomId ?: ""
    }

    override fun provideImGroupId(): String {
        return imConfig?.imGroupId ?: ""
    }

    override fun providePushUri(): String {
        return rtcInfo?.publishUrl ?: ""
    }

    override fun providePullUri(): String {
        return rtcInfo?.rtmpPlayUrl ?: ""
    }

    override fun provideRoomToken(): String {
        return rtcInfo?.roomToken ?: ""
    }
}


class RoomMember {
    var userId = ""
    var name = ""
    var nickname = ""
    var avatar = ""
    var status = ""
    var profile = ""
}


open class RoomListItem : Serializable {
    var title = ""
    var status = ""
    var roomId = ""
    var image = ""
    var type = ""
    var desc = ""
    var creator = ""
    var attrs: List<Attribute>? = null
    var params: List<Attribute>? = null
    var totalUsers = ""

}

class CreateRoomEntity : Serializable {
    var title = ""
    var desc = ""
    var type = ""
    var image = ""
    var attrs: List<Attribute>? = null
    var params: List<Attribute>? = null
}

class JoinRoomEntity : Serializable {
    var roomId = ""
    var type = ""
    var params: List<Attribute>? = null
}


class HeartBeat : Serializable {
    //下一次心跳
    var interval: String? = null
}



