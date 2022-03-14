package com.qiniu.bzuicomp.gift;

/**
 * Created by SuperMan on 2018/10/08 0008.
 */

public class GiftNumbBean {

    public static final int SCALE_NUMB = 0;
    public static final int SCROOL_NUMB = 1;

    int count;

    int type;

    public GiftNumbBean(int count, int type) {
        this.count = count;
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
