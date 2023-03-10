package com.suntek.vdm.gw.conf.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.suntek.vdm.gw.common.api.request.ParticipantsControlRequestEx;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.*;
import com.suntek.vdm.gw.common.pojo.request.ScheduleConfBrief;
import com.suntek.vdm.gw.common.pojo.request.meeting.ParticipantsControlRequest;
import com.suntek.vdm.gw.common.util.SystemConfiguration;
import com.suntek.vdm.gw.common.util.TransactionManage;
import com.suntek.vdm.gw.common.api.request.MeetingControlRequestEx;
import com.suntek.vdm.gw.common.enums.ConfApiUrl;
import com.suntek.vdm.gw.conf.enumeration.SiteTypes;
import com.suntek.vdm.gw.conf.pojo.*;
import com.suntek.vdm.gw.conf.service.*;
import com.suntek.vdm.gw.conf.service.internal.InternalLinkService;
import com.suntek.vdm.gw.core.customexception.BaseStateException;
import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import com.suntek.vdm.gw.common.pojo.CascadeParticipantParameter;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.core.enumeration.NodeBusinessType;
import com.suntek.vdm.gw.core.service.NodeManageService;
import com.suntek.vdm.gw.core.service.RemoteGwService;
import com.suntek.vdm.gw.core.service.RoutManageService;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.common.util.CommonHelper;
import com.suntek.vdm.gw.common.enums.MeetingControlType;
import com.suntek.vdm.gw.common.pojo.request.GetParticipantsRequest;
import com.suntek.vdm.gw.common.api.request.MeetingControlRequest;
import com.suntek.vdm.gw.common.pojo.request.meeting.GetConditionsMeetingRequest;
import com.suntek.vdm.gw.common.pojo.response.meeting.GetMeetingDetailResponse;
import com.suntek.vdm.gw.common.pojo.response.meeting.GetParticipantsResponse;
import com.suntek.vdm.gw.common.pojo.response.GetConditionsMeetingResponse;
import com.suntek.vdm.gw.smc.service.SmcMeetingControlService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;


@Service
@Slf4j
public class NotifyExcecutorServiceImpl extends BaseServiceImpl implements NotifyExcecutorService {
    @Autowired
    private MeetingManagerService meetingManagerService;
    @Autowired
    private MeetingControlService meetingControlService;
    @Autowired
    private SubscribeService subscribeService;
    @Autowired
    private RoutManageService routManageService;
    @Autowired
    private CascadeChannelService cascadeChannelService;
    @Autowired
    private ParticipantInfoManagerService participantInfoManagerService;
    @Autowired
    private MeetingInfoManagerService meetingInfoManagerService;
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private RemoteGwService remoteGwService;
    @Autowired
    private SmcMeetingControlService smcMeetingControlService;
    @Autowired
    private MeetingService meetingService;
    @Autowired
    private VideoSourceService videoSourceService;
    @Autowired
    private TempServiceImpl tempService;
    @Autowired
    private CascadeChannelNotifyService cascadeChannelNotifyService;
    @Autowired
    private NodeManageService nodeManageService;
    @Autowired
    private CascadeChannelManageService cascadeChannelManageService;
    @Autowired
    private InternalLinkService internalLinkService;


    /**
     * @param conferenceStatusNotify
     */
    @Override
    @Async("taskExecutor")
    public void dealConferenceStatusNotify(ConferenceStatusNotify conferenceStatusNotify) {
        log.info("Conferences status notify:{}", JSON.toJSONString(conferenceStatusNotify));
        if (conferenceStatusNotify.getConferenceStages() == null) {
            return;
        }
        ConferenceStatusNotify failConferenceStatusNotify = new ConferenceStatusNotify();
        List<ConferenceStatusInfo> failedList = new ArrayList<>();
        List<ConferenceStatusInfo> list = conferenceStatusNotify.getConferenceStages();
        for (ConferenceStatusInfo conferenceStatusInfo : list) {
            if (CoreConfig.CONFERENCE_CANCEL.equals(conferenceStatusInfo.getStage())) {
                //????????????
                log.info("Conferences end by Id:{}", conferenceStatusInfo.getConferenceId());
                meetingInfoManagerService.del(conferenceStatusInfo.getConferenceId());
            } else if (CoreConfig.CONFERENCE_ONLINE.equals(conferenceStatusInfo.getStage())) {
                //????????????
                String conferenceId = conferenceStatusInfo.getConferenceId();
                log.info("Conferences start by Id:{}", conferenceId);
                //?????????????????? ????????????
                TransactionManage.notify(new TransactionId(TransactionType.CONFERENCES_ONLINE, conferenceId));
                meetingInfoManagerService.onlineMeeting(conferenceId);
                String token = CoreConfig.INTERNAL_USER_TOKEN;
                GetMeetingDetailResponse getMeetingDetailResponse = null;
                try {
                    getMeetingDetailResponse = meetingControlService.getMeetingDetail(conferenceId, token, null, null);
                } catch (MyHttpException e) {
                    if(e.getBody() != null){
                        JSONObject jsonObject = JSON.parseObject(e.getBody());
                        if("The conference does not exist or has been deleted.".equals(jsonObject.getString("errorDes"))){
                            continue;
                        }
                    }
                    if(e.getCode() == 401){
                        internalLinkService.start();
                    }
                    if (getMeetingDetailFailTimes(conferenceStatusInfo)) continue;
                    failedList.add(conferenceStatusInfo);
                    e.printStackTrace();
                } catch (BaseStateException e) {
                    if (getMeetingDetailFailTimes(conferenceStatusInfo)) continue;
                    failedList.add(conferenceStatusInfo);
                    e.printStackTrace();
                }
                if (getMeetingDetailResponse == null) {
                    log.error("get conference detail fail.==> conferenceId: {}", conferenceId);
                    continue;
                }
                ConferenceUiParam conferenceUiParam = getMeetingDetailResponse.getConferenceUiParam();
                String conferenceToken = null;
                try {
                    conferenceToken = meetingManagerService.getToken(conferenceUiParam.getId(), token);
                } catch (MyHttpException e) {
                    e.printStackTrace();
                    if (getMeetingDetailFailTimes(conferenceStatusInfo)) continue;
                    failedList.add(conferenceStatusInfo);
                    log.error("get conference conferenceToken fail. ==> conferenceId: {}", conferenceId);
                    break;
                }
                GetParticipantsResponse participantDetails = null;
                try {
                    participantDetails = meetingControlService.getParticipants(new GetParticipantsRequest(), conferenceUiParam.getId(), token, null, 0, 1000, null);
                } catch (MyHttpException e) {
                    e.printStackTrace();
                }
                if (participantDetails == null || participantDetails.getContent() == null) {
                    log.error("get conference participantDetails fail. conferenceId: {}", conferenceId);
                    failedList.add(conferenceStatusInfo);
                    continue;
                }
                for (ParticipantDetail participantDetail : participantDetails.getContent()) {
                    //TODO ????????????????????????
                    String vdcMarkCascadeParticipant = participantDetail.getGeneralParam().getVdcMarkCascadeParticipant();
                    if (vdcMarkCascadeParticipant == null) {
                        continue;
                    }
                    CascadeParticipantParameter cascadeParticipantParameter = CascadeParticipantParameter.valueOf(vdcMarkCascadeParticipant);
                    if (cascadeParticipantParameter != null) {
                        //??????????????????????????????????????????Id
                        String casConfId = participantDetail.getGeneralParam().getUri();
                        if(casConfId.contains("**")){
                            casConfId = casConfId.split("\\*\\*")[0];
                        }
                        SiteTypes siteTypes = SiteTypes.CALL_CONNECTED;
                        MultiPicInfo multiPicInfo = participantDetail.getState().getMultiPicInfo();
                        cascadeChannelService.updateChannelLstInfo(conferenceId, casConfId,
                                cascadeParticipantParameter.getDirection(),
                                cascadeParticipantParameter.getIndex(), siteTypes, multiPicInfo);
                        //???????????????????????????  ?????????????????????
                        if (cascadeParticipantParameter.getDirection().equals(CascadeParticipantDirection.DOWN) && cascadeParticipantParameter.isMain()) {
                            GetConditionsMeetingRequest body = new GetConditionsMeetingRequest();
                            //??????????????????????????????????????????????????????
                            body.setCasConfId(casConfId);
                            try {
                                NodeBusinessType nodeBusinessType = nodeManageService.getNodeBusinessType(cascadeParticipantParameter.getGwId().getNodeId());
//                            ?????????welink??????????????????
                                if(nodeBusinessType != null && NodeBusinessType.WELINK.equals(nodeBusinessType)){
                                    body.setSmcAccessCode(conferenceUiParam.getAccessCode());
                                }
                                String json = remoteGwService.toByGwId(cascadeParticipantParameter.getGwId()).post(String.format(ConfApiUrl.CONFERENCES_CONDITIONS.value(), 0, 200), body).getBody();
                                GetConditionsMeetingResponse getConditionsMeetingResponse = JSON.parseObject(json, GetConditionsMeetingResponse.class);
                                if (getConditionsMeetingResponse.getContent() != null && getConditionsMeetingResponse.getContent().size() > 0) {
                                    for(ScheduleConfBrief childConfInfo: getConditionsMeetingResponse.getContent()){
                                        if(casConfId.equals(childConfInfo.getAccessCode())){
                                            meetingInfoManagerService.createChild(conferenceId, childConfInfo.getId(), childConfInfo.getAccessCode(), childConfInfo.getSubject(), cascadeParticipantParameter.getGwId());
                                            break;
                                        }
                                    }
                                } else {
                                    //TODO  ????????????????????????
                                }
                            } catch (MyHttpException e) {

                            }
                        }
                    }
                }
                try {
                    subscribeService.conferencesParticipantsStatus(conferenceUiParam.getId(), conferenceToken, null, CoreConfig.INTERNAL_SUBSCRIBE_USER, token, true);
                    subscribeService.conferencesControllerStatus(conferenceUiParam.getId(), conferenceToken, null, CoreConfig.INTERNAL_SUBSCRIBE_USER, token, false);
                }catch (Exception e){
                    log.error("subScribe child error: {}", e.getMessage());
                }
            }
        }
        if(failedList.size()>0){
            failConferenceStatusNotify.setConferenceStages(failedList);
            CommonHelper.sleep(500);
            dealConferenceStatusNotify(failConferenceStatusNotify);
        }
    }

