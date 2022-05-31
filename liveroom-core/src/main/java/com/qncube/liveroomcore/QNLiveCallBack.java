package com.qncube.liveroomcore;

public interface QNLiveCallBack<T> {

    void onError(int code, String msg);

    void onSuccess(T data);

}
