package com.qncube.liveroomcore.innermodel;

import com.alibaba.fastjson.annotation.JSONField;
import com.qncube.liveroomcore.mode.QNLiveUser;

import java.util.Map;

public class InnerUser  extends QNLiveUser {
    @JSONField (serialize = false)
    public String im_password;

}
