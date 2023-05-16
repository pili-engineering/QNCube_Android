package com.qiniu.bzcomp.user;


import java.io.Serializable;

public class LoginToken implements Serializable {

    public String loginToken;
    public String accountId;
    public ImConfig imConfig;
    public String phone;
}
