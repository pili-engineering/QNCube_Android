package com.qiniu.bzcomp.user;


import java.io.Serializable;

public class LoginToken implements Serializable {

    private String loginToken;
    private String accountId;
    private ImConfig imConfig;

    public String getLoginToken() {
        return loginToken;
    }

    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public ImConfig getImConfig() {
        return imConfig;
    }

    public void setImConfig(ImConfig imConfig) {
        this.imConfig = imConfig;
    }
}
