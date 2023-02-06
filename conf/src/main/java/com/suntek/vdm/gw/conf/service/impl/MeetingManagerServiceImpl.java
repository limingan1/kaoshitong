package com.suntek.vdm.gw.conf.service.impl;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.enums.SmcVersionType;
import com.suntek.vdm.gw.common.pojo.*;
import com.suntek.vdm.gw.common.pojo.request.GetSiteRegiesterStatusReq;
import com.suntek.vdm.gw.common.pojo.request.ScheduleConfBrief;
import com.suntek.vdm.gw.common.pojo.request.meeting.GetConditionsMeetingRequest;
import com.suntek.vdm.gw.common.pojo.response.GetConditionsMeetingResponse;
import com.suntek.vdm.gw.common.pojo.response.room.GetSiteRegiesterStatusResp;
import com.suntek.vdm.gw.common.util.TransactionManage;
import com.suntek.vdm.gw.common.util.SystemConfiguration;
import com.suntek.vdm.gw.common.util.UtcTimeUtil;
import com.suntek.vdm.gw.conf.api.request.ChildNodeInfos;
import com.suntek.vdm.gw.conf.api.request.ScheduleMeetingRequestEx;
import com.suntek.vdm.gw.conf.api.response.ScheduleMeetingResponseEx;
import com.suntek.vdm.gw.common.enums.ConfApiUrl;
import com.suntek.vdm.gw.conf.pojo.ChildMeetingInfo;
import com.suntek.vdm.gw.conf.pojo.MeetingInfo;
import com.suntek.vdm.gw.conf.pojo.ParticipantInfo;
import com.suntek.vdm.gw.conf.service.MeetingInfoManagerService;
import com.suntek.vdm.gw.conf.service.MeetingManagerService;
import com.suntek.vdm.gw.conf.service.MeetingService;
import com.suntek.vdm.gw.conf.service.ParticipantInfoManagerService;
import com.suntek.vdm.gw.core.cache.CommonCache;
import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import com.suntek.vdm.gw.common.pojo.CascadeParticipantParameter;
import com.suntek.vdm.gw.core.service.RemoteGwService;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.common.util.CommonHelper;
import com.suntek.vdm.gw.smc.pojo.*;
import com.suntek.vdm.gw.smc.request.meeting.management.*;
import com.suntek.vdm.gw.smc.response.meeting.management.*;
import com.suntek.vdm.gw.smc.service.SmcMeetingControlService;
import com.suntek.vdm.gw.smc.service.SmcMeetingManagementService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MeetingManagerServiceImpl extends BaseServiceImpl implements MeetingManagerService {
    @Autowired
    private MeetingInfoManagerService meetingInfoManagerService;
    @Autowired
    private SmcMeetingManagementService smcMeetingManagementService;
    @Autowired
    private SmcMeetingControlService smcMeetingControlService;
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private MeetingService meetingService;
    @Autowired
    private RemoteGwService remoteGwService;
    @Autowired
    private TempServiceImpl tempService;
    @Autowired
    private ParticipantInfoManagerService participantInfoManagerService;


    @Override
    public ScheduleMeetingResponseEx scheduleConferences(ScheduleMeetingRequestEx request, String token) throws MyHttpException {
        if (request.getCascadeNum() == 0) {
            request.setCascadeNum(1);
        }
//        request.setCascadeNum(4);
        String name = request.getSubject();
        if (SmcVersionType.V2.equals(request.getSmcVersionType()) && SmcVersionType.V2.equals(SystemConfiguration.getSmcVersion())) {
            request.getParticipants().add(getParticipant(request));
        }
        ConferenceReq requestConference = request.getConference();
        if(requestConference.getDuration() == 2147483647 && "EDIT_CONFERENCE".equals(requestConference.getConferenceTimeType())){
            requestConference.setConferenceTimeType("INSTANT_CONFERENCE");
            requestConference.setScheduleStartTime(UtcTimeUtil.getUTCTimeStr());
        }
        String tempStartTime = null;
        boolean updateTimeSign = false;
        if (SystemConfiguration.smcVersionIsV2() && request.instantConferenceFlag()) {
            tempStartTime = requestConference.getScheduleStartTime();
            requestConference.setScheduleStartTime("1970-01-01 00:00:00 UTC");
            updateTimeSign = true;
        }
        String nodeName = nodeDataService.getLocal().getName();
        if(request.getVmNodeName() != null){
            nodeName = request.getVmNodeName();
        }
        requestConference.setSubject(nodeName + request.getSubject());
        ScheduleMeetingRequest param = CommonHelper.copyBean(request, ScheduleMeetingRequest.class);
        transferAttendeesToParticipants(param);
        ScheduleMeetingResponse scheduleMeetingResponse = smcMeetingManagementService.schedule(param, getSmcToken(token));
        requestConference.setSubject(name);
        requestConference.setScheduleStartTime(updateTimeSign ? tempStartTime : requestConference.getScheduleStartTime());//不修改召集子会议的请求
        return scheduleConferences(request, scheduleMeetingResponse, token);
    }
//    @Override
    public void transferAttendeesToParticipants(ScheduleMeetingRequest param) {
        if (SystemConfiguration.smcVersionIsV2()) {
            List<AttendeeReq> attendees = param.getAttendees();
            if (attendees != null && !attendees.isEmpty()) {
                List<ParticipantReq> participants = param.getParticipants();
                if (participants == null || participants.isEmpty()) {
                    participants = new ArrayList<>();
                    param.setParticipants(participants);
                }
                participants.addAll(attendees.stream().map(AttendeeReq::toParticipantReq).collect(Collectors.toList()));
                log.info("transferAttendeesToParticipants，participants：{}", participants);
            }
        }
    }
    private ParticipantReq getParticipant(ScheduleMeetingRequestEx request){
        ParticipantReq participantReq = new ParticipantReq();
        participantReq.setEncodeType("ENCODE_DECODE");
        participantReq.setDialMode("OUT");
        participantReq.setName(request.getNodeName());
        participantReq.setUri(request.getAccessCode() + "@" + request.getCascadeNum());//上级的主叫号码是本级的被叫
        participantReq.setIpProtocolType(0);
        participantReq.setParticipantType("UpperLevelParticipant");
        return participantReq;

    }
    @Override
    public void transferScheduleMeetingData(ConferenceRsp resConference, ConferenceRsp conference) {
        String conferenceTimeType = resConference.getConferenceTimeType();
        if (conferenceTimeType != null) {
            switch (conferenceTimeType) {
                case "0":
                    conference.setConferenceTimeType("INSTANT_CONFERENCE");
                    break;
                case "1":
                    conference.setConferenceTimeType("EDIT_CONFERENCE");
                    break;
            }
        }
    }
    @Override
    public ScheduleMeetingResponseEx scheduleConferences(ScheduleMeetingRequestEx request, ScheduleMeetingResponse response, String token) throws MyHttpException {
        ScheduleMeetingResponseEx scheduleMeetingResponseEx = new ScheduleMeetingResponseEx();
        BeanUtils.copyProperties(response, scheduleMeetingResponseEx);
        if(request.getVmNodeName() != null){
            scheduleMeetingResponseEx.setVmNodeName(request.getVmNodeName());
        }
        transferScheduleMeetingData(response.getConference(), scheduleMeetingResponseEx.getConference());//转换apdater返回格式不一致的数据
        String conferenceId = response.getConferenceId();
        String accessCode = response.getAccessCode();
        //本级创建成功 添加到会议对象
            Boolean startFlag;
            if (SystemConfiguration.smcVersionIsV2()) {
                startFlag = true;
            } else {
                startFlag = false;
            }
            meetingInfoManagerService.create(conferenceId, accessCode, request.getSubject(), startFlag, new ConferenceState());

        //来自上级 需要添加级联通道
        if (!StringUtils.isEmpty(request.getAccessCode())) {
            if (!request.editConferenceFlag() && !SystemConfiguration.smcVersionIsV2()) {
                //等待会议开始通知
                TransactionManage.wait(new TransactionId(TransactionType.CONFERENCES_ONLINE, conferenceId), 1000 * 10);
            }
            if (!SmcVersionType.V2.equals(request.getSmcVersionType()) || !SmcVersionType.V2.equals(SystemConfiguration.getSmcVersion())) {
                int cascadeNum = request.getCascadeNum();
                List<ParticipantReq> participantReqs = new ArrayList<>();
                for (int index = 0; index < cascadeNum; index++) {
                    GwId targetGwId = request.getTargetGwId();
                    if (targetGwId == null) {
                        targetGwId = nodeDataService.getLocal().toGwId();
                    }
                    participantReqs.add(meetingService.addCasParticipantsHandle(index, cascadeNum, CascadeParticipantDirection.UP, request.getSmcVersionType(), request.getNodeName(), request.getAccessCode(), request.getGwId(), accessCode, false, targetGwId));
                    if (request.getSmcVersionType().equals(SmcVersionType.V2) && SystemConfiguration.getSmcVersion().equals(SmcVersionType.V2)) {
                        break;
                    }
                }
                //添加会场
                try{
                    meetingService.addParticipantsByConferencesType(request, conferenceId, participantReqs, token);
                }catch (MyHttpException e){
                    smcMeetingControlService.delMeeting(conferenceId, getSmcToken(token));
                    meetingInfoManagerService.del(conferenceId);
                    throw e;
                }

            }
//            if (!request.editConferenceFlag()) {
//                if (SystemConfiguration.smcVersionIsV2() && request.getSmcVersionType().equals(SmcVersionType.V3)) {
//                    for (ParticipantReq item : participantReqs) {
//                        CascadeParticipantParameter cascadeParticipantParameter = CascadeParticipantParameter.valueOf(item.getVdcMarkCascadeParticipant());
//
//                        if (cascadeParticipantParameter.getIndex()==0){
//                            String uri = item.getUri() + "*" + cascadeParticipantParameter.getGwId().getAreaCode() + "*"  + cascadeParticipantParameter.getDirection().getValue();
//                            tempService.callCascadeChannelAdd(conferenceId, uri);
//                        }else{
//                            String uri = item.getUri() + "*" + cascadeParticipantParameter.getGwId().getAreaCode() + "*" + cascadeParticipantParameter.getIndex() + "*" + cascadeParticipantParameter.getDirection().getValue();
//                            tempService.callCascadeChannelAdd(conferenceId, uri);
//                        }
//
//
//                    }
//                }
//            }
            //回填节点名称
            scheduleMeetingResponseEx.setNodeName(nodeDataService.getLocal().getName());
            //回复本级SMC版本
            scheduleMeetingResponseEx.setSmcVersionType(SystemConfiguration.getSmcVersion());
            //回复本级的接入号
            scheduleMeetingResponseEx.setAccessCode(accessCode);
        }

        Boolean formGw = !StringUtils.isEmpty(request.getAccessCode());
        List<ChildNodeInfos> child = request.getChild();
        if (child != null) {
            //回填节点名称
            request.setNodeName(nodeDataService.getLocal().getName());
            request.setAccessCode(accessCode);
            //回复本级SMC版本
            request.setSmcVersionType(SystemConfiguration.getSmcVersion());
            //多线程召集子会议
            for (ChildNodeInfos item : child) {
                meetingService.scheduleChildConference(request, request.getCascadeNum(), scheduleMeetingResponseEx.getConferenceId(), item, accessCode, token);
            }
        }
        if (formGw && !request.editConferenceFlag()) {
            if (SystemConfiguration.smcVersionIsV3()) {
                //等待会场添加成功后才返回
                //最多等待10秒
                for (int i = 0; i < 20; i++) {
                    Map<String, ParticipantInfo> upCasParticipant = participantInfoManagerService.getLocalCasParticipant(conferenceId, CascadeParticipantDirection.UP);
                    if (upCasParticipant.size() == request.getCascadeNum()) {
                        break;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return scheduleMeetingResponseEx;
    }

    @Override
    public GetConditionsMeetingResponse getConditions(GetConditionsMeetingRequest
                                                              getConditionsMeetingRequest, String token, Integer page, Integer size) throws MyHttpException {
        //TODO 12
        GetConditionsMeetingResponse response = smcMeetingManagementService.getConditions(page, size, getConditionsMeetingRequest, getSmcToken(token));
        return response;
    }

    @Override
    public ModifyMeetingResponse modify(String conferenceId, ModifyMeetingRequest request, String token) throws
            MyHttpException {
        ModifyMeetingResponse response = smcMeetingManagementService.modify(conferenceId, request, getSmcToken(token));
        return response;
    }

    @Override
    public GetOneMeetingResponse getOne(String conferenceId, String token, String confCasId) throws MyHttpException {
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        if (meetingInfo != null) {
            String mainConfCasId = meetingInfo.getConfCasId();
            if (confCasId != null && !confCasId.equals(mainConfCasId)) {
                ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByCasConfId(conferenceId, confCasId);
                if(childMeetingInfo != null){
                    String uri = String.format(ConfApiUrl.GET_ONE_CONFERENCE.value(), childMeetingInfo.getId(), confCasId);
                    String queryResp = remoteGwService.toByGwId(childMeetingInfo.getGwId()).get(uri, null).getBody();
                    return JSON.parseObject(queryResp, GetOneMeetingResponse.class);
                }
            }
        }
        GetOneMeetingResponse response = smcMeetingManagementService.getOne(conferenceId, getSmcToken(token));
        return response;
    }


    @Override
    public List<McuInfo> getMcus(String conferenceId, String token) throws MyHttpException {
        //TODO 需要讨论
        return smcMeetingManagementService.getMcus(conferenceId, getSmcToken(token));
    }

    @Override
    public List<ScheduleConfBrief> getImportant(int page, int size, String token) throws MyHttpException {
        return smcMeetingManagementService.getImportant(page, size, getSmcToken(token));
    }

    @Override
    public int getCount(GetCountMeetingRequest request, String token) throws MyHttpException {
        return smcMeetingManagementService.getCount(request, getSmcToken(token));
    }

    @Override
    public void cancel(String conferenceId, String token) throws MyHttpException {
        GetOneMeetingResponse getOneMeetingResponse = smcMeetingManagementService.getOne(conferenceId, getSmcToken(token));
        List<ParticipantRsp> participantRsps = getOneMeetingResponse.getParticipants();
        if (participantRsps != null) {
            for (ParticipantRsp item : participantRsps) {
                if (!StringUtils.isEmpty(item.getVdcMarkCascadeParticipant())) {
                    CascadeParticipantParameter cascadeParticipantParameter = CascadeParticipantParameter.valueOf(item.getVdcMarkCascadeParticipant());
                    if (cascadeParticipantParameter.getDirection().equals(CascadeParticipantDirection.DOWN) && cascadeParticipantParameter.isMain()) {
                        try {
                            GwId gwId = cascadeParticipantParameter.getGwId();
                            GetConditionsMeetingRequest body = new GetConditionsMeetingRequest();
                            //根据接入号（级联会议接入号）查询会议
                            body.setCasConfId(item.getUri());
                            try {
                                String json = remoteGwService.toByGwId(gwId).post(String.format(ConfApiUrl.CONFERENCES_CONDITIONS.value(), 0, 10), body).getBody();
                                GetConditionsMeetingResponse getConditionsMeetingResponse = JSON.parseObject(json, GetConditionsMeetingResponse.class);
                                if (getConditionsMeetingResponse.getContent() != null && getConditionsMeetingResponse.getContent().size() > 0) {
                                    ScheduleConfBrief childConfInfo = getConditionsMeetingResponse.getContent().get(0);
                                    remoteGwService.toByGwId(gwId).delete(ConfApiUrl.CONFERENCES.value() + "/" + childConfInfo.getId(), null);
                                }
                            } catch (MyHttpException e) {
                                log.error("exception: {}", body);
                            }
                        } catch (Exception e) {
                            log.error("exception", e);
                        }
                    }
                }
            }
        }
        //后结束本级会议
        smcMeetingManagementService.cancel(conferenceId, getSmcToken(token));
//        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
//        if (meetingInfo.hasChildMeeting()) {
//            for (ChildMeetingInfo item : meetingInfo.getChildMeetingInfos())
//                meetingService.cancelChildConference(item);
//        }
    }

    @Override
    public MeetingToTemplateResponse toTemplate(String conferenceId, String token) throws MyHttpException {
        //TODO 会议模板
        return smcMeetingManagementService.toTemplate(conferenceId, getSmcToken(token));
    }

    @Override
    public void sendMail(String conferenceId, SendMeetingMailRequest request, String token) throws MyHttpException {
        smcMeetingManagementService.sendMail(conferenceId, request, getSmcToken(token));
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        if (meetingInfo.hasChild()) {
            for (ChildMeetingInfo item : meetingInfo.getChildMeetingInfoMap().values())
                meetingService.sendMailChildConference(item, request);
        }
    }

    @Override
    public GetMeetingTimeZonesResponse getTimeZones(String lang, String token) throws MyHttpException {
        return smcMeetingManagementService.getTimeZones(lang, getSmcToken(token));
    }

    @Override
    public ModifyPeriodMeetingResponse modifyPeriod(String conferenceId, ModifyPeriodMeetingRequest request, String
            token) throws MyHttpException {
        ModifyPeriodMeetingResponse response = smcMeetingManagementService.modifyPeriod(conferenceId, request, getSmcToken(token));
        return response;
    }

    @Override
    public GetPeriodMeetingIdsResponse getPeriodIds(String conferenceId, String token) throws MyHttpException {
        //TODO  需要讨论
        return smcMeetingManagementService.getPeriodIds(conferenceId, getSmcToken(token));
    }

    @Override
    public void delPeriod(String conferenceId, String token) throws MyHttpException {
        GetOneMeetingResponse getOneMeetingResponse = smcMeetingManagementService.getOne(conferenceId, getSmcToken(token));
        List<ParticipantRsp> participantRsps = getOneMeetingResponse.getParticipants();
        if (participantRsps != null) {
            for (ParticipantRsp item : participantRsps) {
                if (!org.springframework.util.StringUtils.isEmpty(item.getVdcMarkCascadeParticipant())) {
                    CascadeParticipantParameter cascadeParticipantParameter = CascadeParticipantParameter.valueOf(item.getVdcMarkCascadeParticipant());
                    if (cascadeParticipantParameter.getDirection().equals(CascadeParticipantDirection.DOWN) && cascadeParticipantParameter.isMain()) {
                        try {
                            GwId gwId = cascadeParticipantParameter.getGwId();
                            GetConditionsMeetingRequest body = new GetConditionsMeetingRequest();
                            //根据接入号（级联会议接入号）查询会议
                            body.setKeyword(item.getUri());
                            try {
                                String json = remoteGwService.toByGwId(gwId).post(String.format(ConfApiUrl.CONFERENCES_CONDITIONS.value(), 0, 10), body).getBody();
                                GetConditionsMeetingResponse getConditionsMeetingResponse = JSON.parseObject(json, GetConditionsMeetingResponse.class);
                                if (getConditionsMeetingResponse.getContent() != null && getConditionsMeetingResponse.getContent().size() > 0) {
                                    ScheduleConfBrief childConfInfo = getConditionsMeetingResponse.getContent().get(0);
                                    remoteGwService.toByGwId(gwId).delete(String.format(ConfApiUrl.CONFERENCES_PERIOD.value(), childConfInfo.getId()), null);
                                }
                            } catch (MyHttpException e) {

                            }
                        } catch (Exception e) {
                            log.error("exception", e);
                        }
                    }
                }
            }
        }
        //后结束本级会议
        smcMeetingManagementService.delPeriod(conferenceId, getSmcToken(token));
    }

    @Override
    public GetMeetingParticipantsResponse getParticipants(String conferenceId, int page, int size, String
            name, String token) throws MyHttpException {
        //TODo
        return smcMeetingManagementService.getParticipants(conferenceId, page, size, name, getSmcToken(token));
    }

    @Override
    public List<GetParticipantsCalendarListResponse> getParticipantsCalendar(GetParticipantsCalendarRequest
                                                                                     request, String token) throws MyHttpException {
        return smcMeetingManagementService.getParticipantsCalendar(request, getSmcToken(token));
    }

    @Override
    public McuParam getMultipicMode(String conferenceId, String participantId, String token) throws MyHttpException {
        //TODo
        return smcMeetingManagementService.getMultipicMode(conferenceId, participantId, getSmcToken(token));
    }

    @Override
    public GetSsoTicketResponse getSsoTicket(GetSsoTicketRequest request, String token) throws MyHttpException {
        return smcMeetingManagementService.getSsoTicket(request, getSmcToken(token));
    }

    @Override
    public SsoTicketAuthResponse ssoTicketAuth(SsoTicketAuthRequest request, String token) throws MyHttpException {
        return smcMeetingManagementService.ssoTicketAuth(request, getSmcToken(token));
    }

    @Override
    public GetRecordAddressResponse getRecordAddress(String conferenceId, String guestPassword, String token) throws
            MyHttpException {
        //TODo
        return smcMeetingManagementService.getRecordAddress(conferenceId, guestPassword, getSmcToken(token));
    }

    @Override
    public GetExternalRecordAddressResponse getExternalRecordAddress(String conferenceId, String token) throws
            MyHttpException {
        //TODo
        return smcMeetingManagementService.getExternalRecordAddress(conferenceId, getSmcToken(token));
    }

    @Override
    public GetSsoTokenTicketResponse getSsoTokenTicket(GetSsoTokenTicketRequest request, String token) throws
            MyHttpException {
        return smcMeetingManagementService.getSsoTokenTicket(request, getSmcToken(token));
    }

    @Override
    public List<GetSiteRegiesterStatusResp> getStieRegister(GetSiteRegiesterStatusReq request, String token) throws MyHttpException {
        return smcMeetingManagementService.getStieRegister(request, getSmcToken(token));
    }

    @Override
    public String getToken(String conferenceId, String token) throws MyHttpException {
        String conferencesToken = CommonCache.getConferencesToken().get(conferenceId);
        if (conferencesToken == null) {
            String smcConferencesToken = smcMeetingManagementService.getToken(conferenceId, getSmcToken(token));
            CommonCache.getConferencesToken().put(conferenceId, smcConferencesToken);
            return smcConferencesToken;
        } else {
            return conferencesToken;
        }
    }
}
