package com.niucube.rtminvitation;

public class InvitationMsg {

    private String invitationName;
    private Invitation invitation;

    public InvitationMsg(){

    }

    public String getInvitationName() {
        return invitationName;
    }

    public void setInvitationName(String invitationName) {
        this.invitationName = invitationName;
    }

    public Invitation getInvitation() {
        return invitation;
    }

    public void setInvitation(Invitation invitation) {
        this.invitation = invitation;
    }
}
