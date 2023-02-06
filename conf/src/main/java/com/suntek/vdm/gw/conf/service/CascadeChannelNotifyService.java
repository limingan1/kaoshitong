package com.suntek.vdm.gw.conf.service;

import com.suntek.vdm.gw.common.pojo.CascadeChannelFreeInfo;
import com.suntek.vdm.gw.common.pojo.CascadeChannelNotifyInfo;

public interface CascadeChannelNotifyService {
    void notifyHandle(CascadeChannelNotifyInfo cascadeChannelNotifyInfo);
    void notifyFree(CascadeChannelFreeInfo cascadeChannelFreeInfo);
    void targetParticipantOffline(String conferenceId,String remoteConferenceId,String changeParticipantId, boolean isLocalCasChannel);
}


