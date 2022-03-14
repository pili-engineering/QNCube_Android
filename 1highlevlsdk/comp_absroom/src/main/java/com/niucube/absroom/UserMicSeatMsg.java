package com.niucube.absroom;

public class UserMicSeatMsg<T> {

    public T userMicSeat;
    public String msg="";
 public UserMicSeatMsg(){}
    public UserMicSeatMsg(T userMicSeat, String msg) {
        this.userMicSeat = userMicSeat;
        this.msg = msg;
    }
}
