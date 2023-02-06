package com.suntek.vdm.gw.smc.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.request.GetSiteRegiesterStatusReq;
import com.suntek.vdm.gw.common.pojo.request.meeting.GetConditionsMeetingRequest;
import com.suntek.vdm.gw.common.pojo.response.GetConditionsMeetingResponse;
import com.suntek.vdm.gw.common.pojo.response.room.GetSiteRegiesterStatusResp;
import com.suntek.vdm.gw.smc.pojo.McuInfo;
import com.suntek.vdm.gw.smc.pojo.McuParam;
import com.suntek.vdm.gw.common.pojo.request.ScheduleConfBrief;
import com.suntek.vdm.gw.smc.request.meeting.management.*;
import com.suntek.vdm.gw.smc.response.meeting.management.*;

import java.util.List;

public interface SmcMeetingManagementService {


    public ScheduleMeetingResponse schedule(ScheduleMeetingRequest request, String token) throws MyHttpException;

    /**
     * 修改会议
     *
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public ModifyMeetingResponse modify(String conferenceId, ModifyMeetingRequest request, String token) throws MyHttpException;

    /**
     * 查找单个会议
     *
     * @param conferenceId
     * @param token
     * @return
     */
    public GetOneMeetingResponse getOne(String conferenceId, String token) throws MyHttpException;

    /**
     * 获取会议token
     *
     * @param conferenceId
     * @param token
     * @return
     */
    public String getToken(String conferenceId, String token) throws MyHttpException;

    /**
     * 查询会议 MCU 列表
     *
     * @param conferenceId
     * @param token
     * @return
     */
    public List<McuInfo> getMcus(String conferenceId, String token) throws MyHttpException;

    /**
     * 查找最早预约的会议
     *
     * @param token
     * @return
     */
    public GetTopMeetingResponse getTop(String token) throws MyHttpException;

    /**
     * 按照会场数多少批量查找会议
     *
     * @param page
     * @param size
     * @param token
     * @return
     */
    public List<ScheduleConfBrief> getImportant(int page, int size, String token) throws MyHttpException;

    /**
     * 查找已预约会议
     *
     * @param page
     * @param size
     * @param request
     * @param token
     * @return
     */
    public GetConditionsMeetingResponse getConditions(int page, int size, GetConditionsMeetingRequest request, String token) throws MyHttpException;

    /**
     * 查找会议个数
     *
     * @param token
     * @return
     */
    public int getCount(GetCountMeetingRequest request, String token) throws MyHttpException;

    /**
     * 取消预约会议
     *
     * @param conferenceId
     * @param token
     * @return
     */
    public void cancel(String conferenceId, String token) throws MyHttpException;

    /**
     * 会议转模板
     *
     * @param conferenceId
     * @param token
     * @return
     */
    public MeetingToTemplateResponse toTemplate(String conferenceId, String token) throws MyHttpException;

    /**
     * 发送邮件
     *
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public void sendMail(String conferenceId, SendMeetingMailRequest request, String token) throws MyHttpException;

    /**
     * 获取时区列表
     *
     * @param token
     * @return
     */
    public GetMeetingTimeZonesResponse getTimeZones(String lang, String token) throws MyHttpException;


    /**
     * 修改整个周期会议
     *
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    public ModifyPeriodMeetingResponse modifyPeriod(String conferenceId, ModifyPeriodMeetingRequest request, String token) throws MyHttpException;

    /**
     * 查找周期会议的 ID 列表
     *
     * @param conferenceId
     * @param token
     * @return
     */
    public GetPeriodMeetingIdsResponse getPeriodIds(String conferenceId, String token) throws MyHttpException;

    /**
     * 删除整个周期会议
     *
     * @param conferenceId
     * @param token
     * @return
     */
    public void delPeriod(String conferenceId, String token) throws MyHttpException;

    /**
     * 根据 id 查找会场  name可以为空
     *
     * @param conferenceId
     * @param page
     * @param size
     * @param name
     * @param token
     * @return
     */
    public GetMeetingParticipantsResponse getParticipants(String conferenceId, int page, int size, String name, String token) throws MyHttpException;


    /**
     * 批量查询会场日历
     *
     * @param request
     * @param token
     * @return
     */
    public List<GetParticipantsCalendarListResponse> getParticipantsCalendar(GetParticipantsCalendarRequest request, String token) throws MyHttpException;


    /**
     * 查询多画面模式
     *
     * @param conferenceId
     * @param participantId
     * @param token
     * @return
     */
    public McuParam getMultipicMode(String conferenceId, String participantId, String token) throws MyHttpException;

    ;


    /**
     * 查询录播鉴权 TICKET
     *
     * @param request
     * @param token
     * @return
     */
    public GetSsoTicketResponse getSsoTicket(GetSsoTicketRequest request, String token) throws MyHttpException;

    /**
     * 录播 TICKET 鉴权
     *
     * @param request
     * @param token
     * @return
     */
    public SsoTicketAuthResponse ssoTicketAuth(SsoTicketAuthRequest request, String token) throws MyHttpException;

    /**
     * 查询录播地址
     *
     * @param conferenceId
     * @param guestPassword
     * @param token
     * @return
     */
    public GetRecordAddressResponse getRecordAddress(String conferenceId, String guestPassword, String token) throws MyHttpException;

    /**
     * 外部接口查询录播地址
     *
     * @param conferenceId
     * @param token
     * @return
     */
    public GetExternalRecordAddressResponse getExternalRecordAddress(String conferenceId, String token) throws MyHttpException;

    /**
     * Token 查询录播鉴权 Ticket
     *
     * @param request
     * @param token
     * @return
     */
    public GetSsoTokenTicketResponse getSsoTokenTicket(GetSsoTokenTicketRequest request, String token) throws MyHttpException;

    List<GetSiteRegiesterStatusResp> getStieRegister(GetSiteRegiesterStatusReq request, String smcToken) throws MyHttpException;
}
