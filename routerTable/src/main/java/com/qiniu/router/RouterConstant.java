package com.qiniu.router;

public class RouterConstant {

    public static class Login {
        private static final String rootPath = "/login";
        public static final String LOGIN = rootPath + "/login";
        public static final String LOGIN_BY_VERIFICATION = rootPath + "/login_by_verification";
    }


    public static class AudioRoom {
        private static final String rootPath = "/AudioRoom";
        public static final String AudioRoomList = rootPath + "/AudioRoomListActivity";
    }

    public static class RtcRoom {
        private static final String rootPath = "/RtcRoom";
        public static final String VideoLiveListActivity = rootPath + "/VideoLiveListActivity";
    }

    public static class LivingPlayerRoom {
        private static final String rootPath = "/LivingPlayerRoom";
        public static final String LiveListActivity = rootPath + "/LivingListActivity";
    }

    public static class MeetingRoom {
        private static final String rootPath = "/MeetingRoom";
        public static final String MeettingGate = rootPath + "/MettingGate";
    }

    public static class App {
        private static final String rootPath = "/app";
        public static final String MainActivity = rootPath + "/MainActivity";
    }

    public static class Interview {
        private static final String rootPath = "/Interview";
        public static final String InterviewList = rootPath + "/InterviewList";
        public static final String InterviewRoom = rootPath + "/InterviewRoom";
        public static final String InterviewCreate = rootPath + "/InterviewCreate";
    }


    public static class Overhaul {
        private static final String rootPath = "/Overhaul";
        public static final String OverhaulList = rootPath + "/OverhaulList";
        public static final String OverhaulRoom = rootPath + "/OverhaulRoom";

    }

    public static class KTV {
        private static final String rootPath = "/KTV";
        public static final String KTVList = rootPath + "/KTVList";
        public static final String KTVRoom = rootPath + "/KTVRoom";

    }

    public static class Amusement {
        private static final String rootPath = "/Amusement";
        public static final String AmusementList = rootPath + "/AmusementList";
        public static final String AmusementRoom = rootPath + "/AmusementRoom";
    }

    public static class VoiceChatRoom {
        private static final String rootPath = "/voiceChatRoom";
        public static final String voiceChatRoomList = rootPath + "/voiceChatRoomList";
        public static final String voiceChatRoom = rootPath + "/voiceChatRoom";
    }

    public static class VideoRoom {
        private static final String rootPath = "/VideoRoom";
        public static final String VideoListHome = rootPath + "/VideoListHome";
        public static final String VideoRoomList = rootPath + "/VideoRoomList";
        public static final String VideoRoom = rootPath + "/VideoRoom";
        public static final String VideoPlayer = rootPath + "/VideoPlayer";
        public static final String VideoHome = rootPath + "/VideoHome";
    }

}
