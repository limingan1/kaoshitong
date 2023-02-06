package com.suntek.vdm.gw.smc.service;

import com.suntek.vdm.gw.common.api.request.MeetingControlRequest;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.GetParticipantsDetailInfoResponse;
import com.suntek.vdm.gw.common.pojo.request.GetParticipantsRequest;
import com.suntek.vdm.gw.common.pojo.request.meeting.DurationMeetingRequest;
import com.suntek.vdm.gw.common.pojo.request.meeting.ParticipantsControlRequest;
import com.suntek.vdm.gw.common.pojo.response.meeting.GetMeetingDetailResponse;
import com.suntek.vdm.gw.common.pojo.response.meeting.GetParticipantsResponse;
import com.suntek.vdm.gw.smc.pojo.AttendeeReq;
import com.suntek.vdm.gw.smc.pojo.CallInfoRsp;
import com.suntek.vdm.gw.common.pojo.ParticipantReq;
import com.suntek.vdm.gw.smc.pojo.VideoSrcInfo;
import com.suntek.vdm.gw.smc.request.meeting.control.*;
import com.suntek.vdm.gw.smc.response.meeting.control.*;

import java.util.List;

public interface SmcMeetingControlService{
    /**
     *  获取会议详情
     * @param conferenceId
     * @param token
     * @return
     */
    public GetMeetingDetailResponse getMeetingDetail(String conferenceId, String token, String isQueryMultiPicInfo)  throws MyHttpException;

    /**
     * 设置单会场字幕
     * @param conferenceId
     * @param participantId
     * @param request
     * @param token
     * @return
     */
    public void setTextTips(String conferenceId, String participantId, SetTextTipsRequest request, String token) throws MyHttpException;

    /**
     * 会议控制
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public void meetingControl(String conferenceId, MeetingControlRequest request, String token) throws MyHttpException;

    /**
     *  除部分会场外全体静音
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public void chatMic(String conferenceId, ChatMicRequest request, String token) throws MyHttpException;

    /**
     * 除部分会场外全体关闭扬声器
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public void chatSpeaker(String conferenceId, ChatSpeakerRequest request, String token) throws MyHttpException;

    /**
     * 设置会议横幅字幕
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public void setTextTips(String conferenceId, SetTextTipsRequest request, String token) throws MyHttpException;

    /**
     * 延长会议
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public void duration(String conferenceId, DurationMeetingRequest request, String token) throws MyHttpException;

    /**
     * 结束会议
     * @param conferenceId
     * @param token
     * @return
     */
    public void delMeeting(String conferenceId, String token) throws MyHttpException;

    /**
     * 添加会场
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public void addParticipants(String conferenceId, List<ParticipantReq>  request, String token) throws MyHttpException;

    /**
     * 添加与会人
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public void addAttendees(String conferenceId, List<AttendeeReq> request, String token) throws MyHttpException;

    /**
     * 获取会场列表
     * @param conferenceId
     * @param page
     * @param size
     * @param request
     * @param token
     * @return
     */
    public GetParticipantsResponse getParticipants(String conferenceId, int page, int size, GetParticipantsRequest request, String token) throws MyHttpException;

    /**
     * 批量获取会场列表
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public List<GetParticipantsResponse> getParticipantsBriefs(String conferenceId, List<String> request, String token) throws MyHttpException;
    /**
     * 查询会场详情
     * @param conferenceId
     * @param participantId
     * @param token
     * @return
     */
    public GetParticipantsDetailInfoResponse getParticipantsDetailInfo(String conferenceId, String participantId, String token) throws MyHttpException;

    /**
     * 查询会场视音频能力
     * @param conferenceId
     * @param participantId
     * @param token
     * @return
     */

    public GetParticipantsCapabilityResponse getParticipantsCapability(String conferenceId, String participantId, String token) throws MyHttpException;

    /**
     * 会场控制
     * @param conferenceId
     * @param participantId
     * @param request
     * @param token
     * @return
     */

    public String participantsControl(String conferenceId, String participantId, ParticipantsControlRequest request, String token) throws MyHttpException;

    /**
     * 批量会场控制
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public void participantsControl(String conferenceId, List<ParticipantsControlRequest> request, String token) throws MyHttpException;

    /**
     *  设置会场视频源跟随
     * @param conferenceId
     * @param participantId
     * @param request
     * @param token
     * @return
     */
    public void participantsFellow(String conferenceId, String participantId, ParticipantsFellowRequest request, String token) throws MyHttpException;

    /**
     * 摄像机控制
     * @param conferenceId
     * @param participantId
     * @param request
     * @param token
     * @return
     */
    public void cameraControl(String conferenceId, String participantId, CameraControlRequest request, String token) throws MyHttpException;

