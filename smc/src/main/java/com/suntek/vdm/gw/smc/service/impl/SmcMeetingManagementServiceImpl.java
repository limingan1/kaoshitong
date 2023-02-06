package com.suntek.vdm.gw.smc.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.request.GetSiteRegiesterStatusReq;
import com.suntek.vdm.gw.common.pojo.request.meeting.GetConditionsMeetingRequest;
import com.suntek.vdm.gw.common.pojo.response.GetConditionsMeetingResponse;
import com.suntek.vdm.gw.common.pojo.response.room.GetSiteRegiesterStatusResp;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.smc.adaptService.AdaptMeetingManagementService;
import com.suntek.vdm.gw.smc.pojo.McuInfo;
import com.suntek.vdm.gw.smc.pojo.McuParam;
import com.suntek.vdm.gw.common.pojo.request.ScheduleConfBrief;
import com.suntek.vdm.gw.smc.request.meeting.management.*;
import com.suntek.vdm.gw.smc.response.meeting.management.*;
import com.suntek.vdm.gw.smc.service.SmcMeetingManagementService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会议管理
 */
@Service
public class SmcMeetingManagementServiceImpl extends SmcBaseServiceImpl implements SmcMeetingManagementService {
    @Value("${useAdapt}")
    private Boolean useAdapt;

    @Autowired
    AdaptMeetingManagementService adaptMeetingManagementServic;

    @Autowired
    @Qualifier("smcHttpServiceImpl")
    private HttpService httpService;


    /**
     * 预约会议
     *
     * @param request
     * @param token
     * @return
     */
    @Override
    public ScheduleMeetingResponse schedule(ScheduleMeetingRequest request, String token) throws MyHttpException {
        String response = null;
        String subject = request.getConference().getSubject();
        if(subject.length() > 64){
            subject = subject.substring(0,64);
            request.getConference().setSubject(subject);
        }
        if(useAdapt){
            response = adaptMeetingManagementServic.schedule(request, token);
        }else {
            response = httpService.post("/conferences", request, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, ScheduleMeetingResponse.class);
    }

    /**
     * 修改会议
     *
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public ModifyMeetingResponse modify(String conferenceId, ModifyMeetingRequest request, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingManagementServic.modify(conferenceId, request, token);
        }else {
            response = httpService.put("/conferences/" + conferenceId, request, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, ModifyMeetingResponse.class);
    }

    /**
     * 查找单个会议
     *
     * @param conferenceId
     * @param token
     * @return
     */
    @Override
    public GetOneMeetingResponse getOne(String conferenceId, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingManagementServic.getOne(conferenceId, token);
        }else {
            response = httpService.get("/conferences/" + conferenceId, null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetOneMeetingResponse.class);
    }

    /**
     * 获取会议token
     *
     * @param conferenceId
     * @param token
     * @return
     */
    @Override
    public String getToken(String conferenceId, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingManagementServic.getToken(conferenceId, token);
        }else {
            response = httpService.get("/conferences/" + conferenceId + "/token", null, tokenHandle(token)).getBody();
        }
        return response;
    }

    /**
     * 查询会议 MCU 列表
     *
     * @param conferenceId
     * @param token
     * @return
     */
    @Override
    public List<McuInfo> getMcus(String conferenceId, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingManagementServic.getMcus(conferenceId, token);
        }else {
            response = httpService.get("/conferences/" + conferenceId + "/mcus", null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, new TypeReference<List<McuInfo>>() {});

    }

    /**
     * 查找最早预约的会议
     *
     * @param token
     * @return
     */
    @Override
    public GetTopMeetingResponse getTop(String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingManagementServic.getTop(token);
        }else {
            response = httpService.get("/conferences/top", null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetTopMeetingResponse.class);
    }

    /**
     * 按照会场数多少批量查找会议
     *
     * @param page
     * @param size
     * @param token
     * @return
     */
    @Override
    public List<ScheduleConfBrief> getImportant(int page, int size, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingManagementServic.getImportant(page, size, token);
        }else {
            response = httpService.get("/conferences/important?page=" + page + "&size=" + size, null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, new TypeReference<List<ScheduleConfBrief>>(){});
    }

    /**
     * 查找已预约会议
     *
     * @param page
     * @param size
     * @param request
     * @param token
     * @return
     */
    @Override
    public GetConditionsMeetingResponse getConditions(int page, int size, GetConditionsMeetingRequest request, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingManagementServic.getConditions(page, size, request, token);
        }else {
            response = httpService.post("/conferences/conditions?page=" + page + "&size=" + size, request, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetConditionsMeetingResponse.class);
    }

