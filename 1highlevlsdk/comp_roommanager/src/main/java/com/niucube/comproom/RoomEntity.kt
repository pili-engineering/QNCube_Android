package com.niucube.comproom


/**
 * 房间实体抽象
 */
interface  RoomEntity {
    /**
     * 是否加入成功了
     */
    var isJoined:Boolean
    /**
     * 房间ID
     */
     fun provideRoomId():String

    /**
     * 群ID
     */
      fun provideImGroupId():String

    /**
     * 推流地址
     */
     fun providePushUri():String
     fun providePullUri():String

    /**
     * 房间token
     */
     fun provideRoomToken():String

}