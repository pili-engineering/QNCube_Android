package com.hapi.ut.helper;

public class RequestCodeCreator {
    private RequestCodeCreator() {
        // Disabled
    }

    /**
     * The range of request code for startActivityForResult is [0, 32767],
     * startActivityForResult, java.lang.IllegalArgumentException:
     * [throwable: Can only use lower 16 bits for requestCode],
     * the range of int16 is [-32768, +32767].
     * The ID in [12312, 32121] seems can avoid to conflict with some third-party SDK,
     * such as SSO API, for instance:
     * QQ, 10103, 11101, and so on.
     * Weibo, 32973, 40000, and so on.
     * Wechat, ...
     */
    private static final int ACTIVITY_REQUEST_CODE_MIN = 0;
    private static final int ACTIVITY_REQUEST_CODE_MAX = 32121;
    private static final int ACTIVITY_REQUEST_CODE_BASE = 12312;
    private static int sActivityRequestCode = ACTIVITY_REQUEST_CODE_BASE;

    public synchronized static int generate() {
        final int code = sActivityRequestCode++;
        if (ACTIVITY_REQUEST_CODE_MAX == sActivityRequestCode) {
            sActivityRequestCode = ACTIVITY_REQUEST_CODE_BASE;
        }
        return code;
    }
}
