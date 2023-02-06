package com.suntek.vdm.gw.conf.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import com.suntek.vdm.gw.common.enums.SmcVersionType;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.ParticipantReq;
import com.suntek.vdm.gw.common.pojo.request.AddCasChannelReq;
import com.suntek.vdm.gw.common.pojo.response.AddCasChannelResp;
import com.suntek.vdm.gw.conf.api.request.ChildNodeInfos;
import com.suntek.vdm.gw.conf.pojo.ChildMeetingInfo;
import com.suntek.vdm.gw.smc.request.meeting.management.ModifyMeetingRequest;
import com.suntek.vdm.gw.smc.request.meeting.management.ModifyPeriodMeetingRequest;
import com.suntek.vdm.gw.smc.request.meeting.management.ScheduleMeetingRequest;
import com.suntek.vdm.gw.smc.request.meeting.management.SendMeetingMailRequest;

import java.util.List;

public interface MeetingService {
    void scheduleChildConference(ScheduleMeetingRequest scheduleMeetingRequest, int cascadeNum, String conferenceId, ChildNodeInfos childNodeInfos, String accessCode, String token);

    void addParticipantsByConferencesType(ScheduleMeetingRequest scheduleMeetingRequest, String conferenceId, List<ParticipantReq> participantReqs, String token) throws MyHttpException;

    void afterScheduleChildMeeting(String responseBody, String conferenceId, int cascadeNum,
                                   ScheduleMeetingRequest scheduleMeetingRequest,
                                   String accessCode, GwId childNodeGwId,
                                   String subject, String token);

    ParticipantReq addCasParticipantsHandle(int index, int cascadeNum, CascadeParticipantDirection cascadeParticipantDirection, SmcVersionType remoteSmcVersionType, String remoteNodeName, String remoteAccessCode, GwId remoteGwId, String accessCode, boolean isWelink,GwId localGwId) ;

    void modifyChildConference(ModifyMeetingRequest modifyMeetingRequest, ChildMeetingInfo childMeetingInfo);

    void cancelChildConference(ChildMeetingInfo childMeetingInfo);

    void sendMailChildConference(ChildMeetingInfo childMeetingInfo, SendMeetingMailRequest request);

    void modifyPeriodChildConference(ChildMeetingInfo childMeetingInfo, ModifyPeriodMeetingRequest request);

    void delPeriodChildConference(ChildMeetingInfo childMeetingInfo);

    void initCasConf();

    void resumeLowSubscribe(String nodeId);


    AddCasChannelResp addCasChannel(AddCasChannelReq addCasChannelReq, String token) throws MyHttpException;

    void CascadeParticipantStatusHandle(String confId, String pid, boolean main, Boolean mute, Boolean quiet, Integer videoSwitchAttribute);
}
