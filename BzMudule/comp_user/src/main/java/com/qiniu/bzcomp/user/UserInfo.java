package com.qiniu.bzcomp.user;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class UserInfo implements Parcelable, Serializable {

    private String accountId;
    private String nickname;
    private String phone;
    private String avatar;
    private String profile;

    public UserInfo() {

    }

    protected UserInfo(Parcel in) {
        accountId = in.readString();
        nickname = in.readString();
        phone = in.readString();
        avatar = in.readString();
        profile = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(accountId);
        dest.writeString(nickname);
        dest.writeString(phone);
        dest.writeString(avatar);
        dest.writeString(profile);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
