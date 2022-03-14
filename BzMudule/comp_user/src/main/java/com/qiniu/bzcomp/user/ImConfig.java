package com.qiniu.bzcomp.user;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class ImConfig implements Serializable, Parcelable {


    private String imToken;
    private int type;
    private String imUsername;
    private String imPassword;
    private String imUid;
    private String imGroupId;


    public ImConfig(){

    }
    protected ImConfig(Parcel in) {
        imToken = in.readString();
        type = in.readInt();
        imUsername = in.readString();
        imPassword = in.readString();
        imUid = in.readString();
        imGroupId = in.readString();
    }

    public static final Creator<ImConfig> CREATOR = new Creator<ImConfig>() {
        @Override
        public ImConfig createFromParcel(Parcel in) {
            return new ImConfig(in);
        }

        @Override
        public ImConfig[] newArray(int size) {
            return new ImConfig[size];
        }
    };

    public String getImToken() {
        return imToken;
    }

    public void setImToken(String imToken) {
        this.imToken = imToken;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getImUsername() {
        return imUsername;
    }

    public void setImUsername(String imUsername) {
        this.imUsername = imUsername;
    }

    public String getImPassword() {
        return imPassword;
    }

    public void setImPassword(String imPassword) {
        this.imPassword = imPassword;
    }

    public String getImUid() {
        return imUid;
    }

    public void setImUid(String imUid) {
        this.imUid = imUid;
    }

    public String getImGroupId() {
        return imGroupId;
    }

    public void setImGroupId(String imGroupId) {
        this.imGroupId = imGroupId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imToken);
        dest.writeInt(type);
        dest.writeString(imUsername);
        dest.writeString(imPassword);
        dest.writeString(imUid);
        dest.writeString(imGroupId);
    }
}

