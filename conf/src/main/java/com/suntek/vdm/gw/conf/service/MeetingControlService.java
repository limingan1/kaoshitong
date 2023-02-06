package com.suntek.vdm.gw.conf.service;

import com.suntek.vdm.gw.common.api.request.DurationMeetingRequestEx;
import com.suntek.vdm.gw.common.api.request.MeetingControlRequest;
import com.suntek.vdm.gw.common.api.request.MeetingControlRequestEx;
import com.suntek.vdm.gw.common.api.request.ParticipantsControlRequestEx;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.GetParticipantsDetailInfoResponse;
import com.suntek.vdm.gw.common.pojo.MergeConferenceReq;
import com.suntek.vdm.gw.common.pojo.ParticipantReq;
import com.suntek.vdm.gw.common.pojo.request.GetParticipantsRequest;
import com.suntek.vdm.gw.common.pojo.response.meeting.GetMeetingDetailResponse;
import com.suntek.vdm.gw.common.pojo.response.meeting.GetParticipantsResponse;
import com.suntek.vdm.gw.conf.api.request.*;
import com.suntek.vdm.gw.conf.pojo.WatchInfo;
import com.suntek.vdm.gw.core.customexception.BaseStateException;
import com.suntek.vdm.gw.common.enums.MeetingControlType;
import com.suntek.vdm.gw.smc.pojo.*;
import com.suntek.vdm.gw.smc.request.meeting.control.*;
import com.suntek.vdm.gw.smc.response.meeting.control.*;

import java.util.List;
import java.util.Set;

public interface MeetingControlService {
    void delMeeting(String conferenceId, String token, String confCasId, Boolean keepByCasState) throws MyHttpException;

    void meetingControl(MeetingControlRequestEx meetingControlRequestEx, String conferenceId, String token) throws MyHttpException;

    void meetingControlTop(MeetingControlRequest meetingControlRequest, String confCasId, String lowConferenceId, String token) throws MyHttpException;

    void meetingControlDirect(MeetingControlRequestEx request, String conferenceId, String token) throws MyHttpException;

    WatchInfo participantsControl(ParticipantsControlRequestEx participantsControlRequestEx, String conferenceId, String participantId, String token) throws MyHttpException;

    void participantsControl(List<ParticipantsControlRequestEx> participantsControlRequestExs, String conferenceId, String token) throws MyHttpException;

    void setTextTips(SetTextTipsRequestEx setTextTipsRequestEx, String conferenceId, String participantId, String token) throws MyHttpException;

    void setTextTips(SetTextTipsRequestEx setTextTipsRequestEx, String conferenceId, String token) throws MyHttpException;

    void duration(DurationMeetingRequestEx durationMeetingRequestEx, String conferenceId, String token) throws MyHttpException;

    void addParticipants(List<ParticipantReq> participantReqs, String conferenceId, String token, String confCasId,boolean createSign) throws MyHttpException;

    void mergeConference(String confCasId,String conferenceId, MergeConferenceReq req, String token) throws MyHttpException;

    void delParticipants(List<String> delParticipantIds, String conferenceId, String token, String confCasId) throws MyHttpException;

    GetMeetingDetailResponse getMeetingDetail(String conferenceId, String token, String confCasId, String isQueryMultiPicInfo) throws MyHttpException, BaseStateException;

    GetParticipantsResponse getParticipants(GetParticipantsRequest getParticipantsRequest, String conferenceId, String token, String confCasId, Integer page, Integer size, String towall) throws MyHttpException;

    void cameraControl(CameraControlRequest cameraControlRequest, String conferenceId, String participantId, String token, String confCasId) throws MyHttpException;

    void participantsFellow(ParticipantsFellowRequest participantsFellowRequest, String conferenceId, String participantId, String token, String confCasId) throws MyHttpException;

    public void chatMic(String conferenceId, ChatMicRequest chatMicRequest, String token) throws MyHttpException;

