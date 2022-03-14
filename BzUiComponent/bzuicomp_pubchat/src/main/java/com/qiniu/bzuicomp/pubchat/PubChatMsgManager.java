package com.qiniu.bzuicomp.pubchat;

import java.util.ArrayList;
import java.util.List;

public class PubChatMsgManager {

    public static List<IChatMsgCall> iChatMsgCalls = new ArrayList<IChatMsgCall>();

    public static void onNewMsg(IChatMsg msg) {
        for (IChatMsgCall call : iChatMsgCalls){
            call.onNewMsg(msg);
        }
    }

    public interface IChatMsgCall {
        public void onNewMsg(IChatMsg msg);
    }
}
