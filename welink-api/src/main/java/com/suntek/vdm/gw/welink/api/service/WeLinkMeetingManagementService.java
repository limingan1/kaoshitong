package com.suntek.vdm.gw.welink.api.service;


import com.alibaba.fastjson.JSONObject;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.welink.api.request.GetMeetingDetailRequest;
import com.suntek.vdm.gw.welink.api.request.GetMeetingListRequest;
import com.suntek.vdm.gw.welink.api.request.ScheduleMeetingRequest;
import com.suntek.vdm.gw.welink.api.response.GetMeetingDetailResponse;
import com.suntek.vdm.gw.welink.api.response.GetMeetingListResponse;
import com.suntek.vdm.gw.welink.api.response.QueryUserResultDTO;
import com.suntek.vdm.gw.welink.api.response.ScheduleMeetingResponse;

public interface WeLinkMeetingManagementService {

    /**
     * 预约会议
     *
     * @param request
     * @param token
     * @return
     * @throws MyHttpException
     */
     ScheduleMeetingResponse scheduleMeeting(ScheduleMeetingRequest request, String token) throws MyHttpException;


    /**
     * 获取会议详情
     * @param request
     * @param token
     * @return
     * @throws MyHttpException
     */
     GetMeetingDetailResponse getMeetingDetail(GetMeetingDetailRequest request, String token) throws MyHttpException;


    /**
     * 获取会议列表
     * @param request
     * @param token
     * @return
     * @throws MyHttpException
     */
     GetMeetingListResponse getMeetingList(GetMeetingListRequest request, String token) throws MyHttpException;

    JSONObject getMeetingControlToken(String conferenceID, String password, String loginType,String ip) throws MyHttpException;

    JSONObject getConference(String token,String conferenceId,String ip) throws MyHttpException;

    JSONObject getWebSocketTemporaryToken(String conferenceID, String meetingControlToken, String wsUrl)throws MyHttpException;

    void participantsControl(String url, String body, String token) throws MyHttpException;

    QueryUserResultDTO getUserDcsMenber(String token) throws MyHttpException;

    void deleteMeeting(String conferenceId, String token) throws MyHttpException;
}