    public void chatSpeaker(String conferenceId, ChatSpeakerRequest chatSpeakerRequest, String token) throws MyHttpException;

    public void addAttendees(String conferenceId, List<AttendeeReq> addAttendeesRequest, String token, String confCasId) throws MyHttpException;

    public List<GetParticipantsResponse> getParticipantsBriefs(String conferenceId, List<String> request, String token) throws MyHttpException;

    public GetParticipantsDetailInfoResponse getParticipantsDetailInfo(String conferenceId, String participantId, String token) throws MyHttpException;

    public GetParticipantsCapabilityResponse getParticipantsCapability(String conferenceId, String participantId, String token) throws MyHttpException;

    public void setCommonlyUsedParticipants(String conferenceId, SetCommonlyUsedParticipantsRequest request, String token) throws MyHttpException;

    public GetCommonlyUsedParticipantsResponse getCommonlyUsedParticipants(String conferenceId, GetCommonlyUsedParticipantsRequest request, String token) throws MyHttpException;

    public void subscribeParticipantsStatus(String conferenceId, String groupId, SubscribeParticipantsStatusRequest request, String token) throws MyHttpException;

    public void unSubscribeParticipantsStatus(String conferenceId, String groupId, String token) throws MyHttpException;

    public void subscribeParticipantsStatusRealTime(String conferenceId, String groupId, List<String> request, String token) throws MyHttpException;

    public void unSubscribeParticipantsStatusRealTime(String conferenceId, String groupId, String token) throws MyHttpException;

    public void setRemind(String conferenceId, String participantId, String token) throws MyHttpException;

    public void setChairmanPoll(String conferenceId, SetChairmanPollRequest request, String token) throws MyHttpException;

    public GetChairmanPollResponse getChairmanPoll(String conferenceId, String token) throws MyHttpException;

    public void setBroadcastPoll(String conferenceId, SetBroadcastPollRequest request, String token) throws MyHttpException;

    public GetBroadcastPollResponse getBroadcastPoll(String conferenceId, String token) throws MyHttpException;

    public void setMultiPicPoll(String conferenceId, SetMultiPicPollRequest request, String token) throws MyHttpException;

    public GetMultiPicPollResponse getMultiPicPoll(String conferenceId, String token) throws MyHttpException;

    public SetParticipantsParameterResponse setParticipantsParameter(String conferenceId, String participantId, SetParticipantsParameterRequest request, String token) throws MyHttpException;

    public void migrate(String conferenceId, MigrateRequest request, String token) throws MyHttpException;

    public List<VideoSrcInfo> getVideoSource(String conferenceId, List<String> request, String token) throws MyHttpException;

    public void rseStream(String conferenceId, RseStreamRequest request, String token) throws MyHttpException;

    public void batchTextTips(String conferenceId, BatchTextTipsRequest request, String token) throws MyHttpException;

    public void updateSubscribeParticipantsStatus(String conferenceId, String groupId, UpdateSubscribeParticipantsStatusRequest request, String token) throws MyHttpException;

    public void pushAiCaption(String conferenceId, String request, String token) throws MyHttpException;

    public GetPresetParamResponse getPresetParam(String conferenceId, String token) throws MyHttpException;

    void quickHangup(String uri, String token) throws MyHttpException;

    CallInfoRsp callInfo(String uri, String token) throws MyHttpException;

    public WatchInfo pullSource(String conferenceId, String participantId, PullSourceRequest request, String token) throws MyHttpException, BaseStateException;

    void sendChild(String conferenceId, MeetingControlRequest request) throws MyHttpException;


    void sendChild(String conferenceId, MeetingControlRequest request, Set<String> excludeConferenceIdSet) throws MyHttpException;


    void sendTop(String conferenceId, MeetingControlRequest request) throws MyHttpException;


    void childCasChannelControl(String conferenceId, String pid, MeetingControlType meetingControlType);

    void changeSiteName(String conferenceId, ParticipantUpdateDto participantUpdateDto, String token) throws MyHttpException;
}
