package com.suntek.vdm.gw.conf.service;

import com.alibaba.fastjson.JSONObject;
import com.suntek.vdm.gw.conf.pojo.MeetingInfo;
import com.suntek.vdm.gw.conf.pojo.ParticipantInfo;
import com.suntek.vdm.gw.common.pojo.MultiPicInfo;
import com.suntek.vdm.gw.common.pojo.ParticipantDetail;

import java.util.List;

public interface VideoSourceService {
    void videoSourceHandleByRemote(String conferenceId, MultiPicInfo changeMultiPicInfo, String changeParticipantId, boolean isCasChannel);

    void videoSourceHandleByParticipantSelect(String conferenceId, List<ParticipantDetail> participantDetails);

    void videoSourceHandleBySubscribeNotify(ParticipantInfo participantInfo, MultiPicInfo newMultiPicInfo);

    void changeSource(JSONObject outputObj);

    String changeSource(String message);

    void casChannelFreeHandelBySubscribeNotify(String conferenceId, ParticipantInfo participantInfo, MultiPicInfo multiPicInfo);

    void checkOtherParticipantsHasViewThisChannel(MeetingInfo meetingInfo, String cascadeChannelPid, String oldParticipantId);
}
