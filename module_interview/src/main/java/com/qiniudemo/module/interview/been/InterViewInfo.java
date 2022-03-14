package com.qiniudemo.module.interview.been;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;


public class InterViewInfo implements Parcelable, Serializable {


    /**
     * id : r62xh5y817qku2ah
     * title : 秘籍的面试
     * goverment : 刚回来
     * career : logo
     * candidateId : 038ef95sam2u
     * candidateName : 秘籍
     * candidatePhone : 13141616034
     * interviewerName : 满一刀
     * interviewerPhone : 13141616037
     * interviewerId : p3bb4ihfyd57
     * startTime : 1619159400257
     * endTime : 1619161200257
     * status : 已结束
     * statusCode : -10
     * roleCode : 1
     * role : 面试官
     * isAuth : false
     * authCode : 4070
     * isRecorded : false
     */


    private String id;
    private String title;
    private String goverment;
    private String career;
    private String candidateId;
    private String candidateName;
    private String candidatePhone;
    private String interviewerName;
    private String interviewerPhone;
    private String interviewerId;
    private Long startTime;
    private Long endTime;
    private String status;
    private Integer statusCode;
    private Integer roleCode;
    private String role;
    private Boolean isAuth;
    private String authCode;
    private Boolean isRecorded;
    private List<Option> options;
    private ShareInfo shareInfo;
    /**
     * id : voluptate ipsum
     * title : commodo consequat officia do
     * government : officia veniam proident id
     * career : qui aliqua id magna
     * startTime : laboris proident
     * endTime : dolore et Ut sunt ad
     * status : incididunt non minim anim
     * statusCode : 8.027193681767595E7
     * roleCode : 7.182343567477152E7
     * role : culpa
     * enableJoinAuth : true
     * options : [null,null,{"type":"velit in","properties":{"type":"cupidatat in laborum Duis dolor","requestUrl":"commodo et mollit"},"required":["type","requestUrl"],"requestUrl":"proident minim tempor aliqua"}]
     */


    public InterViewInfo(){

    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public ShareInfo getShareInfo() {
        return shareInfo;
    }

    public void setShareInfo(ShareInfo shareInfo) {
        this.shareInfo = shareInfo;
    }

    protected InterViewInfo(Parcel in) {
        id = in.readString();
        title = in.readString();
        goverment = in.readString();
        career = in.readString();
        candidateId = in.readString();
        candidateName = in.readString();
        candidatePhone = in.readString();
        interviewerName = in.readString();
        interviewerPhone = in.readString();
        interviewerId = in.readString();
        if (in.readByte() == 0) {
            startTime = null;
        } else {
            startTime = in.readLong();
        }
        if (in.readByte() == 0) {
            endTime = null;
        } else {
            endTime = in.readLong();
        }
        status = in.readString();
        if (in.readByte() == 0) {
            statusCode = null;
        } else {
            statusCode = in.readInt();
        }
        if (in.readByte() == 0) {
            roleCode = null;
        } else {
            roleCode = in.readInt();
        }
        role = in.readString();
        byte tmpIsAuth = in.readByte();
        isAuth = tmpIsAuth == 0 ? null : tmpIsAuth == 1;
        authCode = in.readString();
        byte tmpIsRecorded = in.readByte();
        isRecorded = tmpIsRecorded == 0 ? null : tmpIsRecorded == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(goverment);
        dest.writeString(career);
        dest.writeString(candidateId);
        dest.writeString(candidateName);
        dest.writeString(candidatePhone);
        dest.writeString(interviewerName);
        dest.writeString(interviewerPhone);
        dest.writeString(interviewerId);
        if (startTime == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(startTime);
        }
        if (endTime == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(endTime);
        }
        dest.writeString(status);
        if (statusCode == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(statusCode);
        }
        if (roleCode == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(roleCode);
        }
        dest.writeString(role);
        dest.writeByte((byte) (isAuth == null ? 0 : isAuth ? 1 : 2));
        dest.writeString(authCode);
        dest.writeByte((byte) (isRecorded == null ? 0 : isRecorded ? 1 : 2));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<InterViewInfo> CREATOR = new Creator<InterViewInfo>() {
        @Override
        public InterViewInfo createFromParcel(Parcel in) {
            return new InterViewInfo(in);
        }

        @Override
        public InterViewInfo[] newArray(int size) {
            return new InterViewInfo[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGoverment() {
        return goverment;
    }

    public void setGoverment(String goverment) {
        this.goverment = goverment;
    }

    public String getCareer() {
        return career;
    }

    public void setCareer(String career) {
        this.career = career;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getCandidatePhone() {
        return candidatePhone;
    }

    public void setCandidatePhone(String candidatePhone) {
        this.candidatePhone = candidatePhone;
    }

    public String getInterviewerName() {
        return interviewerName;
    }

    public void setInterviewerName(String interviewerName) {
        this.interviewerName = interviewerName;
    }

    public String getInterviewerPhone() {
        return interviewerPhone;
    }

    public void setInterviewerPhone(String interviewerPhone) {
        this.interviewerPhone = interviewerPhone;
    }

    public String getInterviewerId() {
        return interviewerId;
    }

    public void setInterviewerId(String interviewerId) {
        this.interviewerId = interviewerId;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Integer getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(Integer roleCode) {
        this.roleCode = roleCode;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getAuth() {
        return isAuth;
    }

    public void setAuth(Boolean auth) {
        isAuth = auth;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public Boolean getRecorded() {
        return isRecorded;
    }

    public void setRecorded(Boolean recorded) {
        isRecorded = recorded;
    }

    public static class Option implements Parcelable,Serializable {
        public static final String type_share="200";
        public static final String type_cancel="50";
        public static final String type_end="51";
        private String type;
        private String requestUrl;
        private String title;
        private String method;


        public Option(){

        }

        protected Option(Parcel in) {
            type = in.readString();
            requestUrl = in.readString();
            title = in.readString();
            method = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(type);
            dest.writeString(requestUrl);
            dest.writeString(title);
            dest.writeString(method);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Option> CREATOR = new Creator<Option>() {
            @Override
            public Option createFromParcel(Parcel in) {
                return new Option(in);
            }

            @Override
            public Option[] newArray(int size) {
                return new Option[size];
            }
        };

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }



        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getRequestUrl() {
            return requestUrl;
        }

        public void setRequestUrl(String requestUrl) {
            this.requestUrl = requestUrl;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }




    public static class ShareInfo implements Parcelable,Serializable{
        private String url;
        private String icon;
        private String content;

        protected ShareInfo(Parcel in) {
            url = in.readString();
            icon = in.readString();
            content = in.readString();
        }

        public static final Creator<ShareInfo> CREATOR = new Creator<ShareInfo>() {
            @Override
            public ShareInfo createFromParcel(Parcel in) {
                return new ShareInfo(in);
            }

            @Override
            public ShareInfo[] newArray(int size) {
                return new ShareInfo[size];
            }
        };

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        /**
         * Describe the kinds of special objects contained in this Parcelable
         * instance's marshaled representation. For example, if the object will
         * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
         * the return value of this method must include the
         * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
         *
         * @return a bitmask indicating the set of special object types marshaled
         * by this Parcelable object instance.
         */
        @Override
        public int describeContents() {
            return 0;
        }

        /**
         * Flatten this object in to a Parcel.
         *
         * @param dest  The Parcel in which the object should be written.
         * @param flags Additional flags about how the object should be written.
         *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
         */
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(url);
            dest.writeString(icon);
            dest.writeString(content);
        }
    }
}
