package com.qiniudemo.baseapp.service;


public class RoomIdType {
    public String type;
    public String roomId;

    public RoomIdType(String type, String roomId) {
        this.type = type;
        this.roomId = roomId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
