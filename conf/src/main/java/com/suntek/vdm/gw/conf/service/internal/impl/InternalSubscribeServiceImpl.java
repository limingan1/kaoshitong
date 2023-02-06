package com.suntek.vdm.gw.conf.service.internal.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.enums.ConfApiUrl;
import com.suntek.vdm.gw.conf.enumeration.SiteTypes;
import com.suntek.vdm.gw.conf.enumeration.SubscribeUserType;
import com.suntek.vdm.gw.conf.service.*;
import com.suntek.vdm.gw.conf.service.impl.BaseServiceImpl;
import com.suntek.vdm.gw.conf.service.impl.OtherServiceImpl;
import com.suntek.vdm.gw.conf.service.internal.InternalSubscribeService;
import com.suntek.vdm.gw.conf.ws.client.CustomStompSessionHandler;
import com.suntek.vdm.gw.core.customexception.BaseStateException;
import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import com.suntek.vdm.gw.common.pojo.CascadeParticipantParameter;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.core.service.*;
import com.suntek.vdm.gw.common.util.CommonHelper;
import com.suntek.vdm.gw.common.pojo.MultiPicInfo;
import com.suntek.vdm.gw.common.pojo.ParticipantDetail;
import com.suntek.vdm.gw.common.pojo.request.ScheduleConfBrief;
import com.suntek.vdm.gw.common.pojo.request.meeting.GetConditionsMeetingRequest;
import com.suntek.vdm.gw.common.pojo.response.meeting.GetMeetingDetailResponse;
import com.suntek.vdm.gw.common.pojo.response.meeting.GetParticipantsResponse;
import com.suntek.vdm.gw.common.pojo.response.GetConditionsMeetingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InternalSubscribeServiceImpl extends BaseServiceImpl implements InternalSubscribeService {
    @Autowired
    private SubscribeService subscribeService;
    @Autowired
    private SubscribeManageService subscribeManageService;
    @Autowired
    private MeetingManagerService meetingManagerService;
    @Autowired
    private MeetingControlService meetingControlService;
    @Autowired
    private MeetingInfoManagerService meetingInfoManagerService;
    @Autowired
    private CascadeChannelService cascadeChannelService;
    @Autowired
    private RemoteGwService remoteGwService;
    @Autowired
    private CustomStompSessionHandler customStompSessionHandler;
    @Autowired
    OtherServiceImpl otherService;

    public void init(String username) {
        log.info("init local smc data");
        for (int i = 0; i < 3; i++) {
            CommonHelper.sleep(200 * i);
            String ticket = null;
            try {
                ticket = otherService.getMeetingTickets(username, CoreConfig.INTERNAL_USER_TOKEN);
            } catch (MyHttpException exception) {
                exception.printStackTrace();
            }
            if(ticket == null){
                continue;
            }
            try{
                boolean openFlag = subscribeManageService.connect(CoreConfig.INTERNAL_USER_TOKEN, CoreConfig.INTERNAL_SUBSCRIBE_USER, SubscribeUserType.INTERNAL, ticket, CoreConfig.INTERNAL_USER_TOKEN, customStompSessionHandler);
                if (openFlag) {
                    break;
                }
            }catch (Exception e){
                log.error(e.getMessage());
                log.error(String.valueOf(e.getStackTrace()));
            }
        }
        //订阅所有会议状态
        subscribeService.conferencesStatus(null, CoreConfig.INTERNAL_SUBSCRIBE_USER, CoreConfig.INTERNAL_USER_TOKEN);
        //查询所有会议
        GetConditionsMeetingResponse response = null;
        try {
            response = meetingManagerService.getConditions(null, CoreConfig.INTERNAL_USER_TOKEN, 0, 1000);
        } catch (Exception e) {
            log.error("exception", e);
        }
        if (null == response) {
            log.error("query conference list fail;");
            return;
        }
        try {
            if (!CollectionUtils.isEmpty(response.getContent())) {
                resumeMeeting(response.getContent());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 恢复在线会议
     *
     * @param list
     */
    private void resumeMeeting(List<ScheduleConfBrief> list) {
        for (ScheduleConfBrief scheduleConfBrief : list) {
            if (!"ONLINE".equals(scheduleConfBrief.getStage())) {
                continue;
            }
            //建立会议缓存
            try {
                GetMeetingDetailResponse getMeetingDetailResponse = meetingControlService.getMeetingDetail(scheduleConfBrief.getId(), CoreConfig.INTERNAL_USER_TOKEN, null, null);
            } catch (MyHttpException e) {
                e.printStackTrace();
            } catch (BaseStateException e) {
                e.printStackTrace();
            }

            GetParticipantsResponse response = null;
            try {
                response = meetingControlService.getParticipants(null, scheduleConfBrief.getId(), CoreConfig.INTERNAL_USER_TOKEN, null, 0, 1000, null);
            } catch (MyHttpException e) {
                e.printStackTrace();
            }
            if (response == null || response.getContent() == null) {
                continue;
            }
            List<ParticipantDetail> participantDetails = response.getContent().stream().filter(x -> x.getGeneralParam().getVdcMarkCascadeParticipant() != null).collect(Collectors.toList());
            for (ParticipantDetail participantDetail : participantDetails) {
                //TODO 建立级联通道内存
                CascadeParticipantParameter cascadeParticipantParameter = CascadeParticipantParameter.valueOf(participantDetail.getGeneralParam().getVdcMarkCascadeParticipant());
                if (cascadeParticipantParameter != null) {
                    //取级联通道对应的会议级联会议Id
                    String casConfId = participantDetail.getGeneralParam().getUri();
                    final String finalCasConfId = casConfId;
                    if(casConfId.contains("**")){
                        casConfId = casConfId.split("\\*\\*")[0];
                    }
                    GwId gwId = cascadeParticipantParameter.getGwId();
                    SiteTypes siteTypes = SiteTypes.CALL_CONNECTED;
                    MultiPicInfo multiPicInfo = participantDetail.getState().getMultiPicInfo();
                    cascadeChannelService.updateChannelLstInfo(scheduleConfBrief.getId(), casConfId,
                            cascadeParticipantParameter.getDirection(),
                            cascadeParticipantParameter.getIndex(), siteTypes, multiPicInfo);
                    //如果是下级级联主通道  说明有下级会议
                    if (cascadeParticipantParameter.getDirection().equals(CascadeParticipantDirection.DOWN)&&cascadeParticipantParameter.isMain()) {
                        GetConditionsMeetingRequest body = new GetConditionsMeetingRequest();
                        //根据接入号（级联会议接入号）查询会议
                        body.setCasConfId(casConfId);
                        try {
                            body.setSmcAccessCode(scheduleConfBrief.getAccessCode());
                            String json = remoteGwService.toByGwId(gwId).post(String.format(ConfApiUrl.CONFERENCES_CONDITIONS.value(), 0, 10), body).getBody();
                            GetConditionsMeetingResponse getConditionsMeetingResponse = JSON.parseObject(json, GetConditionsMeetingResponse.class);
                            if (getConditionsMeetingResponse.getContent() != null && getConditionsMeetingResponse.getContent().size() > 0) {
                                List<ScheduleConfBrief> collect = getConditionsMeetingResponse.getContent().stream().filter(item -> finalCasConfId.equals(item.getAccessCode())).collect(Collectors.toList());
                                if (collect.size() == 0) {
                                    return;
                                }
                                ScheduleConfBrief childConfInfo = collect.get(0);
                                meetingInfoManagerService.createChild(scheduleConfBrief.getId(), childConfInfo.getId(), childConfInfo.getAccessCode(), childConfInfo.getSubject(), gwId);
                            }
                            continue;
                        } catch (MyHttpException e) {
                            log.error("MyHttpExceptionz :{}", e.getBody());
                            meetingInfoManagerService.createChild(scheduleConfBrief.getId(), null, casConfId, null, gwId);
                        } catch (Exception e) {
                            meetingInfoManagerService.createChild(scheduleConfBrief.getId(), null, casConfId, null, gwId);
                            log.error("exception", e);
                        }
                    }
                }
            }
            try {
                String conferenceToken = meetingManagerService.getToken(scheduleConfBrief.getId(), CoreConfig.INTERNAL_USER_TOKEN);
                subscribeService.conferencesParticipantsStatus(scheduleConfBrief.getId(), conferenceToken, null, CoreConfig.INTERNAL_SUBSCRIBE_USER, CoreConfig.INTERNAL_USER_TOKEN, true);
                subscribeService.conferencesControllerStatus(scheduleConfBrief.getId(), conferenceToken, null, CoreConfig.INTERNAL_SUBSCRIBE_USER, CoreConfig.INTERNAL_USER_TOKEN, false);
            } catch (MyHttpException e) {
                e.printStackTrace();
                continue;
            }
        }
    }
}

