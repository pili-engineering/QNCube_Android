package com.qiniudemo.module.interview.been;

import com.qiniu.bzcomp.user.ImConfig;
import com.qiniu.bzcomp.user.UserInfo;
import com.niucube.comproom.RoomEntity;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

public class InterviewRoomModel implements Serializable, RoomEntity {

    private String roomToken;
    private UserInfo userInfo;
    private InterViewInfo interview;
    private List<RoomUser>allUserList;
    private String  publishUrl;
    public ImConfig imConfig;


    public  RoomUser getRoomUser(String uid){
        RoomUser roomUser = null;
        if(allUserList==null){return null;}

        for (int i=0;i<allUserList.size();i++){
            if(allUserList.get(i).accountId.equals(uid)){
                roomUser=allUserList.get(i);
            }
        }
        return roomUser;
    }

    public String getPublishUrl() {
        return publishUrl;
    }

    public void setPublishUrl(String publishUrl) {
        this.publishUrl = publishUrl;
    }

    public String getRoomToken() {
        return roomToken;
    }

    public void setRoomToken(String roomToken) {
        this.roomToken = roomToken;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public InterViewInfo getInterview() {
        return interview;
    }

    public void setInterview(InterViewInfo interview) {
        this.interview = interview;
    }

    public List<RoomUser> getAllUserList() {
        return allUserList;
    }

    public void setAllUserList(List<RoomUser> allUserList) {
        this.allUserList = allUserList;
    }

    public boolean isRoomOwner() {
       return interview.getRoleCode() == 1;
    }


    private boolean isJoined=false;
    @Override
    public boolean isJoined() {
        return isJoined;
    }

    @Override
    public void setJoined(boolean isJoined) {
       this.isJoined = isJoined;
    }

    public static class RoomUser{

        /**
         * accountId : p3bb4ihfyd57
         * nickname : 满一刀
         * avatar : https://demo-qnrtc-files.qnsdk.com/img_avater_8.png
         * phone : 13141616037
         * profile : MediaPaaS的第一选择~
         */

        private String accountId;
        private String nickname;
        private String avatar;
        private String phone;
        private String profile;

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

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getProfile() {
            return profile;
        }

        public void setProfile(String profile) {
            this.profile = profile;
        }
    }
    @NotNull
    @Override
    public String provideRoomId() {
        return interview.getId();
    }


    public String provideHostUid() {
        return interview.getInterviewerId();
    }

    @NotNull
    @Override
    public String provideImGroupId() {
        return imConfig.getImGroupId();
    }

    @NotNull
    @Override
    public String providePushUri() {
        return publishUrl;
    }

    @NotNull
    @Override
    public String providePullUri() {
        return "";
    }

    @NotNull
    @Override
    public String provideRoomToken() {
        return roomToken;
    }

}
