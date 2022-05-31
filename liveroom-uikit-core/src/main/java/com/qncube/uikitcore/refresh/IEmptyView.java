package com.qncube.uikitcore.refresh;

import android.view.View;

public interface IEmptyView {

    /**
     * 隐藏
     */
    int HIDE_LAYOUT = 3;
    /**
     * 网络异常
     */
    int NETWORK_ERROR = 1;
    /**
     * 服务器数据空
     */
    int NODATA = 2;


    int START_REFREASH_WHEN_EMPTY=-1; //数据为空时候刷新


    View getContentView();


    void setStatus(int type);


}
