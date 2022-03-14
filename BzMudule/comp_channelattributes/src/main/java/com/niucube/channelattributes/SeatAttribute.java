package com.niucube.channelattributes;

import com.qiniu.jsonutil.JsonUtils;
import com.qiniudemo.baseapp.been.Attribute;

import java.io.Serializable;

class SeatAttribute extends Attribute implements Serializable {
    public String roomId;
    public String uid;
    public String toJson(){
        return JsonUtils.INSTANCE.toJson(this);
    }
}