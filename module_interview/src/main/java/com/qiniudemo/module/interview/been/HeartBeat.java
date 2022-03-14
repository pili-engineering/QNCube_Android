package com.qiniudemo.module.interview.been;

import java.io.Serializable;

public class HeartBeat implements Serializable {

    private String interviewId;
    //下一次心跳
    private String interval;
    private Op options;

    public Op getOptions() {
        return options;
    }

    public void setOptions(Op options) {
        this.options = options;
    }

    public String getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(String interviewId) {
        this.interviewId = interviewId;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }


  public static   class Op{
        //显示挂断
        private boolean showLeaveInterview;

      public boolean isShowLeaveInterview() {
          return showLeaveInterview;
      }

      public void setShowLeaveInterview(boolean showLeaveInterview) {
          this.showLeaveInterview = showLeaveInterview;
      }
  }
}
