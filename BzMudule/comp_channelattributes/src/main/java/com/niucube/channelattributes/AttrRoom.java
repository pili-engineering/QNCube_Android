package com.niucube.channelattributes;

import com.qiniudemo.baseapp.been.Attribute;

import java.util.List;

/**
 * 房间
 */
public class AttrRoom {

    public RoomInfo roomInfo;
    public List<AttrMicSeat> mics;

    public static class RoomInfo {
        public String roomId = "";
        public List<Attribute> attrs = null;
    }
}
