package com.niucube.mutabletrackroom;
import com.niucube.absroom.seat.UserExtension;

public class UserExtensionWrap extends UserExtension {

    public int clientRoleType = 0;

    public UserExtensionWrap() {
    }

    public UserExtensionWrap(UserExtension extension, int clientType) {
        this.uid = extension.uid;
        this.clientRoleType = clientType;
        this.userExtRoleType = extension.userExtRoleType;
        this.userExtProfile = extension.userExtProfile;
        this.userExtensionMsg = extension.userExtensionMsg;
    }
}
