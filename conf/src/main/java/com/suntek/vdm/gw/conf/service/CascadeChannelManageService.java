package com.suntek.vdm.gw.conf.service;

import com.suntek.vdm.gw.common.pojo.AllocateCasChannelInfo;
import com.suntek.vdm.gw.common.pojo.CascadeChannelInfo;
import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import com.suntek.vdm.gw.common.enums.MeetingControlType;

public interface CascadeChannelManageService {
    CascadeChannelInfo getCascadeChannelMain(String id, CascadeParticipantDirection direction, String childConfId);

    CascadeChannelInfo getCascadeChannelOne(String id, CascadeParticipantDirection direction, String childConfId, Integer index);

    AllocateCasChannelInfo allocateCasChannel(String conferenceId, CascadeParticipantDirection direction, String childConferenceId, MeetingControlType meetingControlType, String watchParticipantId);
}
