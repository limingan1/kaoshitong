package com.suntek.vdm.gw.smc.adaptService.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huawei.vdmserver.common.dto.ErrorInfo;
import com.huawei.vdmserver.common.dto.ResponseEntityEx;
import com.huawei.vdmserver.common.dto.requestDto.ConfInfoReq;
import com.huawei.vdmserver.common.dto.requestDto.QueryScheduleConferenceReq;
import com.huawei.vdmserver.common.dto.requestDto.QueryVenuesStatusReq;
import com.huawei.vdmserver.common.vo.ConfVO;
import com.huawei.vdmserver.smc.core.service.SmcConferenceScheduledService;
import com.huawei.vdmserver.smc.core.service.SmcHistoryConfRecordAddrService;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.request.GetSiteRegiesterStatusReq;
import com.suntek.vdm.gw.common.pojo.request.ScheduleConfBrief;
import com.suntek.vdm.gw.common.pojo.request.meeting.GetConditionsMeetingRequest;
import com.suntek.vdm.gw.smc.adaptService.AdaptMeetingManagementService;
import com.suntek.vdm.gw.smc.adaptService.util.AdaptHttpStateUtil;
import com.suntek.vdm.gw.smc.pojo.McuInfo;
import com.suntek.vdm.gw.smc.pojo.McuParam;
import com.suntek.vdm.gw.smc.request.meeting.management.*;
import com.suntek.vdm.gw.smc.response.meeting.management.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AdaptMeetingManagementServiceImpl implements AdaptMeetingManagementService {
    @Autowired
    @Qualifier("SmcConferenceScheduledService2.0")
    SmcConferenceScheduledService smcConferenceScheduledService;

    @Autowired
    @Qualifier("SmcHistoryConfRecordAddrService2.0")
    SmcHistoryConfRecordAddrService smcHistoryConfRecordAddrService;

    @Override
    public String schedule(ScheduleMeetingRequest request, String token) throws MyHttpException {
        ConfVO confVO = new ConfVO();
        String jsonString = JSONObject.toJSONString(request);
        confVO.setData(jsonString);
        confVO.setToken(token);
        ResponseEntityEx<?> object = smcConferenceScheduledService.scheduleConf(confVO);
        return AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public String modify(String conferenceId, ModifyMeetingRequest request, String token) throws MyHttpException {
        ConfInfoReq confInfoReq = JSON.parseObject(JSON.toJSONString(request), ConfInfoReq.class);
        ConfVO confVO = new ConfVO();
        confVO.setData(confInfoReq);
        confVO.setToken(token);
        confVO.setConferenceId(conferenceId);
        ResponseEntityEx<?> object = smcConferenceScheduledService.editConf(confVO);
        return AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public String getOne(String conferenceId, String token) throws MyHttpException {
        ConfVO confVO = new ConfVO();
        confVO.setToken(token);
        confVO.setConferenceId(conferenceId);
        ResponseEntityEx<?> object = smcConferenceScheduledService.getOneMeeting(confVO);
        return AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public String getToken(String conferenceId, String token) throws MyHttpException {
        return token;
    }

    @Override
    public String getMcus(String conferenceId, String token) throws MyHttpException {
        return null;
    }

    @Override
    public String getTop(String token) throws MyHttpException {
        return null;
    }

    @Override
    public String getImportant(int page, int size, String token) throws MyHttpException {
        return null;
    }

    @Override
    public String getConditions(int page, int size, GetConditionsMeetingRequest request, String token) throws MyHttpException {
        ConfVO confVO = new ConfVO();
        confVO.setToken(token);
        confVO.setPage(page);
        confVO.setSize(size);
        QueryScheduleConferenceReq queryScheduleConferenceReq = null;
        if (request != null) {
            queryScheduleConferenceReq = JSON.parseObject(JSON.toJSONString(request), QueryScheduleConferenceReq.class);
        }else {
            queryScheduleConferenceReq = new QueryScheduleConferenceReq();
        }
        if ( !StringUtils.isEmpty(queryScheduleConferenceReq.getCasConfId())){
            confVO.setConfCasId(request.getCasConfId());
        }
        confVO.setData(queryScheduleConferenceReq);
        ResponseEntityEx<?> object = smcConferenceScheduledService.queryScheduleConference(confVO);
        return AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public String getCount(GetCountMeetingRequest request, String token) throws MyHttpException {
        return null;
    }

    @Override
    public void cancel(String conferenceId, String token) throws MyHttpException {
        ConfVO confVO = new ConfVO();
        confVO.setToken(token);
        confVO.setConferenceId(conferenceId);
        ResponseEntityEx<?> object = smcConferenceScheduledService.delPeriodicMeeting(confVO);
        AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public String toTemplate(String conferenceId, String token) throws MyHttpException {
        return null;
    }

    @Override
    public void sendMail(String conferenceId, SendMeetingMailRequest request, String token) throws MyHttpException {

    }

    @Override
    public String getTimeZones(String lang, String token) throws MyHttpException {
        return null;
    }

    @Override
    public String modifyPeriod(String conferenceId, ModifyPeriodMeetingRequest request, String token) throws MyHttpException {
        ConfInfoReq confInfoReq = JSON.parseObject(JSON.toJSONString(request), ConfInfoReq.class);
        ConfVO confVO = new ConfVO();
        confVO.setToken(token);
        confVO.setConferenceId(conferenceId);
        confVO.setData(confInfoReq);
        ResponseEntityEx<?> object = smcConferenceScheduledService.editRecurrenceConf(confVO);
        return AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public String getPeriodIds(String conferenceId, String token) throws MyHttpException {
        return null;
    }

    @Override
    public void delPeriod(String conferenceId, String token) throws MyHttpException {

    }

    @Override
    public String getParticipants(String conferenceId, int page, int size, String name, String token) throws MyHttpException {
        return null;
    }

    @Override
    public String getParticipantsCalendar(GetParticipantsCalendarRequest request, String token) throws MyHttpException {
        return null;
    }

    @Override
    public String getMultipicMode(String conferenceId, String participantId, String token) throws MyHttpException {
        return null;
    }

    @Override
    public String getSsoTicket(GetSsoTicketRequest request, String token) throws MyHttpException {
        return null;
    }

    @Override
    public String ssoTicketAuth(SsoTicketAuthRequest request, String token) throws MyHttpException {
        return null;
    }

    @Override
    public String getRecordAddress(String conferenceId, String guestPassword, String token) throws MyHttpException {
        Object object = smcHistoryConfRecordAddrService.queryHistoryConfRecordAddr(conferenceId, token);
        if (object instanceof ErrorInfo) {
            throw new MyHttpException(409, JSON.toJSONString(object));
        }
        return object.toString();
    }

    @Override
    public String getExternalRecordAddress(String conferenceId, String token) throws MyHttpException {
        return null;
    }

    @Override
    public String getSsoTokenTicket(GetSsoTokenTicketRequest request, String token) throws MyHttpException {
        return null;
    }

    @Override
    public String getStieRegister(GetSiteRegiesterStatusReq request, String token) throws MyHttpException {
        QueryVenuesStatusReq queryVenuesStatusReq = new QueryVenuesStatusReq();
        queryVenuesStatusReq.setUris(request.getUris());
        ConfVO confVO = new ConfVO();
        confVO.setToken(token);
        confVO.setData(queryVenuesStatusReq);
        ResponseEntityEx<?> object = smcConferenceScheduledService.queryVenuesStatus(confVO);
        return AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }
}