    private boolean getMeetingDetailFailTimes(ConferenceStatusInfo conferenceStatusInfo) {
        Integer failTimes = conferenceStatusInfo.getFailTimes();
        if(failTimes == null){
            conferenceStatusInfo.setFailTimes(10);
        }else{
            conferenceStatusInfo.setFailTimes(failTimes--);
        }
        if(failTimes == 0){
            return true;
        }
        return false;
    }

    @Override
    @Async("taskExecutor")
    public void dealConferencesControllerStatusNotify(ConferencesControllerStatusNotify conferencesControllerStatusNotify, String confId) {
        try {
            if(conferencesControllerStatusNotify.getState() == null){
                return;
            }
            MeetingInfo meetingInfo = meetingInfoManagerService.get(conferencesControllerStatusNotify.getState().getConferenceId());
            if (meetingInfo != null) {
                //???????????????????????????
                TransactionId transactionId = new TransactionId(TransactionType.CONFERENCES_STATUS, confId);
                if (TransactionManage.get(transactionId) != null) {
                    TransactionManage.notify(transactionId);
                }

                ConferenceState conferenceStateOld = meetingInfo.getConferenceState();
                ConferenceState conferenceStateNew = conferencesControllerStatusNotify.getState();
//                ?????????????????????1s??????????????????????????????
                if(System.currentTimeMillis() - meetingInfo.getConferenceState().getChairmanFlagTime() < 1000){
                    meetingInfo.setConferenceState(conferencesControllerStatusNotify.getState());
                    return;
                }
                //?????? ?????? ?????????????????????
                if ((!StringUtils.isEmpty(conferenceStateOld.getChairmanId())) && (!StringUtils.isEmpty(conferenceStateOld.getBroadcastId())) && StringUtils.isEmpty(conferenceStateNew.getChairmanId())) {
                    String pid = conferenceStateOld.getChairmanId();
                    ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(confId, pid);
                    if (participantInfo != null) {
                        if (participantInfo.getConferenceId().equals(confId)) {
                            if (!participantInfo.isCascadeParticipant()) {
                                if (participantInfo.getOnline()) {
                                    MeetingControlRequest meetingControlRequest = new MeetingControlRequest();
                                    meetingControlRequest.setBroadcaster("");
                                    try {
                                        if (SystemConfiguration.smcVersionIsV3()) {
                                            smcMeetingControlService.meetingControl(confId, meetingControlRequest, getSmcToken(CoreConfig.INTERNAL_USER_TOKEN));
                                        }
                                        meetingControlService.sendChild(confId, meetingControlRequest);
                                        meetingControlService.sendTop(confId, meetingControlRequest);
                                    } catch (MyHttpException e) {

                                    }
                                }
                            }
                        }
                    }
                }
                meetingInfo.setConferenceState(conferencesControllerStatusNotify.getState());
            }
        }catch (Exception e){
            log.error("dealConferencesControllerStatusNotify exception: {}, stackTrace???{}",e.getMessage(),e.getStackTrace());
        }
    }


