package com.niucube.module.amusement.mode

import com.niucube.comproom.RoomEntity

fun RoomEntity.asAmusementRoomEntity():AmusementRoomEntity{
    return this as AmusementRoomEntity
}