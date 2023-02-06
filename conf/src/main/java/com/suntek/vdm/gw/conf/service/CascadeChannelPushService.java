package com.suntek.vdm.gw.conf.service;

import com.suntek.vdm.gw.common.enums.CascadeChannelNotifyType;
import com.suntek.vdm.gw.conf.pojo.ParticipantInfo;

public interface CascadeChannelPushService {
    void pushToRemote(String conferenceId, String changeCasParticipantId, String changeCasParticipantIdProxy, CascadeChannelNotifyType cascadeChannelNotifyType);

    void freeToRemote(String conferenceId, ParticipantInfo participantInfo);
}
