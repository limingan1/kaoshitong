package com.suntek.vdm.gw.welink.service;

import com.suntek.vdm.gw.common.enums.MeetingControlType;
import com.suntek.vdm.gw.common.pojo.AllocateCasChannelInfo;
import com.suntek.vdm.gw.common.pojo.CasChannelParameter;
import com.suntek.vdm.gw.common.pojo.CascadeChannelFreeInfo;
import com.suntek.vdm.gw.common.pojo.CascadeChannelInfo;

public interface WelinkCasChannelManageService {
    AllocateCasChannelInfo allocateCasChannel(String conferenceId, CasChannelParameter watchCasChannel, MeetingControlType meetingControlType, String watchParticipantId);

    AllocateCasChannelInfo allocateMainCasChannel(String conferenceId);

    CascadeChannelInfo getCascadeChannelInfoByIndex(String conferenceId, int index);

    void freeCascadeChannel(CascadeChannelFreeInfo cascadeChannelFreeInfo);
}