    /**
     * 设置常用会场
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public void setCommonlyUsedParticipants(String conferenceId, SetCommonlyUsedParticipantsRequest request, String token) throws MyHttpException;

    /**
     * 查询常用会场列表
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public GetCommonlyUsedParticipantsResponse getCommonlyUsedParticipants(String conferenceId, GetCommonlyUsedParticipantsRequest request, String token) throws MyHttpException;

    /**
     *  删除会场
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public void delParticipants(String conferenceId, List<String> request, String token) throws MyHttpException;

    /**
     * 订阅会场状态
     * @param conferenceId
     * @param groupId
     * @param request
     * @param token
     * @return
     */
    public void subscribeParticipantsStatus(String conferenceId, String groupId,SubscribeParticipantsStatusRequest request, String token) throws MyHttpException;

    /**
     * 取消订阅会场状态
     * @param conferenceId
     * @param groupId
     * @param token
     * @return
     */
    public void unSubscribeParticipantsStatus(String conferenceId, String groupId,String token) throws MyHttpException;

    /**
     * 订阅会场实时状态
     * @param conferenceId
     * @param groupId
     * @param request
     * @param token
     * @return
     */
    public void subscribeParticipantsStatusRealTime(String conferenceId, String groupId, List<String> request, String token) throws MyHttpException;

    /**
     *  取消订阅会场实时状态
     * @param conferenceId
     * @param groupId
     * @param token
     * @return
     */
    public void unSubscribeParticipantsStatusRealTime(String conferenceId, String groupId, String token) throws MyHttpException;

    /**
     * 会场点名前提示
     * @param conferenceId
     * @param participantId
     * @param token
     * @return
     */
    public void setRemind(String conferenceId, String participantId, String token) throws MyHttpException;

    /**
     * 设置主席轮询
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public void setChairmanPoll(String conferenceId, SetChairmanPollRequest request, String token) throws MyHttpException;

    /**
     * 获取主席轮询设置
     * @param conferenceId
     * @param token
     * @return
     */
    public GetChairmanPollResponse getChairmanPoll(String conferenceId,String token) throws MyHttpException;

    /**
     * 设置定时广播
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public void setBroadcastPoll(String conferenceId, SetBroadcastPollRequest request, String token) throws MyHttpException;

    /**
     * 获取定时广播设置
     * @param conferenceId
     * @param token
     * @return
     */
    public GetBroadcastPollResponse getBroadcastPoll(String conferenceId, String token) throws MyHttpException;

    /**
     * 设置多画面轮询
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public void setMultiPicPoll(String conferenceId, SetMultiPicPollRequest request, String token) throws MyHttpException;

    /**
     * 获取多画面轮询设置
     * @param conferenceId
     * @param token
     * @return
     */
    public GetMultiPicPollResponse getMultiPicPoll(String conferenceId, String token) throws MyHttpException;

    /**
     * 提供会场设置二次拨号信息功能。
     * @param conferenceId
     * @param participantId
     * @param request
     * @param token
     * @return
     */
    public SetParticipantsParameterResponse setParticipantsParameter(String conferenceId,String participantId,SetParticipantsParameterRequest request, String token) throws MyHttpException;

    /**
     * 会场迁移
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public void migrate(String conferenceId, MigrateRequest request, String token) throws MyHttpException;

    /**
     * 查询会场视频源
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public List<VideoSrcInfo> getVideoSource(String conferenceId, List<String> request, String token) throws MyHttpException;

    /**
     * 推流控制
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public void rseStream(String conferenceId, RseStreamRequest request, String token) throws MyHttpException;

    /**
     * 批量设置会场字幕
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public void batchTextTips(String conferenceId, BatchTextTipsRequest request, String token) throws MyHttpException;

    /**
     * 更新订阅会场状态
     * @param conferenceId
     * @param groupId
     * @param request
     * @param token
     * @return
     */

    public void updateSubscribeParticipantsStatus(String conferenceId, String groupId, UpdateSubscribeParticipantsStatusRequest request, String token) throws MyHttpException;

    /**
     * 推送 AI 字幕
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */

    public void pushAiCaption(String conferenceId,String request, String token) throws MyHttpException;

    /**
     * 获取预置多画面
     * @param conferenceId
     * @param token
     * @return
     */
    public GetPresetParamResponse getPresetParam(String conferenceId, String token) throws MyHttpException;

    void quickHangup(String uri, String smcToken) throws MyHttpException;

    CallInfoRsp callInfo(String uri, String smcToken) throws MyHttpException;;

    void changeSiteName(String conferenceId, ParticipantUpdateDto participantUpdateDto, String smcToken) throws MyHttpException;
}
