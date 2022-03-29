package com.niucube.absroom;

public class UserMicSeatMsg<T> {

    public T seat;
    public String msg = "";

    public UserMicSeatMsg() {
    }

    public UserMicSeatMsg(T seat, String msg) {
        this.seat = seat;
        this.msg = msg;
    }
}
