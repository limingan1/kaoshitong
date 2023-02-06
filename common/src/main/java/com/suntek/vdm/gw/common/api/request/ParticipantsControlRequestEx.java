package com.suntek.vdm.gw.common.api.request;

import com.suntek.vdm.gw.common.enums.MeetingControlType;
import com.suntek.vdm.gw.common.pojo.request.meeting.ParticipantsControlRequest;
import lombok.Data;

import java.util.Map;

@Data
public class ParticipantsControlRequestEx extends ParticipantsControlRequest {
    private Map<String, ParticipantPositionInfo> participantPositionInfo;
    private boolean autoCascadeChannel;
    /**
     * 观看类型
     */
    private MeetingControlType watchMeetingControlType;
}
