package com.qiniudemo.module.interview.been;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;



public class InterViewDetails implements Parcelable {


    /**
     * id : iw2xbukzllln8l70
     * title : 噶的面试
     * goverment : '不
     * career : 把
     * candidateId : vwac60d85sbo
     * candidateName : 噶
     * candidatePhone : 13141616035
     * interviewerName : 满一刀
     * interviewerPhone : 13141616037
     * interviewerId : p3bb4ihfyd57
     * startTime : 1619164800477
     * endTime : 1619166600477
     * status : 待面试
     * statusCode : 0
     * roleCode : 1
     * role : 面试官
     * isAuth : false
     * authCode : 8390
     * isRecorded : false
     * options : [{"type":100,"title":"进入面试","requestUrl":"niucube://interview/joinInterview?interviewId=iw2xbukzllln8l70","method":""},{"type":1,"title":"修改面试","requestUrl":"niucube://interview/updateInterview?interviewId=iw2xbukzllln8l70","method":""},{"type":50,"title":"取消面试","requestUrl":"http://100.100.68.126:5080/v1/cancelInterview/iw2xbukzllln8l70","method":"POST"},{"type":200,"title":"分享面试","requestUrl":"","method":""}]
     * shareInfo : {"url":"www.qiniu.com","icon":"https://dn-odum9helk.qbox.me/Fur2V8BraQp3ZU48itnkmqAw0XNm","content":"分享就点击这个 https://www.qiniu.com"}
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


    /**
     * status : Ut in ut laborum ut
     * interviewerPhone : est ipsum Ut nisi
     * endTime : commodo pariatur
     * title : magna cillum ipsum elit aliqua
     * enableJoinAuth : true
     * statusCode : -2.2857151904876977E7
     * government : est ad non
     * interviewerName : irure culpa commodo
     * candidateName : sit
     * career : do est ullamco in minim
     * candidatePhone : sed
     * id : aliqua
     * startTime : dolore aliqua dolore mollit ea
     * authCode : pariatur elit id consectetur
     */
    
    public InterViewDetails() {
    }


    protected InterViewDetails(Parcel in) {
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

    public static final Creator<InterViewDetails> CREATOR = new Creator<InterViewDetails>() {
        @Override
        public InterViewDetails createFromParcel(Parcel in) {
            return new InterViewDetails(in);
        }

        @Override
        public InterViewDetails[] newArray(int size) {
            return new InterViewDetails[size];
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
}