    @Override
    @Async("taskExecutor")
    public void dealParticipantStatusNotify(ParticipantStatusNotify participantStatusNotify, String confId) {
        try {
            //??????
            if (participantStatusNotify.getType() == 3) {
                for (ParticipantStatusInfo participantStatusInfo : participantStatusNotify.getChangeList()) {
                    ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(confId, participantStatusInfo.getId());
                    if (participantInfo == null) {
                        if(participantStatusInfo.getOnline() != null && participantStatusInfo.getOnline()){
                            participantInfo = new ParticipantInfo();
                            participantInfo.setParticipantId(participantStatusInfo.getId());
                            participantInfo.setConferenceId(participantStatusNotify.getConferenceId());
                            participantInfo.setUri(participantStatusInfo.getUri());
                            participantInfo.setName(participantStatusInfo.getName());
                            participantInfo.setVideoMute(participantStatusInfo.getVideoMute());
                            participantInfo.setSiteVideoMute(participantStatusInfo.getSiteVideoMute());
                            participantInfo.setOnline(participantStatusInfo.getOnline());
                            participantInfo.setMultiPicInfo(participantStatusInfo.getMultiPicInfo());
                            participantInfo.setEncodeType(participantStatusInfo.getEncodeType());
                            MeetingInfo meetingInfo = meetingInfoManagerService.get(confId);
                            siteOnlineCheckToBroadcast(confId, participantInfo, meetingInfo, true);
                        }
                        continue;
                    }
                    Boolean oldOnline = participantInfo.getOnline();
                    Boolean oldVideoMute = participantInfo.getVideoMute() == null || participantInfo.getVideoMute();
                    Boolean oldsiteVideoMute = participantInfo.getSiteVideoMute() == null || participantInfo.getSiteVideoMute();
                    participantInfo.setVideoMute(participantStatusInfo.getVideoMute());
                    participantInfo.setSiteVideoMute(participantStatusInfo.getSiteVideoMute());
                    participantInfo.setOnline(participantStatusInfo.getOnline());
                    participantConferenceStateCheck(confId, oldOnline != null && oldOnline, participantStatusInfo, participantInfo);
                    if (participantStatusInfo.getOnline()) {
                        //????????????????????????
                        if (participantInfo.isCascadeParticipant() && confId.equals(participantInfo.getConferenceId())) {
                            //????????????
                            tempService.callCascadeChannelDel(confId, participantStatusInfo.getParticipantId());
                            //?????????????????????????????????????????????????????????
                            meetingService.CascadeParticipantStatusHandle(confId, participantStatusInfo.getId(), participantInfo.isCascadeMainParticipant(), participantStatusInfo.getMute(), participantStatusInfo.getQuiet(), participantStatusInfo.getVideoSwitchAttribute());
                            //?????????????????????
                            videoSourceService.videoSourceHandleBySubscribeNotify(participantInfo, participantStatusInfo.getMultiPicInfo());
                            //????????????????????????????????????
                            dealMainChannelOnlineChairmanStatus(confId, participantInfo, oldOnline);

                            participantInfo.setMultiPicInfo(participantStatusInfo.getMultiPicInfo());
                        } else {

                            participantInfo.setMultiPicInfo(participantStatusInfo.getMultiPicInfo());
                        }
                        videoSourceService.casChannelFreeHandelBySubscribeNotify(confId, participantInfo, participantStatusInfo.getMultiPicInfo());
                    } else {
                        if (oldOnline != null && oldOnline) {
                            //??????????????????
                            if (confId.equals(participantInfo.getConferenceId())) {
                                cascadeChannelNotifyService.targetParticipantOffline(confId, participantInfo.getConferenceId(), participantInfo.getParticipantId(), false);
                            }
                        }

                        participantInfo.setMultiPicInfo(null);
                    }
                    //???????????????
                    if(((oldVideoMute || oldsiteVideoMute)
                            && ((participantStatusInfo.getVideoMute() != null && !participantStatusInfo.getVideoMute()) || (participantStatusInfo.getSiteVideoMute() != null && !participantStatusInfo.getSiteVideoMute())))
                            && participantInfo.isCascadeParticipant() && participantInfo.isCascadeMainParticipant()
                            && participantInfo.getOnline() != null && participantInfo.getOnline() && !"ENCODE".equals(participantInfo.getEncodeType())){
                        //????????????????????????
                        MeetingInfo meetingInfo = meetingInfoManagerService.get(confId);
                        siteOnlineCheckToBroadcast(confId, participantInfo, meetingInfo, true);
                    }
                }
                return;
            }
            //??????
            if (participantStatusNotify.getType() == 1) {
                MeetingInfo meetingInfo = meetingInfoManagerService.get(confId);
                for (ParticipantStatusInfo participantStatusInfo : participantStatusNotify.getChangeList()) {
                    ParticipantInfo participantInfo = new ParticipantInfo();
                    participantInfo.setParticipantId(participantStatusInfo.getId());
                    participantInfo.setConferenceId(participantStatusNotify.getConferenceId());
                    participantInfo.setUri(participantStatusInfo.getUri());
                    participantInfo.setName(participantStatusInfo.getName());
                    participantInfo.setVideoMute(participantStatusInfo.getVideoMute());
                    participantInfo.setSiteVideoMute(participantStatusInfo.getSiteVideoMute());
                    participantInfo.setOnline(participantStatusInfo.getOnline());
                    participantInfo.setMultiPicInfo(participantStatusInfo.getMultiPicInfo());
                    participantInfo.setEncodeType(participantStatusInfo.getEncodeType());
                    //?????????????????????
                    if (participantStatusInfo.getVdcMarkCascadeParticipant() != null) {
                        CascadeParticipantParameter cascadeParticipantParameter = CascadeParticipantParameter.valueOf(participantStatusInfo.getVdcMarkCascadeParticipant());
                        participantInfo.setCascadeParticipantParameter(cascadeParticipantParameter);
                        //??????????????????????????????  ??????????????????????????????
                        if (cascadeParticipantParameter.getDirection().equals(CascadeParticipantDirection.DOWN) && cascadeParticipantParameter.isMain()) {
                            //???????????????????????????????????????
                            ChildMeetingInfo childMeetingInfo;
                            if (!confId.equals(participantStatusNotify.getConferenceId())) {
                                childMeetingInfo = meetingInfoManagerService.get(confId).getChildByConferenceId(participantStatusNotify.getConferenceId());
                            } else {
                                //??????????????????
                                String confCasId = participantInfo.getUri();
                                childMeetingInfo = meetingInfoManagerService.get(confId).getChild(confCasId);
                            }
                            if (childMeetingInfo != null) {
                                if (!childMeetingInfo.initialized()) {
                                    meetingService.initCasConf();
                                }
                            } else {
                                try {
                                    GetParticipantsRequest getParticipantsRequest = new GetParticipantsRequest();
                                    List<String> participantIds = new ArrayList<>();
                                    participantIds.add(participantInfo.getParticipantId());
                                    getParticipantsRequest.setParticipantIds(participantIds);
                                    GetParticipantsResponse participantDetails = meetingControlService.getParticipants(new GetParticipantsRequest(), confId, CoreConfig.INTERNAL_USER_TOKEN, null, 0, 1000, null);
                                    if (participantDetails.exist()) {
                                        meetingInfoManagerService.createChild(confId, null, participantInfo.getUri(), null, cascadeParticipantParameter.getGwId());
                                        meetingService.initCasConf();
                                    }
                                } catch (MyHttpException e) {
                                }
                            }
                            //?????????????????????????????????
                            if(participantStatusInfo.getVideoMute() != null && !participantStatusInfo.getVideoMute() && confId.equals(participantStatusNotify.getConferenceId())){
                                //????????????????????????
                                siteOnlineCheckToBroadcast(confId, participantInfo, meetingInfo, true);
                            }

                        }
                        if (StringUtils.isEmpty(participantStatusNotify.getConfCasId()) || meetingInfo.getConfCasId().equals(participantStatusNotify.getConfCasId())){
                            meetingService.CascadeParticipantStatusHandle(confId, participantStatusInfo.getId(), participantInfo.isCascadeMainParticipant(), participantStatusInfo.getMute(), participantStatusInfo.getQuiet(), participantStatusInfo.getVideoSwitchAttribute());
                        }
//                        welink??????????????????????????????
                        if(cascadeParticipantParameter.isMain() && cascadeParticipantParameter.getDirection().equals(CascadeParticipantDirection.UP)
                                && participantInfo.getOnline() != null && participantInfo.getOnline()){
                            siteOnlineCheckToBroadcast(confId, participantInfo, meetingInfo, true);
                        }
                    }
                    if(participantStatusInfo.getSubTpParams() != null){
                        for(TpGeneralParam tpGeneralParam: participantStatusInfo.getSubTpParams()){
                            ParticipantInfo tpParticipantInfo = new ParticipantInfo();
                            tpParticipantInfo.setUri(tpGeneralParam.getUri());
                            tpParticipantInfo.setParticipantId(tpGeneralParam.getId());
                            tpParticipantInfo.setConferenceId(participantStatusNotify.getConferenceId());
                            tpParticipantInfo.setName(tpGeneralParam.getName());
                            participantInfoManagerService.addParticipant(confId, tpParticipantInfo);
                        }
                    }
                    participantInfoManagerService.addParticipant(confId, participantInfo);
                    if(participantInfo.getOnline() != null && participantInfo.getOnline() && !participantInfo.isCascadeParticipant() && !"ENCODE".equals(participantInfo.getEncodeType())){
                        //?????????????????????
                        siteOnlineCheckToBroadcast(confId, participantInfo, meetingInfo, false);
                    }
                }
            }
            //??????
            if (participantStatusNotify.getType() == 2) {
                for (ParticipantStatusInfo participantStatusInfo : participantStatusNotify.getChangeList()) {
                    participantNotOnlineHandle(confId, participantStatusInfo.getId());
                    participantInfoManagerService.delParticipant(confId, participantStatusInfo.getId());
                }
            }
        } catch (
                NullPointerException e) {
            e.printStackTrace();
        }

    }