    /**
     * 查找会议个数
     *
     * @param token
     * @return
     */
    @Override
    public int getCount(GetCountMeetingRequest request, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingManagementServic.getCount(request, token);
        }else {
            response = httpService.post("/conferences/count", request, tokenHandle(token)).getBody();
        }
        return Integer.parseInt(response);
    }

    /**
     * 取消预约会议
     *
     * @param conferenceId
     * @param token
     * @return
     */
    @Override
    public void cancel(String conferenceId, String token) throws MyHttpException {
        if(useAdapt){
            adaptMeetingManagementServic.cancel(conferenceId, token);
        }else {
            httpService.delete("/conferences/" + conferenceId, null, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 会议转模板
     *
     * @param conferenceId
     * @param token
     * @return
     */
    @Override
    public MeetingToTemplateResponse toTemplate(String conferenceId, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingManagementServic.toTemplate(conferenceId, token);
        }else {
            response = httpService.post("/conferences/" + conferenceId + "/template", null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, MeetingToTemplateResponse.class);
    }

    /**
     * 发送邮件
     *
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public void sendMail(String conferenceId, SendMeetingMailRequest request, String token) throws MyHttpException {
        if(useAdapt){
            adaptMeetingManagementServic.sendMail(conferenceId, request, token);
        }else {
            httpService.post("/mail/conferences/" + conferenceId, request, tokenHandle(token)).getBody();
        }
        
    }

    /**
     * 获取时区列表
     *
     * @param token
     * @return
     */
    @Override
    public GetMeetingTimeZonesResponse getTimeZones(String lang, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingManagementServic.getTimeZones(lang, token);
        }else {
            response = httpService.get("/timezones?lang=" + lang, null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetMeetingTimeZonesResponse.class);
    }


    /**
     * 修改整个周期会议
     *
     * @param conferenceId
     * @param request
     * @param token
     * @return
     */
    @Override
    public ModifyPeriodMeetingResponse modifyPeriod(String conferenceId, ModifyPeriodMeetingRequest request, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingManagementServic.modifyPeriod(conferenceId, request, token);
        }else {
            response = httpService.put("/conferences/period/" + conferenceId, request, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, ModifyPeriodMeetingResponse.class);
    }

    /**
     * 查找周期会议的 ID 列表
     *
     * @param conferenceId
     * @param token
     * @return
     */
    @Override
    public GetPeriodMeetingIdsResponse getPeriodIds(String conferenceId, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingManagementServic.getPeriodIds(conferenceId, token);
        }else {
            response = httpService.get(" /conferences/period/" + conferenceId, null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetPeriodMeetingIdsResponse.class);
    }

    /**
     * 删除整个周期会议
     *
     * @param conferenceId
     * @param token
     * @return
     */
    @Override
    public void delPeriod(String conferenceId, String token) throws MyHttpException {
        if(useAdapt){
            adaptMeetingManagementServic.delPeriod(conferenceId, token);
        }else {
            httpService.delete("/conferences/period/" + conferenceId, null, tokenHandle(token)).getBody();
        }
        
    }

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
    @Override
    public GetMeetingParticipantsResponse getParticipants(String conferenceId, int page, int size, String name, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingManagementServic.getParticipants(conferenceId, page, size, name, token);
        }else {
            String url = "/conferences/" + conferenceId + "/participants?page=" + page + "&size=" + size;
            if (StringUtils.isNotBlank(name)) {
                url = url + "&name=" + name;
            }
            response = httpService.get(url, null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetMeetingParticipantsResponse.class);
    }

    /**
     * 批量查询会场日历
     *
     * @param request
     * @param token
     * @return
     */
    @Override
    public List<GetParticipantsCalendarListResponse> getParticipantsCalendar(GetParticipantsCalendarRequest request, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingManagementServic.getParticipantsCalendar(request, token);
        }else {
            response = httpService.post("/conferences/participants/calendar", request, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, new TypeReference<List<GetParticipantsCalendarListResponse>>() {});

    }

    /**
     * 查询多画面模式
     *
     * @param conferenceId
     * @param participantId
     * @param token
     * @return
     */
    @Override
    public McuParam getMultipicMode(String conferenceId, String participantId, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingManagementServic.getMultipicMode(conferenceId, participantId, token);
        }else {
            response = httpService.get("/conferences/" + conferenceId + "/participants/" + participantId + "/multipic", null, tokenHandle(token)).getBody();
        }
        return  JSON.parseObject(response,McuParam.class);
    }

    /**
     * 查询录播鉴权 TICKET
     *
     * @param request
     * @param token
     * @return
     */
    @Override
    public GetSsoTicketResponse getSsoTicket(GetSsoTicketRequest request, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingManagementServic.getSsoTicket(request, token);
        }else {
            response = httpService.post("/conferences/sso/ticket", request, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetSsoTicketResponse.class);
    }

    /**
     * 录播 TICKET 鉴权
     *
     * @param request
     * @param token
     * @return
     */
    @Override
    public SsoTicketAuthResponse ssoTicketAuth(SsoTicketAuthRequest request, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingManagementServic.ssoTicketAuth(request, token);
        }else {
            response = httpService.post("/conferences/sso/ticket/auth", request, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, SsoTicketAuthResponse.class);
    }

    /**
     * 查询录播地址
     *
     * @param conferenceId
     * @param guestPassword
     * @param token
     * @return
     */
    @Override
    public GetRecordAddressResponse getRecordAddress(String conferenceId, String guestPassword, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingManagementServic.getRecordAddress(conferenceId, guestPassword, token);
        }else {
            response = httpService.get("/conferences/record/" + conferenceId + guestPassword, null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetRecordAddressResponse.class);
    }

    /**
     * 外部接口查询录播地址
     *
     * @param conferenceId
     * @param token
     * @return
     */
    @Override
    public GetExternalRecordAddressResponse getExternalRecordAddress(String conferenceId, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingManagementServic.getExternalRecordAddress(conferenceId, token);
        }else {
            response = httpService.get("/conferences/external/recordAddress/" + conferenceId, null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetExternalRecordAddressResponse.class);
    }

    /**
     * Token 查询录播鉴权 Ticket
     *
     * @param request
     * @param token
     * @return
     */
    @Override
    public GetSsoTokenTicketResponse getSsoTokenTicket(GetSsoTokenTicketRequest request, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingManagementServic.getSsoTokenTicket(request, token);
        }else {
            response = httpService.post("/conferences/sso/token/ticket", request, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetSsoTokenTicketResponse.class);
    }

    @Override
    public List<GetSiteRegiesterStatusResp> getStieRegister(GetSiteRegiesterStatusReq request, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingManagementServic.getStieRegister(request, token);
        }else {
            response = httpService.post("/conferences/register/status/conditions", request, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, new TypeReference<List<GetSiteRegiesterStatusResp>>() {});
    }
}













