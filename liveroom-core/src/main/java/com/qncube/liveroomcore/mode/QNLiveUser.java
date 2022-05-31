package com.qncube.liveroomcore.mode;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.HashMap;
import java.util.Map;

public class QNLiveUser {

    @JSONField(name = "user_id")
    public String userId;
    public String avatar;
    public String nick;
    @JSONField(name = "extends")
    public Map<String,String> extensions;
    @JSONField(name = "im_userid")
    public String imUid;
    public String im_username;
}
