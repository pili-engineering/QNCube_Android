package com.qiniudemo.baseapp.manager.swith;


public enum EnvType {
    Dev(2),
    Beta(1),
    Release(0);

    private int type;

    EnvType(int type) {
        this.type = type;
    }

    public static EnvType valueOf(int type) {
        switch (type) {
            case 2:
                return Dev;
            case 1:
                return Beta;
            case 0:
            default:
                return Release;
        }
    }

    public int getType() {
        return type;
    }
}