    private synchronized void firstSiteOnlineToBroadcast(String confId, ParticipantInfo participantInfo, boolean iswelink, Boolean isCasChannel){
        MeetingInfo meetingInfo = meetingInfoManagerService.get(confId);



        if(iswelink){
            ChildMeetingInfo childMeetingInfo = meetingInfo.getFirstChildMeetingInfoByConfId(participantInfo.getConferenceId());
            //????????????
            Integer bSyncBroadcasOnlineFlag = childMeetingInfo.getBSyncBroadcasOnlineFlag();
            Integer bSyncBroadcastOnlineChannelFlag = childMeetingInfo.getBSyncBroadcastOnlineChannelFlag();
            if(isCasChannel && (bSyncBroadcastOnlineChannelFlag == null || bSyncBroadcastOnlineChannelFlag == 0)){
                if(meetingInfo.getBSyncBroadcasOnlineFlag() != null && meetingInfo.getBSyncBroadcasOnlineFlag() == 1
                        && childMeetingInfo.getBSyncBroadcastOnlineChannelFlag() != null && childMeetingInfo.getBSyncBroadcastOnlineChannelFlag() == 0){
                    return;
                }
                log.info("welink casChannel ready");
                childMeetingInfo.setBSyncBroadcastOnlineChannelFlag(0);
                if(meetingInfo.getBSyncBroadcasOnlineFlag() == null || meetingInfo.getBSyncBroadcasOnlineFlag() != 1){
                    return;
                }
                //???????????????????????????????????????
                participantInfo = getOnlineParticipantInfoByConfId(meetingInfo.getId(), meetingInfo);
                if(participantInfo == null){
                    log.info("welink online participant ont found");
                    return;
                }

                CascadeChannelInfo cascadeChannelInfoMain = cascadeChannelManageService.getCascadeChannelMain(confId, CascadeParticipantDirection.DOWN, childMeetingInfo.getId());
                if(cascadeChannelInfoMain == null){
                    log.error("Main participantId is null");
                    return;
                }
//                ParticipantInfo mainChannelParticipantInfo = meetingInfo.getAllParticipantMap().get(cascadeChannelInfoMain.getParticipantId());
                //??????smc???????????????????????????
//                if((mainChannelParticipantInfo.getVideoMute() != null && mainChannelParticipantInfo.getVideoMute()) || (mainChannelParticipantInfo.getSiteVideoMute() != null && mainChannelParticipantInfo.getSiteVideoMute())){
//                    log.info("welink main channel is not video.");
//                    return;
//                }



                if(!meetingInfo.getAndSetHasBroacdcastFirst()){
//                smc?????????welink?????????
//                    smcViewSiteReq(confId, participantInfo.getParticipantId(), mainChannelParticipantInfo.getParticipantId());

//                ??????smc??????
                    MeetingControlRequest requestLocal = new MeetingControlRequest();
                    requestLocal.setBroadcaster(participantInfo.getParticipantId());
                    try {
                        smcMeetingControlService.meetingControl(confId, requestLocal, getSmcToken(CoreConfig.INTERNAL_USER_TOKEN));
                    } catch (MyHttpException exception) {
                        exception.printStackTrace();
                    }

//                ??????welink???????????????
                    broadcastWelinkUpCasChannel(confId, childMeetingInfo, cascadeChannelInfoMain);

                }else{
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    welink channel?????????
                    MeetingControlRequest requestLocal = new MeetingControlRequest();
                    requestLocal.setBroadcaster(participantInfo.getParticipantId());
                    try {
                        smcMeetingControlService.meetingControl(confId, requestLocal, getSmcToken(CoreConfig.INTERNAL_USER_TOKEN));
                    } catch (MyHttpException exception) {
                        exception.printStackTrace();
                    }

//                ??????welink???????????????
                    broadcastWelinkUpCasChannel(confId, childMeetingInfo, cascadeChannelInfoMain);

////                ?????????????????????
//                    try {
//                        lockReq(confId, 1, mainChannelParticipantInfo.getParticipantId());
//
////                smc??? welink??????????????????????????????
//                        smcViewSiteReq(confId, mainChannelParticipantInfo.getParticipantId(), participantInfo.getParticipantId());
//
//                    }catch (Exception e){
//
//                    }finally {
////                ??????????????????
//                        lockReq(confId, 0, mainChannelParticipantInfo.getParticipantId());
//                    }
                    meetingInfo.setBSyncBroadcastCompleteFlag(true);
                }

            }else if(!isCasChannel && (bSyncBroadcasOnlineFlag == null || bSyncBroadcasOnlineFlag == 1)){
                if(meetingInfo.getBSyncBroadcastOnlineChannelFlag() != null && meetingInfo.getBSyncBroadcastOnlineChannelFlag() == 0
                        && childMeetingInfo.getBSyncBroadcasOnlineFlag() != null  && childMeetingInfo.getBSyncBroadcasOnlineFlag() == 1){
                    return;
                }
                log.info("welink participant ready");
                childMeetingInfo.setBSyncBroadcasOnlineFlag(1);
                if(meetingInfo.getBSyncBroadcastOnlineChannelFlag() == null || meetingInfo.getBSyncBroadcastOnlineChannelFlag() != 0){
                    return;
                }

                CascadeChannelInfo cascadeChannelInfoMain = cascadeChannelManageService.getCascadeChannelMain(confId, CascadeParticipantDirection.DOWN, childMeetingInfo.getId());
                if(cascadeChannelInfoMain == null){
                    log.error("Main participantId is null");
                    return;
                }
                ParticipantInfo mainChannelParticipantInfo = meetingInfo.getAllParticipantMap().get(cascadeChannelInfoMain.getParticipantId());
                //??????smc???????????????????????????
                if((mainChannelParticipantInfo.getVideoMute() != null && mainChannelParticipantInfo.getVideoMute()) || (mainChannelParticipantInfo.getSiteVideoMute() != null && mainChannelParticipantInfo.getSiteVideoMute())){
                    log.info("welink main channel is not video.");
                    return;
                }

                if(!meetingInfo.getAndSetHasBroacdcastFirst()){
//                ??????welink???????????????
//                    broadcastWelinkUpCasChannel(confId, childMeetingInfo, cascadeChannelInfoMain);

////                welink??????????????????????????????
//                    ParticipantsControlRequestEx request = new ParticipantsControlRequestEx();
//                    SubPic subPic = new SubPic();
//                    subPic.setParticipantId(participantInfo.getParticipantId());
//                    request.setMultiPicInfo(MultiPicInfo.valueOfDefault(subPic));
//
//                    //?????????????????????
//                    CasChannelParameter casChannelParameter = new CasChannelParameter(childMeetingInfo.getId(), 0, CascadeParticipantDirection.UP);
//                    String watchPid = casChannelParameter.toString();
//                    try {
//                        remoteGwService.toByGwId(childMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_PARTICIPANT_STATUS.value(), childMeetingInfo.getId(), watchPid), request).getBody();
//                    }catch (MyHttpException e){
//                        log.error("view remote fail");
//                    }
                    MeetingControlRequestEx requestEx = new MeetingControlRequestEx();
                    requestEx.setBroadcaster(participantInfo.getParticipantId());
                    requestEx.setFrom(CascadeParticipantDirection.UP);
                    try {
                        remoteGwService.toByGwId(childMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_CONFERENCE_STATUS.value(), childMeetingInfo.getId()), requestEx);
                    } catch (MyHttpException exception) {
                        exception.printStackTrace();
                    }

//                ??????smc??? welink???????????????
                    String cascadeChannelPid = cascadeChannelInfoMain.getParticipantId();
                    MeetingControlRequest requestLocal = new MeetingControlRequest();
                    requestLocal.setBroadcaster(cascadeChannelPid);
                    try {
                        smcMeetingControlService.meetingControl(confId, requestLocal, getSmcToken(CoreConfig.INTERNAL_USER_TOKEN));
                    } catch (MyHttpException exception) {
                        exception.printStackTrace();
                    }

                }else{
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    MeetingControlRequestEx requestEx = new MeetingControlRequestEx();
                    requestEx.setBroadcaster(participantInfo.getParticipantId());
                    requestEx.setFrom(CascadeParticipantDirection.UP);
                    try {
                        remoteGwService.toByGwId(childMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_CONFERENCE_STATUS.value(), childMeetingInfo.getId()), requestEx);
                    } catch (MyHttpException exception) {
                        exception.printStackTrace();
                    }

//                ??????smc??? welink???????????????
                    String cascadeChannelPid = cascadeChannelInfoMain.getParticipantId();
                    MeetingControlRequest requestLocal = new MeetingControlRequest();
                    requestLocal.setBroadcaster(cascadeChannelPid);
                    try {
                        smcMeetingControlService.meetingControl(confId, requestLocal, getSmcToken(CoreConfig.INTERNAL_USER_TOKEN));
                    } catch (MyHttpException exception) {
                        exception.printStackTrace();
                    }

//                welink??????????????????????????????
//                    ParticipantsControlRequestEx request = new ParticipantsControlRequestEx();
//                    SubPic subPic = new SubPic();
//                    subPic.setParticipantId(participantInfo.getParticipantId());
//                    request.setMultiPicInfo(MultiPicInfo.valueOfDefault(subPic));
//
//                    //?????????????????????
//                    CasChannelParameter casChannelParameter = new CasChannelParameter(childMeetingInfo.getId(), 0, CascadeParticipantDirection.UP);
//                    String watchPid = casChannelParameter.toString();
//                    try {
//                        remoteGwService.toByGwId(childMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_PARTICIPANT_STATUS.value(), childMeetingInfo.getId(), watchPid), request).getBody();
//                    }catch (MyHttpException e){
//                        log.error("view remote fail");
//                    }
                    meetingInfo.setBSyncBroadcastCompleteFlag(true);
                }
            }

        }else{
            Integer bSyncBroadcasOnlineFlag = meetingInfo.getBSyncBroadcasOnlineFlag();
            Integer bSyncBroadcastOnlineChannelFlag = meetingInfo.getBSyncBroadcastOnlineChannelFlag();
            if(isCasChannel && (bSyncBroadcastOnlineChannelFlag == null || bSyncBroadcastOnlineChannelFlag == 0)){
                ChildMeetingInfo childMeetingInfo = null;
                Map<String, ChildMeetingInfo> childMeetingInfoMap = meetingInfo.getChildMeetingInfoMap();
                for(ChildMeetingInfo child: childMeetingInfoMap.values()){
                    if(child.isWelink()){
                        childMeetingInfo = child;
                    }
                }
                if(childMeetingInfo == null){
                    log.error("welink child meeting is empty.");
                    return;
                }
                if(meetingInfo.getBSyncBroadcastOnlineChannelFlag() != null && meetingInfo.getBSyncBroadcastOnlineChannelFlag() == 0
                     && childMeetingInfo.getBSyncBroadcasOnlineFlag() != null  && childMeetingInfo.getBSyncBroadcasOnlineFlag() == 1){
                    return;
                }
                CascadeChannelInfo cascadeChannelInfoMain = cascadeChannelManageService.getCascadeChannelMain(confId, CascadeParticipantDirection.DOWN, childMeetingInfo.getId());
                if(cascadeChannelInfoMain == null){
                    log.error("Main participantId is null");
                    return;
                }
                ParticipantInfo mainChannelParticipantInfo = meetingInfo.getAllParticipantMap().get(cascadeChannelInfoMain.getParticipantId());
                //??????smc???????????????????????????
                if((mainChannelParticipantInfo.getVideoMute() != null && mainChannelParticipantInfo.getVideoMute()) || (mainChannelParticipantInfo.getSiteVideoMute() != null && mainChannelParticipantInfo.getSiteVideoMute())){
                    log.info("welink main channel is not video.");
                    return;
                }


                log.info("smc casChannel ready");
                meetingInfo.setBSyncBroadcastOnlineChannelFlag(0);
                if(childMeetingInfo.getBSyncBroadcasOnlineFlag() == null || childMeetingInfo.getBSyncBroadcasOnlineFlag() != 1){
                    return;
                }

                participantInfo = getOnlineParticipantInfoByConfId(childMeetingInfo.getId(), meetingInfo);
                if(participantInfo == null){
                    log.info("welink online participant ont found");
                    return;
                }

                if(!meetingInfo.getAndSetHasBroacdcastFirst()){
////                ??????welink???????????????
//                    broadcastWelinkUpCasChannel(confId, childMeetingInfo, cascadeChannelInfoMain);
//
////                welink??????????????????????????????
//                    ParticipantsControlRequestEx request = new ParticipantsControlRequestEx();
//                    SubPic subPic = new SubPic();
//                    subPic.setParticipantId(participantInfo.getParticipantId());
//                    request.setMultiPicInfo(MultiPicInfo.valueOfDefault(subPic));
//
//                    //?????????????????????
//                    CasChannelParameter casChannelParameter = new CasChannelParameter(childMeetingInfo.getId(), 0, CascadeParticipantDirection.UP);
//                    String watchPid = casChannelParameter.toString();
//                    try {
//                        remoteGwService.toByGwId(childMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_PARTICIPANT_STATUS.value(), childMeetingInfo.getId(), watchPid), request).getBody();
//                    }catch (MyHttpException e){
//                        log.error("view remote fail");
//                    }
                    MeetingControlRequestEx requestEx = new MeetingControlRequestEx();
                    requestEx.setBroadcaster(participantInfo.getParticipantId());
                    requestEx.setFrom(CascadeParticipantDirection.UP);
                    try {
                        remoteGwService.toByGwId(childMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_CONFERENCE_STATUS.value(), childMeetingInfo.getId()), requestEx);
                    } catch (MyHttpException exception) {
                        exception.printStackTrace();
                    }


//                ??????smc??? welink???????????????
                    String cascadeChannelPid = cascadeChannelInfoMain.getParticipantId();
                    MeetingControlRequest requestLocal = new MeetingControlRequest();
                    requestLocal.setBroadcaster(cascadeChannelPid);
                    try {
                        smcMeetingControlService.meetingControl(confId, requestLocal, getSmcToken(CoreConfig.INTERNAL_USER_TOKEN));
                    } catch (MyHttpException exception) {
                        exception.printStackTrace();
                    }

                }else{


//                welink??????????????????????????????
//                    ParticipantsControlRequestEx request = new ParticipantsControlRequestEx();
//                    SubPic subPic = new SubPic();
//                    subPic.setParticipantId(participantInfo.getParticipantId());
//                    request.setMultiPicInfo(MultiPicInfo.valueOfDefault(subPic));
//
//                    //?????????????????????
//                    CasChannelParameter casChannelParameter = new CasChannelParameter(childMeetingInfo.getId(), 0, CascadeParticipantDirection.UP);
//                    String watchPid = casChannelParameter.toString();
//                    try {
//                        remoteGwService.toByGwId(childMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_PARTICIPANT_STATUS.value(), childMeetingInfo.getId(), watchPid), request).getBody();
//                    }catch (MyHttpException e){
//                        log.error("view remote fail");
//                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    MeetingControlRequestEx requestEx = new MeetingControlRequestEx();
                    requestEx.setBroadcaster(participantInfo.getParticipantId());
                    requestEx.setFrom(CascadeParticipantDirection.UP);
                    try {
                        remoteGwService.toByGwId(childMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_CONFERENCE_STATUS.value(), childMeetingInfo.getId()), requestEx);
                    } catch (MyHttpException exception) {
                        exception.printStackTrace();
                    }


//                ??????smc??? welink???????????????
                    String cascadeChannelPid = cascadeChannelInfoMain.getParticipantId();
                    MeetingControlRequest requestLocal = new MeetingControlRequest();
                    requestLocal.setBroadcaster(cascadeChannelPid);
                    try {
                        smcMeetingControlService.meetingControl(confId, requestLocal, getSmcToken(CoreConfig.INTERNAL_USER_TOKEN));
                    } catch (MyHttpException exception) {
                        exception.printStackTrace();
                    }
                    meetingInfo.setBSyncBroadcastCompleteFlag(true);

                }
            }else if(!isCasChannel && (bSyncBroadcasOnlineFlag == null || bSyncBroadcasOnlineFlag == 1)){

                ChildMeetingInfo childMeetingInfo = null;
                Map<String, ChildMeetingInfo> childMeetingInfoMap = meetingInfo.getChildMeetingInfoMap();
                for(ChildMeetingInfo child: childMeetingInfoMap.values()){
                    if(child.isWelink()){
                        childMeetingInfo = child;
                    }
                }
                if(childMeetingInfo == null){
                    log.error("welink child meeting is empty.");
                    return;
                }
                if(meetingInfo.getBSyncBroadcasOnlineFlag() != null && meetingInfo.getBSyncBroadcasOnlineFlag() == 1
                        && childMeetingInfo.getBSyncBroadcastOnlineChannelFlag() != null && childMeetingInfo.getBSyncBroadcastOnlineChannelFlag() == 0){
                    return;
                }
                log.info("smc participant ready");
                meetingInfo.setBSyncBroadcasOnlineFlag(1);
                if(childMeetingInfo.getBSyncBroadcastOnlineChannelFlag() == null || childMeetingInfo.getBSyncBroadcastOnlineChannelFlag() != 0){
                    return;
                }

                CascadeChannelInfo cascadeChannelInfoMain = cascadeChannelManageService.getCascadeChannelMain(confId, CascadeParticipantDirection.DOWN, childMeetingInfo.getId());
                if(cascadeChannelInfoMain == null){
                    log.error("Main participantId is null");
                    return;
                }
//                ParticipantInfo mainChannelParticipantInfo = meetingInfo.getAllParticipantMap().get(cascadeChannelInfoMain.getParticipantId());
                //??????smc???????????????????????????
//                if((mainChannelParticipantInfo.getVideoMute() != null && mainChannelParticipantInfo.getVideoMute()) || (mainChannelParticipantInfo.getSiteVideoMute() != null && mainChannelParticipantInfo.getSiteVideoMute())){
//                    log.info("welink main channel is not video.");
//                    return;
//                }
//
//                //        smc?????????
//                CascadeChannelInfo cascadeChannelInfo = cascadeChannelManageService.getCascadeChannelOne(confId, CascadeParticipantDirection.DOWN, childMeetingInfo.getId(), 0);
//                if (cascadeChannelInfo == null) {
//                    log.error("lower cascadeChannel not found");
//                    return;
//                }

                if(!meetingInfo.getAndSetHasBroacdcastFirst()){
//                smc?????????welink?????????
//                    smcViewSiteReq(confId, participantInfo.getParticipantId(), cascadeChannelInfo.getParticipantId());

//                ??????smc??????
                    MeetingControlRequest requestLocal = new MeetingControlRequest();
                    requestLocal.setBroadcaster(participantInfo.getParticipantId());
                    try {
                        smcMeetingControlService.meetingControl(confId, requestLocal, getSmcToken(CoreConfig.INTERNAL_USER_TOKEN));
                    } catch (MyHttpException exception) {
                        exception.printStackTrace();
                    }

//                ??????welink???????????????
                    broadcastWelinkUpCasChannel(confId, childMeetingInfo, cascadeChannelInfoMain);

                }else{
//                    welink channel?????????

////                ?????????????????????
//                    try {
//                        lockReq(confId, 1, cascadeChannelInfo.getParticipantId());
//
////                smc??? welink??????????????????????????????
//                        smcViewSiteReq(confId, cascadeChannelInfo.getParticipantId(), participantInfo.getParticipantId());
//
//                    }catch (Exception e){
//
//                    }finally {
////                ??????????????????
//                        lockReq(confId, 0, cascadeChannelInfo.getParticipantId());
//                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    MeetingControlRequest requestLocal = new MeetingControlRequest();
                    requestLocal.setBroadcaster(participantInfo.getParticipantId());
                    try {
                        smcMeetingControlService.meetingControl(confId, requestLocal, getSmcToken(CoreConfig.INTERNAL_USER_TOKEN));
                    } catch (MyHttpException exception) {
                        exception.printStackTrace();
                    }

//                ??????welink???????????????
                    broadcastWelinkUpCasChannel(confId, childMeetingInfo, cascadeChannelInfoMain);
                    meetingInfo.setBSyncBroadcastCompleteFlag(true);
                }

            }

        }
    }

    private ParticipantInfo getOnlineParticipantInfoByConfId(String confId, MeetingInfo meetingInfo) {
        Map<String, ParticipantInfo> allParticipantMap = meetingInfo.getAllParticipantMap();
        for(ParticipantInfo onlineParticipantInfo: allParticipantMap.values()){
            if(!onlineParticipantInfo.isCascadeParticipant() && onlineParticipantInfo.getOnline()
                    && onlineParticipantInfo.getConferenceId().equals(confId) && !"ENCODE".equals(onlineParticipantInfo.getEncodeType())){
                return onlineParticipantInfo;
            }
        }
        return null;
    }

    private void smcViewSiteReq(String confId, String participantId, String watchedPid) {
        ParticipantsControlRequest request = new ParticipantsControlRequest();
        SubPic subPic = new SubPic();
        subPic.setParticipantId(watchedPid);
        request.setMultiPicInfo(MultiPicInfo.valueOfDefault(subPic));
        try {
            smcMeetingControlService.participantsControl(confId, participantId, request, getSmcToken(CoreConfig.INTERNAL_USER_TOKEN));
        } catch (MyHttpException exception) {
            exception.printStackTrace();
        }
    }

    private void lockReq(String confId, int i, String participantId) {
        ParticipantsControlRequest lockRequest = new ParticipantsControlRequest();
        lockRequest.setVideoSwitchAttribute(i);
        try {
            smcMeetingControlService.participantsControl(confId, participantId, lockRequest, getSmcToken(CoreConfig.INTERNAL_USER_TOKEN));
        } catch (MyHttpException exception) {
            exception.printStackTrace();
        }
    }

    private CascadeChannelInfo broadcastWelinkUpCasChannel(String confId, ChildMeetingInfo childMeetingInfo, CascadeChannelInfo cascadeChannelInfoMain) {

        ParticipantInfo cascadeChannelParticipantInfo = participantInfoManagerService.getParticipant(confId, cascadeChannelInfoMain.getParticipantId());
        if (cascadeChannelParticipantInfo == null) {
            log.error("participantId is null");
        }
        if (cascadeChannelParticipantInfo.getOnline()) {
            try {
                MeetingControlRequestEx requestEx = new MeetingControlRequestEx();
                String requestPid;
                CasChannelParameter casChannelParameter = new CasChannelParameter(childMeetingInfo.getId(), 0, CascadeParticipantDirection.UP);
                requestPid = casChannelParameter.toString();

                requestEx.setBroadcaster(requestPid);
                requestEx.setFrom(CascadeParticipantDirection.UP);
                remoteGwService.toByGwId(childMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_CONFERENCE_STATUS.value(), childMeetingInfo.getId()), requestEx);
            } catch (MyHttpException e) {

            }
        }
        return cascadeChannelInfoMain;
    }

    private synchronized void dealMainChannelOnlineChairmanStatus(String confId, ParticipantInfo participantInfo, Boolean oldOnline) {
        MeetingInfo meetingInfo = meetingInfoManagerService.get(confId);
        if(!participantInfo.isCascadeMainParticipant() || oldOnline == null || oldOnline || CascadeParticipantDirection.UP.equals(participantInfo.getCascadeParticipantParameter().getDirection())) {
            if(CascadeParticipantDirection.UP.equals(participantInfo.getCascadeParticipantParameter().getDirection())){
                meetingInfo.getConferenceState().setChairmanFlagTime(System.currentTimeMillis());
            }
            return;
        }
        try {
//            ??????
            String conferenceStatePid = meetingInfo.getConferenceState().getByType(MeetingControlType.CHAIRMAN);
            ParticipantInfo cascadeChannelParticipantInfo = participantInfoManagerService.getParticipant(confId, participantInfo.getParticipantId());
            MeetingControlRequestEx requestEx = new MeetingControlRequestEx();
            ChildMeetingInfo childMeetingInfo = meetingInfo.getChild(participantInfo.getUri());
            if (childMeetingInfo == null) {
                log.error("childMeetingInfo can not found.");
                return;
            }
            if (childMeetingInfo.smcVersionIsV2() && SystemConfiguration.smcVersionIsV2()) {
                MeetingControlMeetingInfo meetingControlMeetingInfo = new MeetingControlMeetingInfo(confId, nodeDataService.getLocal().toGwId(), SystemConfiguration.getSmcVersion());
                requestEx.setRemoteMeetingInfo(meetingControlMeetingInfo);
            }
            if (StringUtils.isEmpty(conferenceStatePid)) {
                requestEx.setChairman("");
            } else {
                CasChannelParameter casChannelParameter = new CasChannelParameter(childMeetingInfo.getId(), cascadeChannelParticipantInfo.getCascadeParticipantParameter().getIndex(), CascadeParticipantDirection.UP);
                requestEx.setChairman(casChannelParameter.toString());
            }
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
            headers.set("RequestFrom", "UP");
            try {
                remoteGwService.toByGwId(childMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_CONFERENCE_STATUS.value(), childMeetingInfo.getId()), requestEx, headers);
            } catch (MyHttpException e) {

            }
//            ??????
            String conferenceStateSpokesman = meetingInfo.getConferenceState().getByType(MeetingControlType.SPOKESMAN);
            requestEx.setChairman(null);
            if (StringUtils.isEmpty(conferenceStateSpokesman)) {
                requestEx.setSpokesman("");
            } else {
                CasChannelParameter casChannelParameter = new CasChannelParameter(childMeetingInfo.getId(), cascadeChannelParticipantInfo.getCascadeParticipantParameter().getIndex(), CascadeParticipantDirection.UP);
                requestEx.setSpokesman(casChannelParameter.toString());
            }
            try {
                remoteGwService.toByGwId(childMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_CONFERENCE_STATUS.value(), childMeetingInfo.getId()), requestEx, headers);
            } catch (MyHttpException e) {

            }
