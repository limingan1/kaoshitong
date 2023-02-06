package com.suntek.vdm.gw.welink.service;

import com.alibaba.fastjson.JSONObject;
import com.suntek.vdm.gw.common.api.request.DurationMeetingRequestEx;
import com.suntek.vdm.gw.common.api.request.MeetingControlRequestEx;
import com.suntek.vdm.gw.common.api.request.ParticipantsControlRequestEx;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.CascadeChannelNotifyInfo;
import com.suntek.vdm.gw.common.pojo.GetParticipantsDetailInfoResponse;
import com.suntek.vdm.gw.common.pojo.request.AddCasChannelReq;
import com.suntek.vdm.gw.common.pojo.request.meeting.GetConditionsMeetingRequest;
import com.suntek.vdm.gw.common.pojo.response.AddCasChannelResp;
import com.suntek.vdm.gw.common.pojo.response.CasConferenceInfosResponse;
import com.suntek.vdm.gw.common.pojo.response.GetConditionsMeetingResponse;
import com.suntek.vdm.gw.common.pojo.response.meeting.GetMeetingDetailResponse;
import com.suntek.vdm.gw.common.pojo.response.meeting.GetParticipantsResponse;

import java.util.List;

public interface WelinkMeetingService {
    public JSONObject ScheduleConf(String request, String token) throws MyHttpException;

    //得到welink增加级联后缀标识的结果,0101234-->0101234001，第一条大于64
    String getWelinkUriByNumber(String smcAccess, Integer num, Integer cascadeNum);

    GetMeetingDetailResponse getWelinkMeetingDetail(String conferenceId, String token) throws MyHttpException;

    GetConditionsMeetingResponse getWelinkMeetingConditions(GetConditionsMeetingRequest getConditionsMeetingRequest, String query, String token) throws MyHttpException;

    GetParticipantsResponse getWelinkParticipants(String conferenceId);

    GetParticipantsDetailInfoResponse getParticipantsDetailInfo(String conferenceId, String participantId);

    void participantsControl(List<JSONObject> participantsControlRequestExs, String conferenceId) throws MyHttpException;

    void addParticipants(List<JSONObject> participantsList, String conferenceId,String token,String query) throws MyHttpException;

    void delAttendees(List<String> participantsList, String conferenceId) throws MyHttpException;

    void meetingControl(String conferenceId, MeetingControlRequestEx requestEx) throws MyHttpException;

    void durationMeeting(String conferenceId, DurationMeetingRequestEx requestEx) throws MyHttpException;

    void stopMeeting(String conferenceId) throws MyHttpException;

    void broadcastParticipants(String conferenceId, String participantId) throws MyHttpException;

    JSONObject participantControl(String conferenceId, String participantId, ParticipantsControlRequestEx requestEx) throws MyHttpException;

    void reNameMainChannel(CascadeChannelNotifyInfo cascadeChannelNotifyInfo) throws MyHttpException;

    AddCasChannelResp addCasChannel(AddCasChannelReq addCasChannelReq, String token) throws MyHttpException;

    JSONObject getOne(String conferenceId, String token);

    void deleteMeeting(String conferenceId, String token) throws MyHttpException;

    CasConferenceInfosResponse getCasConferenceInfos(String conferenceId);
}
