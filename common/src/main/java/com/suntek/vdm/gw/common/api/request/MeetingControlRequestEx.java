package com.suntek.vdm.gw.common.api.request;


import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import com.suntek.vdm.gw.common.enums.SmcVersionType;
import com.suntek.vdm.gw.common.pojo.CasConfInfo;
import com.suntek.vdm.gw.common.pojo.MeetingControlMeetingInfo;
import com.suntek.vdm.gw.common.util.CommonHelper;
import lombok.Data;

@Data
public class MeetingControlRequestEx extends MeetingControlRequest {
    /**
     * 级联会议ID
     */
    private String confCasId;

    /**
     * 是否welink会议
     */
    private Boolean isWeLink;

    /**
     * 级联会议信息
     */
    private CasConfInfo casConfInfo;


    private CascadeParticipantDirection from;

    /**
     * SMC版本
     */
    private SmcVersionType smcVersionType;


    private MeetingControlMeetingInfo remoteMeetingInfo;


    public boolean remoteV2() {
        if (remoteMeetingInfo != null && remoteMeetingInfo.getSmcVersionType().equals(SmcVersionType.V2)) {
            return true;
        }
        return false;
    }

    public MeetingControlRequestEx purify() {
        MeetingControlRequestEx meetingControlRequestEx = new MeetingControlRequestEx();
        meetingControlRequestEx.setFrom(from);
        meetingControlRequestEx.setRemoteMeetingInfo(remoteMeetingInfo);
        return meetingControlRequestEx;
    }

    public MeetingControlRequest toMeetingControlRequest() {
        return CommonHelper.copyBean(this, MeetingControlRequest.class);
    }


    public boolean fromUp() {
        return (this.from != null && this.from.equals(CascadeParticipantDirection.UP));
    }

}
