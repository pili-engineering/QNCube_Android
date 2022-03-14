package com.hapi.ut.helper;

/**
 * @author xx
 * @date 2018/8/23
 */

public class ActionResult {
    private int code;
    private String mErrorMsg;

    public ActionResult(int code, String errMsg) {
        this.code = code;
        this.mErrorMsg = errMsg;
    }

    public boolean isSuccess() {
        return code == 0;
    }

    public void setErrorMsg(String mErrorMsg) {
        this.mErrorMsg = mErrorMsg;
    }

    public String getErrorMsg() {
        return mErrorMsg;
    }

    public static ActionResult Empty = new ActionResult(0, "");

    public static ActionResult errAction(String msg) {
        return new ActionResult(-1, msg);
    }

    public static ActionResult errAction(int code, String msg) {
        return new ActionResult(code, msg);
    }
}