//            ??????
            String conferenceStatusBoradcast = meetingInfo.getConferenceState().getByType(MeetingControlType.BROADCASTER);
            requestEx.setSpokesman(null);
            if (StringUtils.isEmpty(conferenceStatusBoradcast)) {
                requestEx.setBroadcaster("");
            } else {
                CasChannelParameter casChannelParameter = new CasChannelParameter(childMeetingInfo.getId(), cascadeChannelParticipantInfo.getCascadeParticipantParameter().getIndex(), CascadeParticipantDirection.UP);
                requestEx.setBroadcaster(casChannelParameter.toString());
            }
            try {
                remoteGwService.toByGwId(childMeetingInfo.getGwId()).patch(String.format(ConfApiUrl.CONTROLLER_CONFERENCE_STATUS.value(), childMeetingInfo.getId()), requestEx, headers);
            } catch (MyHttpException e) {

            }


        }catch (Exception e){
            log.error("set chairman error: {}",e.getMessage());
            e.printStackTrace();
        }
    }


    private void participantNotOnlineHandle(String confId, String pId) {
        MeetingInfo meetingInfo = meetingInfoManagerService.get(confId);
        //????????????
        if (meetingInfo.checkParticipantConferenceState(MeetingControlType.CHAIRMAN, pId)) {
            MeetingControlRequest meetingControlRequest = new MeetingControlRequest();
            meetingControlRequest.setChairman("");
            meetingControlRequest.setBroadcaster("");
            try {
                if (SystemConfiguration.smcVersionIsV3()) {
                    smcMeetingControlService.meetingControl(confId, meetingControlRequest, getSmcToken(CoreConfig.INTERNAL_USER_TOKEN));
                }
                meetingControlService.sendChild(confId, meetingControlRequest);
                meetingControlService.sendTop(confId, meetingControlRequest);
            } catch (MyHttpException e) {

            }
        }
        if (meetingInfo.checkParticipantConferenceState(MeetingControlType.BROADCASTER, pId)) {
            MeetingControlRequest meetingControlRequest = new MeetingControlRequest();
            meetingControlRequest.setBroadcaster("");
            try {
                if (SystemConfiguration.smcVersionIsV3()) {
                    smcMeetingControlService.meetingControl(confId, meetingControlRequest, getSmcToken(CoreConfig.INTERNAL_USER_TOKEN));
                }
                meetingControlService.sendChild(confId, meetingControlRequest);
                meetingControlService.sendTop(confId, meetingControlRequest);
            } catch (MyHttpException e) {

            }
        }
    }


    public void participantConferenceStateCheck(String confId, Boolean oldOnline, ParticipantStatusInfo participantStatusInfo, ParticipantInfo participantInfo) {
        MeetingInfo meetingInfo = meetingInfoManagerService.get(confId);
        if (oldOnline == null) {
            return;
        }
        String pId = participantInfo.getParticipantId();
        //??????
        if (oldOnline && !participantStatusInfo.getOnline()) {
            participantNotOnlineHandle(confId, pId);
        }
        //??????
        if (!oldOnline && participantStatusInfo.getOnline()) {
            //?????????????????????
            if (participantInfo.isCascadeParticipant() && confId.equals(participantInfo.getConferenceId())) {
                //???????????????????????????
                if(participantInfo.getCascadeParticipantParameter().getIndex() != 0){
                    ParticipantsControlRequest participantsControlRequest = new ParticipantsControlRequest();
                    participantsControlRequest.setVideoSwitchAttribute(1);
                    try {
                        smcMeetingControlService.participantsControl(confId, pId, participantsControlRequest, getSmcToken(CoreConfig.INTERNAL_USER_TOKEN));
                    } catch (MyHttpException exception) {
                        exception.printStackTrace();
                    }
                }
                if (participantInfo.getCascadeParticipantParameter().getDirection().equals(CascadeParticipantDirection.DOWN)) {
                    ChildMeetingInfo childMeetingInfo = meetingInfo.getChild(participantInfo.getConfCasId());
                    if (childMeetingInfo !=null && !(childMeetingInfo.smcVersionIsV2() && SystemConfiguration.smcVersionIsV2())) {
                        //????????????
                        if (StringUtils.isEmpty(meetingInfo.getConferenceState().getChairmanId())) {
                            MeetingControlRequestEx requestEx = new MeetingControlRequestEx();
                            requestEx.setChairman("");
                            try {
                                meetingControlService.sendChild(confId, requestEx);
                            } catch (MyHttpException e) {

                            }
                        } else {
                            meetingControlService.childCasChannelControl(confId, meetingInfo.getConferenceState().getChairmanId(), MeetingControlType.CHAIRMAN);
                        }
                    }
                }

            }
            if(!participantInfo.isCascadeParticipant() && !"ENCODE".equals(participantInfo.getEncodeType())) {
                siteOnlineCheckToBroadcast(confId, participantInfo, meetingInfo, false);
            }
        }
    }

    private void siteOnlineCheckToBroadcast(String confId, ParticipantInfo participantInfo, MeetingInfo meetingInfo, Boolean isCasChannel) {
        if(meetingInfo.getBSyncBroadcastFlag() == null || meetingInfo.getBSyncBroadcastCompleteFlag()){
            return;
        }
        if(confId.equals(participantInfo.getConferenceId())){
            //?????????????????????
            firstSiteOnlineToBroadcast(confId, participantInfo, false, isCasChannel);
        }else{
            ChildMeetingInfo childMeetingInfo = meetingInfo.getFirstChildMeetingInfoByConfId(participantInfo.getConferenceId());
            if(childMeetingInfo !=null && childMeetingInfo.isWelink()){
                firstSiteOnlineToBroadcast(confId, participantInfo, true, isCasChannel);
            }
        }
    }






    @Override
    @Async("taskExecutor")
    public void dealParticipantInfoNotify(ParticipantInfoNotify participantInfoNotify, String confId, String childConfId) {
        String remoteConfCasId = participantInfoNotify.getConfCasId();
        String remoteConferenceId = participantInfoNotify.getConferenceId();
        MeetingInfo meetingInfo = meetingInfoManagerService.get(confId);
        if (meetingInfo != null){
            ChildMeetingInfo childMeetingInfo = meetingInfo.getChildByConferenceId(childConfId);
            if (childMeetingInfo != null) {
                if (remoteConfCasId.equals(childMeetingInfo.getConfCasId())) {
                    childMeetingInfo.setId(childConfId);
                } else {
                    GwConferenceId gwConferenceId = new GwConferenceId(remoteConferenceId, remoteConfCasId);
                    childMeetingInfo.getChildConferenceIdSet().add(gwConferenceId);
                }
                if(participantInfoNotify.getAllChildConferenceIdSet() != null){
                    Set<GwConferenceId> allChildConferenceIdSet = new HashSet<>();
                    for(GwConferenceId remoteGwConferenceId: participantInfoNotify.getAllChildConferenceIdSet()){
                        Boolean isFound = false;
                        for (GwConferenceId gwConferenceId : childMeetingInfo.getChildConferenceIdSet()) {
                            if(remoteGwConferenceId.getConfCasId().equals(gwConferenceId.getConfCasId())
                                    && (gwConferenceId.getConferenceId() == null || remoteGwConferenceId.getConferenceId() == null || remoteGwConferenceId.getConferenceId().equals(gwConferenceId.getConferenceId()))){
                                isFound = true;
                                break;
                            }
                        }
                        if(!isFound){
                            allChildConferenceIdSet.add(remoteGwConferenceId);
                        }
                    }
                    log.info("add child confId set: {}", allChildConferenceIdSet);
                    childMeetingInfo.getChildConferenceIdSet().addAll(allChildConferenceIdSet);
                }
            } else {
                log.warn("child meeting info is null remoteConfCasId:{},remoteConferenceId:{}", remoteConfCasId, remoteConferenceId);
            }
            for (ParticipantInfo item : participantInfoNotify.getChangeList()) {
                participantInfoManagerService.addParticipant(confId, item);
                if(item.getOnline() != null && item.getOnline() && !"ENCODE".equals(item.getEncodeType())){
                    //?????????????????????
                    boolean isCasChannel = false;
                    if(item.isCascadeParticipant() ){
                        isCasChannel = true;
                    }
                    siteOnlineCheckToBroadcast(confId, item, meetingInfo, isCasChannel);
                }
            }
        }
    }
}
