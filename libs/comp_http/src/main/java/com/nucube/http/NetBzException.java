package com.nucube.http;

public class NetBzException extends RuntimeException {

    private int code;

    private NetBzException() {
    }

    public NetBzException(String detailMessage) {
        super(detailMessage);
    }

    public NetBzException(int code, String detailMessage) {
        super(detailMessage);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
