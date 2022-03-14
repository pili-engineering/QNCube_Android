package com.niucube.overhaul.mode;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.niucube.comproom.RoomEntity;
import com.qiniu.bzcomp.user.ImConfig;

import java.util.List;

public class OverhaulRoom implements Parcelable, RoomEntity {

    public String roomToken;
    public String publishUrl;
    public RoomInfo roomInfo;
    public List<UserList> allUserList;
    public String role;
    public Boolean isStudentJoinRtc = false;
    public ImConfig imConfig;


    protected OverhaulRoom(Parcel in) {
        roomToken = in.readString();
        publishUrl = in.readString();
        roomInfo = in.readParcelable(RoomInfo.class.getClassLoader());
        allUserList = in.createTypedArrayList(UserList.CREATOR);
        role = in.readString();
        byte tmpIsStudentJoinRtc = in.readByte();
        isStudentJoinRtc = tmpIsStudentJoinRtc == 0 ? null : tmpIsStudentJoinRtc == 1;
        imConfig = in.readParcelable(ImConfig.class.getClassLoader());
        isJoined = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(roomToken);
        dest.writeString(publishUrl);
        dest.writeParcelable(roomInfo, flags);
        dest.writeTypedList(allUserList);
        dest.writeString(role);
        dest.writeByte((byte) (isStudentJoinRtc == null ? 0 : isStudentJoinRtc ? 1 : 2));
        dest.writeParcelable(imConfig, flags);
        dest.writeByte((byte) (isJoined ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<OverhaulRoom> CREATOR = new Creator<OverhaulRoom>() {
        @Override
        public OverhaulRoom createFromParcel(Parcel in) {
            return new OverhaulRoom(in);
        }

        @Override
        public OverhaulRoom[] newArray(int size) {
            return new OverhaulRoom[size];
        }
    };

    @NonNull
    @Override
    public String provideRoomId() {
        return roomInfo.roomId;
    }


    @NonNull
    @Override
    public String provideImGroupId() {
        return imConfig.getImGroupId();
    }

    @NonNull
    @Override
    public String providePushUri() {
        return publishUrl;
    }

    @NonNull
    @Override
    public String providePullUri() {
        return publishUrl;
    }

    @NonNull
    @Override
    public String provideRoomToken() {
        return roomToken;
    }



    public static class RoomInfo implements Parcelable {
        public String roomId;
        public String title;
        public String image;
        public int status;

        protected RoomInfo(Parcel in) {
            roomId = in.readString();
            title = in.readString();
            image = in.readString();
            status = in.readInt();
        }

        public static final Creator<RoomInfo> CREATOR = new Creator<RoomInfo>() {
            @Override
            public RoomInfo createFromParcel(Parcel in) {
                return new RoomInfo(in);
            }

            @Override
            public RoomInfo[] newArray(int size) {
                return new RoomInfo[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(roomId);
            parcel.writeString(title);
            parcel.writeString(image);
            parcel.writeInt(status);
        }
    }

    public static class UserList implements Parcelable {
        public String accountId;
        public String nickname;
        public String avatar;
        public String phone;
        public String role;
        public String profile;

        protected UserList(Parcel in) {
            accountId = in.readString();
            nickname = in.readString();
            avatar = in.readString();
            phone = in.readString();
            role = in.readString();
            profile = in.readString();
        }

        public static final Creator<UserList> CREATOR = new Creator<UserList>() {
            @Override
            public UserList createFromParcel(Parcel in) {
                return new UserList(in);
            }

            @Override
            public UserList[] newArray(int size) {
                return new UserList[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(accountId);
            parcel.writeString(nickname);
            parcel.writeString(avatar);
            parcel.writeString(phone);
            parcel.writeString(role);
            parcel.writeString(profile);
        }
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
}
