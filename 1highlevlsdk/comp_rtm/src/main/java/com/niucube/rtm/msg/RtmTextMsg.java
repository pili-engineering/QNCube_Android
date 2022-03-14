package com.niucube.rtm.msg;

import com.alibaba.fastjson.annotation.JSONField;
import com.qiniu.jsonutil.JsonUtils;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class RtmTextMsg<T> implements RtmMessage , Serializable {

    private String action="";

    private T data =null;

    public RtmTextMsg(){}

    public RtmTextMsg(String action, T data) {
        this.action = action;
        this.data = data;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setData(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    @JSONField(serialize = false)
    @NotNull
    @Override
    public MsgType getMsgType() {
        return MsgType.MsgTypeText;
    }

    @NotNull
    @Override
    public String getAction() {
        return action;
    }

    public String toJsonString(){
        return JsonUtils.INSTANCE.toJson(this);
    }
}
