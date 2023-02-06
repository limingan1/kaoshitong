package com.suntek.vdm.gw.conf.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.enums.SmcVersionType;
import com.suntek.vdm.gw.common.pojo.*;
import com.suntek.vdm.gw.common.pojo.request.AddCasChannelReq;
import com.suntek.vdm.gw.common.pojo.request.meeting.ParticipantsControlRequest;
import com.suntek.vdm.gw.common.pojo.response.AddCasChannelResp;
import com.suntek.vdm.gw.common.util.SystemConfiguration;
import com.suntek.vdm.gw.common.util.TransactionManage;
import com.suntek.vdm.gw.conf.api.request.ChildNodeInfos;
import com.suntek.vdm.gw.conf.api.request.ScheduleMeetingRequestEx;
import com.suntek.vdm.gw.conf.api.response.ScheduleMeetingResponseEx;
import com.suntek.vdm.gw.common.enums.ConfApiUrl;
import com.suntek.vdm.gw.conf.pojo.*;
import com.suntek.vdm.gw.conf.service.*;
import com.suntek.vdm.gw.conf.util.CascadeChannelParameterHandle;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import com.suntek.vdm.gw.core.enumeration.NodeBusinessType;
import com.suntek.vdm.gw.core.service.*;
import com.suntek.vdm.gw.common.util.CommonHelper;
import com.suntek.vdm.gw.smc.pojo.AttendeeReq;
import com.suntek.vdm.gw.smc.pojo.ConferenceReq;
import com.suntek.vdm.gw.smc.request.meeting.management.ModifyMeetingRequest;
import com.suntek.vdm.gw.smc.request.meeting.management.ModifyPeriodMeetingRequest;
import com.suntek.vdm.gw.smc.request.meeting.management.ScheduleMeetingRequest;
import com.suntek.vdm.gw.smc.request.meeting.management.SendMeetingMailRequest;
import com.suntek.vdm.gw.smc.response.meeting.management.GetOneMeetingResponse;
import com.suntek.vdm.gw.smc.service.SmcMeetingControlService;
import com.suntek.vdm.gw.smc.service.SmcMeetingManagementService;
import com.suntek.vdm.gw.welink.service.WelinkMeetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MeetingServiceImpl extends BaseServiceImpl implements MeetingService {
    @Autowired
    private MeetingInfoManagerService meetingInfoManagerService;
    @Autowired
    private SmcMeetingControlService smcMeetingControlService;
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private NodeManageService nodeManageService;
    @Autowired
    private RemoteGwService remoteGwService;
    @Autowired
    @Lazy
    private ProxySubscribeConferencesService proxySubscribeConferencesService;
    @Autowired
    private SubscribeManageService subscribeManageService;
    @Autowired
    private SmcMeetingManagementService smcMeetingManagementService;
    @Autowired
    private TempService tempService;
    @Autowired
    private RoutManageService routManageService;
    @Autowired
    private WelinkMeetingService welinkMeetingService;
    @Autowired
    @Lazy
    private ParticipantInfoManagerService participantInfoManagerService;
    @Autowired
    private ProxySubscribeService proxySubscribeService;
    @Value("${casChannelNotDisplay}")
    private Boolean notDisplay;
    @Autowired
    private LocalTokenManageService localTokenManageService;

    private static Interner<String> pool = Interners.newWeakInterner();

    @Override
    @Async("taskExecutor")
    public void scheduleChildConference(ScheduleMeetingRequest scheduleMeetingRequest, int cascadeNum, String conferenceId, ChildNodeInfos childNodeInfos, String accessCode, String token) {
        ScheduleMeetingRequestEx copy = new ScheduleMeetingRequestEx();
        BeanUtils.copyProperties(scheduleMeetingRequest, copy);
        copy.setCascadeNum(cascadeNum);
        copy.setGwId(localTokenManageService.getRealLocalGwIdByToken(token));
        copy.setParticipants(childNodeInfos.getParticipants());
        copy.setIsVmr(childNodeInfos.getIsVmr());
        copy.setChild(childNodeInfos.getChild());
        copy.setAccessCode(accessCode);
        //去掉下级不要的参数
        copy.delRecordParam();//去掉录播会议
        List<AttendeeReq> attendees = childNodeInfos.getAttendees();
        if (attendees != null && !attendees.isEmpty()) {
            copy.setAttendees(attendees);
        }else{
            copy.setAttendees(new ArrayList<>());//去掉与会人
        }
        GwId id = childNodeInfos.getGwId();
        copy.setTargetGwId(id);
        //如果下级为虚拟节点，则会议名称前缀需要改为虚拟节点名称
        String name = nodeManageService.getNodeNameByGwId(id.getNodeId());
        if(name != null){
            copy.setVmNodeName(name);
        }
        try {

            if (id.inComplete()) {
                GwId completeGwId = routManageService.getCompleteGwIdBy(id);
                if (completeGwId != null) {
                    id = completeGwId;
                } else {
                    throw new MyHttpException(HttpStatus.NOT_ACCEPTABLE.value(), "smc node not found.");
                }
            }
//            GwId realId = routManageService.getWayByGwId(id);
//            if (realId == null) {
//                //realId为空时，说明该子会议是在上级
//                realId = id;
//            }
            String json = remoteGwService.toByGwId(id).post(ConfApiUrl.CONFERENCES.value(), copy).getBody();
            afterScheduleChildMeeting(json, conferenceId, cascadeNum, scheduleMeetingRequest, accessCode, id, copy.getSubject(), token);
        } catch (Exception e) {
            //TODO 失败的处理逻辑
            e.getStackTrace();
        }

    }

    @Override
    public void afterScheduleChildMeeting(String responseBody, String conferenceId, int cascadeNum,
                                          ScheduleMeetingRequest scheduleMeetingRequest,
                                          String accessCode, GwId childNodeGwId,
                                          String subject, String token) {
        ScheduleMeetingResponseEx scheduleMeetingResponseEx = JSON.parseObject(responseBody, ScheduleMeetingResponseEx.class);
        String childConferenceId = scheduleMeetingResponseEx.getConferenceId();
        String childAccessCode = scheduleMeetingResponseEx.getAccessCode();
        //判断会议是否存在 不存在需要删除
        if (meetingInfoManagerService.contains(conferenceId) || !scheduleMeetingRequest.instantConferenceFlag()) {
            MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
            if (scheduleMeetingRequest.instantConferenceFlag()) {
                //缓存下级会议的会议id
                meetingInfoManagerService.createChild(conferenceId, childConferenceId, scheduleMeetingResponseEx.getAccessCode(), subject, childNodeGwId);

                if(meetingInfo.getSourceGwId() != null) {
                    //推送全量消息

                    ParticipantInfoNotify participantInfoNotify = new ParticipantInfoNotify();
                    participantInfoNotify.setConferenceId(conferenceId);
                    participantInfoNotify.setType(0);
                    participantInfoNotify.setConfCasId(accessCode);

                    Set<GwConferenceId> allChildConferenceIdSet = new HashSet<>();
                    for (ChildMeetingInfo item : meetingInfo.getChildMeetingInfoMap().values()) {
                        GwConferenceId gwConferenceId = new GwConferenceId(item.getId(), item.getConfCasId());
                        allChildConferenceIdSet.add(gwConferenceId);
                        Set<GwConferenceId> childConferenceIdSet = item.getChildConferenceIdSet();
                        allChildConferenceIdSet.addAll(childConferenceIdSet);
                    }
                    participantInfoNotify.setAllChildConferenceIdSet(allChildConferenceIdSet);
                    log.warn("set child confId set: {}", allChildConferenceIdSet);

                    List<ParticipantInfo> changeList = new ArrayList<>();

                    participantInfoNotify.setChangeList(changeList);
                    log.info("send all participant notify.");

                    proxySubscribeService.distributionRemote(meetingInfo.getSourceGwId(), meetingInfo.getDestination(), meetingInfo.getBackDestination(), JSON.toJSONString(participantInfoNotify), meetingInfo.getInfo());
                }

                if (SmcVersionType.V2.equals(scheduleMeetingResponseEx.getSmcVersionType()) && SmcVersionType.V2.equals(SystemConfiguration.getSmcVersion())) {
                    log.info("2.0 to 2.0 add casChannel.");
                    try {
                        addParticipantsToconference(scheduleMeetingRequest, cascadeNum, conferenceId, childNodeGwId, accessCode, token, scheduleMeetingResponseEx);
                    } catch (MyHttpException exception) {
                        try {
                            remoteGwService.toByGwId(childNodeGwId).delete("/conf-portal/online/conferences/" + scheduleMeetingResponseEx.getConference().getId(), null).getBody();
                        } catch (MyHttpException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                }

                //缓存下级会议会场
                //处理订阅
                String subscribeParticipantsDestination = "/topic/conferences/%s/participants/general";
                String subscribeConferencesDestination = "/topic/conferences/%s";
                //查询是否有人订阅了本（非下级）会议 订阅了话需要向下补发订阅
                String[] destinations = new String[]{subscribeParticipantsDestination, subscribeConferencesDestination};
                for (String item : destinations) {
                    String destinationP = String.format(item, conferenceId);
                    String destination = String.format(item, childConferenceId);
                    if (subscribeManageService.hasSubScribe(destinationP)) {
                        log.info("The parent meeting contains a subscription [{}] and the child subscription [{}] needs to be reissued", destinationP, destination);
                        proxySubscribeConferencesService.subscribeChild(childConferenceId,
                                childAccessCode,
                                childNodeGwId,
                                destination,
                                conferenceId,
                                destinationP);
                    }
                }
            }
            if (scheduleMeetingRequest.instantConferenceFlag() && !SystemConfiguration.smcVersionIsV2()) {
                TransactionManage.wait(new TransactionId(TransactionType.CONFERENCES_ONLINE, conferenceId), 1000 * 10);
            }
            if (scheduleMeetingRequest.instantConferenceFlag() && SmcVersionType.V2.equals(scheduleMeetingResponseEx.getSmcVersionType()) && SmcVersionType.V2.equals(SystemConfiguration.getSmcVersion())) {
                return;
            }

            try {
                addParticipantsToconference(scheduleMeetingRequest, cascadeNum, conferenceId, childNodeGwId, accessCode, token, scheduleMeetingResponseEx);
            } catch (MyHttpException exception) {
                try {
                    remoteGwService.toByGwId(childNodeGwId).delete("/conf-portal/online/conferences/" + scheduleMeetingResponseEx.getConference().getId(), null).getBody();
                } catch (MyHttpException e) {
                    e.printStackTrace();
                }
                exception.printStackTrace();
            }

            //下级是否welink
            NodeBusinessType nodeBusinessType = nodeManageService.getNodeBusinessType(childNodeGwId.getNodeId());
            if(NodeBusinessType.WELINK.equals(nodeBusinessType) || NodeBusinessType.CLOUDLINK.equals(nodeBusinessType)){
                meetingInfoManagerService.createChild(conferenceId, childConferenceId, scheduleMeetingResponseEx.getAccessCode(), subject, childNodeGwId);
                meetingInfo.setBSyncBroadcastFlag(false);
            }

        } else {
            try {
                //查找不到当前会议就删除下级的会议
                remoteGwService.toByGwId(childNodeGwId).delete(String.format(ConfApiUrl.DEL_MEETING.value(), scheduleMeetingResponseEx.getConference().getId()), null);
            } catch (MyHttpException e) {
                //TODO 删除失败的处理逻辑
            }
        }
    }

    private void addParticipantsToconference(ScheduleMeetingRequest scheduleMeetingRequest, int cascadeNum, String conferenceId, GwId childNodeGwId, String accessCode, String token, ScheduleMeetingResponseEx scheduleMeetingResponseEx) throws MyHttpException {
        List<ParticipantReq> participantReqs = new ArrayList<>();
        NodeBusinessType nodeBusinessType = nodeManageService.getNodeBusinessType(childNodeGwId.getNodeId());
        boolean isWelink = false;
        if(NodeBusinessType.WELINK.equals(nodeBusinessType) || NodeBusinessType.CLOUDLINK.equals(nodeBusinessType)){
            isWelink = true;
        }
        String remoteNodeName = scheduleMeetingResponseEx.getNodeName();
        if(scheduleMeetingResponseEx.getVmNodeName() != null){
            remoteNodeName = scheduleMeetingResponseEx.getVmNodeName();
        }
        GwId localGwId = localTokenManageService.getRealLocalGwIdByToken(token);
        for (int index = 0; index < cascadeNum; index++) {
            ParticipantReq participantReq = addCasParticipantsHandle(index, cascadeNum, CascadeParticipantDirection.DOWN, scheduleMeetingResponseEx.getSmcVersionType(), remoteNodeName, scheduleMeetingResponseEx.getAccessCode(), childNodeGwId, accessCode, isWelink, localGwId);
            if(isWelink && index == 0){
                participantReq.setDtmfInfo(scheduleMeetingResponseEx.getConference().getChairmanPassword()+"#");
            }
            participantReqs.add(participantReq);
            if(isWelink){
                break;
            }
            if (scheduleMeetingResponseEx.getSmcVersionType().equals(SmcVersionType.V2) && SystemConfiguration.getSmcVersion().equals(SmcVersionType.V2)) {
                break;
            }
        }
        addParticipantsByConferencesType(scheduleMeetingRequest, conferenceId, participantReqs, token);
    }

    public ParticipantReq addCasParticipantsHandle(int index, int cascadeNum,
                                                   CascadeParticipantDirection cascadeParticipantDirection,
                                                   SmcVersionType remoteSmcVersionType, String remoteNodeName,
                                                   String remoteAccessCode,GwId remoteGwId,String accessCode,
                                                   boolean isWelink,GwId localGwId) {
        ParticipantReq participantReq = new ParticipantReq();
        participantReq.setEncodeType("ENCODE_DECODE");
        if (cascadeParticipantDirection.equals(CascadeParticipantDirection.UP)) {
            participantReq.setDialMode("IN");
        } else {
            participantReq.setDialMode("OUT");
        }
        //TODO 临时代码后面删除
//        if (SystemConfiguration.smcVersionIsV3() && remoteSmcVersionType.equals(SmcVersionType.V2)) {
//            participantReq.setDialMode("IN");
//        }
        if (remoteSmcVersionType.equals(SmcVersionType.V2) && SystemConfiguration.getSmcVersion().equals(SmcVersionType.V2)) {
            if(remoteNodeName.length()>64){
                remoteNodeName = remoteNodeName.substring(0, 64);
            }
            participantReq.setName(remoteNodeName);
            participantReq.setUri(remoteAccessCode + "@" + cascadeNum);//上级的主叫号码是本级的被叫
            participantReq.setIpProtocolType(0);
            if (cascadeParticipantDirection.equals(CascadeParticipantDirection.UP)) {
                participantReq.setParticipantType("UpperLevelParticipant");
                participantReq.setDialMode("OUT");
            } else {
                participantReq.setParticipantType("LowerLevelParticipant");
                participantReq.setDialMode("IN");
            }

        } else {
            String channelIndex = "(" + (index + 1) + ")";
            String name = remoteNodeName + channelIndex;
            if(name.length()>64){
                name = remoteNodeName.substring(0, 64-channelIndex.length()) + channelIndex;
            }
            participantReq.setName(name);
            CascadeParticipantParameter cascadeParticipantParameter = new CascadeParticipantParameter(remoteGwId, index, cascadeParticipantDirection);
            String vdcParameter = cascadeParticipantParameter.toString();
            participantReq.setVdcMarkCascadeParticipant(vdcParameter);
            participantReq.setIpProtocolType(1);
            participantReq.setUri(CascadeChannelParameterHandle.getUri(remoteAccessCode, index, remoteGwId, cascadeParticipantDirection));
            //方向取反
            CascadeParticipantDirection cascadeParticipantDirectionReverse;
            if (cascadeParticipantDirection.equals(CascadeParticipantDirection.UP)) {
                cascadeParticipantDirectionReverse = CascadeParticipantDirection.DOWN;
            } else {
                cascadeParticipantDirectionReverse = CascadeParticipantDirection.UP;
            }
            if(isWelink){
                participantReq.setSpecificCallerAlias(welinkMeetingService.getWelinkUriByNumber(accessCode,index,cascadeNum));
            }else {
                participantReq.setSpecificCallerAlias(CascadeChannelParameterHandle.getCustomCallingNum(accessCode, index, remoteSmcVersionType, cascadeParticipantDirectionReverse, localGwId));
            }
            //不加入多画面
            participantReq.setNotJoinAutoMultiPic(true);
            //不是主通道  需要特殊处理
            if (index != 0) {
                participantReq.setNotDisplay(notDisplay);
                participantReq.setDisableAux(true);
                participantReq.setNotAudioMixing(true);
                participantReq.setLockVideoSrc(true);
            }
        }
        return participantReq;
    }


    /**
     * 根据会议类型添加会场
     *
     * @param scheduleMeetingRequest
     * @param conferenceId
     * @param participantReqs
     * @param token
     */
    public void addParticipantsByConferencesType(ScheduleMeetingRequest scheduleMeetingRequest, String conferenceId, List<ParticipantReq> participantReqs, String token) throws MyHttpException {
        if (SystemConfiguration.smcVersionIsV2()) {
            smcMeetingControlService.addParticipants(conferenceId, participantReqs, getSmcToken(token));
        } else {
            if (scheduleMeetingRequest.instantConferenceFlag()) {
                smcMeetingControlService.addParticipants(conferenceId, participantReqs, getSmcToken(token));
            } else {
                if (scheduleMeetingRequest.editConferenceFlag()) {
                    synchronized (conferenceId) {
                        GetOneMeetingResponse getOneMeetingResponse = smcMeetingManagementService.getOne(conferenceId, getSmcToken(token));
                        ModifyMeetingRequest modifyMeetingRequest = getOneMeetingResponse.toModifyMeetingRequest();
                        modifyMeetingRequest.getParticipants().addAll(participantReqs);
                        smcMeetingManagementService.modify(conferenceId, modifyMeetingRequest, getSmcToken(token));
                    }
                }
                if (scheduleMeetingRequest.periodConferenceFlag()) {
                    synchronized (conferenceId) {
                        GetOneMeetingResponse getOneMeetingResponse = smcMeetingManagementService.getOne(conferenceId, getSmcToken(token));
                        ModifyMeetingRequest modifyMeetingRequest = getOneMeetingResponse.toModifyMeetingRequest();
                        modifyMeetingRequest.getParticipants().addAll(participantReqs);
                        ModifyPeriodMeetingRequest modifyPeriodMeetingRequest = JSON.parseObject(JSON.toJSONString(modifyMeetingRequest), ModifyPeriodMeetingRequest.class);
                        smcMeetingManagementService.modifyPeriod(conferenceId, modifyPeriodMeetingRequest, getSmcToken(token));
                    }
                }
            }
        }
    }


    @Async("taskExecutor")
    public void modifyChildConference(ModifyMeetingRequest modifyMeetingRequest, ChildMeetingInfo childMeetingInfo) {
        try {
            remoteGwService.toByGwId(childMeetingInfo.getGwId()).patch(ConfApiUrl.CONFERENCES.value() + childMeetingInfo.getId(), modifyMeetingRequest);
        } catch (MyHttpException e) {
            //TODO 失败的处理逻辑
        }
    }

    @Override
    @Async("taskExecutor")
    public void cancelChildConference(ChildMeetingInfo childMeetingInfo) {
        try {
            remoteGwService.toByGwId(childMeetingInfo.getGwId()).delete(ConfApiUrl.CONFERENCES.value() + childMeetingInfo.getId(), null);
        } catch (MyHttpException e) {
            //TODO 失败的处理逻辑
        }
    }

    @Override
    @Async("taskExecutor")
    public void sendMailChildConference(ChildMeetingInfo childMeetingInfo, SendMeetingMailRequest request) {
        try {
            remoteGwService.toByGwId(childMeetingInfo.getGwId()).post(String.format(ConfApiUrl.SEND_MAIL.value(), childMeetingInfo.getId()), request);
        } catch (MyHttpException e) {
            //TODO 失败的处理逻辑
        }
    }

    @Override
    @Async("taskExecutor")
    public void modifyPeriodChildConference(ChildMeetingInfo childMeetingInfo, ModifyPeriodMeetingRequest request) {
        try {
            remoteGwService.toByGwId(childMeetingInfo.getGwId()).put(String.format(ConfApiUrl.CONFERENCES_PERIOD.value(), childMeetingInfo.getId()), request);
        } catch (MyHttpException e) {
            //TODO 失败的处理逻辑
        }
    }

    @Override
    @Async("taskExecutor")
    public void delPeriodChildConference(ChildMeetingInfo childMeetingInfo) {
        try {
            remoteGwService.toByGwId(childMeetingInfo.getGwId()).delete(String.format(ConfApiUrl.CONFERENCES_PERIOD.value(), childMeetingInfo.getId()), null);
        } catch (MyHttpException e) {
            //TODO 失败的处理逻辑
        }
    }


    @Override
    @Async("taskExecutor")
    public void initCasConf() {
        synchronized (pool.intern("initCasConf")) {
            Map<GwId, List<ChildConferenceWait>> gwIdListMap = meetingInfoManagerService.getChildConferenceWaits();
            for (Map.Entry<GwId, List<ChildConferenceWait>> entry : gwIdListMap.entrySet()) {
                try {
                    GwId gwId = entry.getKey();
                    if (gwId.inComplete()) {
                        gwId = routManageService.getCompleteGwIdBy(gwId);
                        if (gwId == null) {
                            break;
                        }
                    }
                    GwId wayId = routManageService.getWayByGwId(gwId);
                    if (wayId != null) {
                        List<ChildConferenceWait> childConferenceWaits = entry.getValue();
                        //正确 可删除多个
                        Iterator<ChildConferenceWait> iterator = childConferenceWaits.iterator();
                        while (iterator.hasNext()) {
                            ChildConferenceWait item = iterator.next();
                            ChildMeetingInfo childMeetingInfo = meetingInfoManagerService.getChildByCasConfId(item.getId(), item.getChildConfCasId());
                            if (childMeetingInfo == null) {
                                iterator.remove();
                                continue;
                            }
                            if (childMeetingInfo.initialized()) {
                                String subscribeParticipantsDestination = "/topic/conferences/%s/participants/general";
                                String destination = String.format(subscribeParticipantsDestination, childMeetingInfo.getId());
                                proxySubscribeConferencesService.subscribeChild(childMeetingInfo.getId(),
                                        childMeetingInfo.getAccessCode(),
                                        childMeetingInfo.getGwId(),
                                        destination,
                                        item.getId(),
                                        String.format(subscribeParticipantsDestination, item.getId()));
                                iterator.remove();
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("init casConf fail");
                    log.error("exception", e);
                }
            }
        }
    }


    @Override
    @Async("taskExecutor")
    public void resumeLowSubscribe(String nodeId) {
        NodeData nodeData = nodeDataService.getOneById(nodeId);
        if (nodeData == null) {
            log.warn("nodeData is null");
            return;
        }
        log.warn("[resumeLowSubscribe] nodeId {}", nodeId);
        Map<String, MeetingInfo> meetingInfoMap = MeetingInfoManagerServiceImpl.getMeetingInfoMap();
        for (MeetingInfo meetingInfo : meetingInfoMap.values()) {
            log.warn("[resumeLowSubscribe] meetingInfo {}", JSON.toJSONString(meetingInfo));
            for (ChildMeetingInfo childMeetingInfo : meetingInfo.getChildMeetingInfoMap().values()) {
                log.warn("[resumeLowSubscribe] childMeetingInfo {}", JSON.toJSONString(childMeetingInfo));
                if (childMeetingInfo.getGwId().equals(nodeData.toGwId()) && childMeetingInfo.initialized()) {
                    //恢复订阅
                    String subscribeParticipantsDestination = "/topic/conferences/%s/participants/general";
                    String subscribeConferencesDestination = "/topic/conferences/%s";
                    String[] destinations = new String[]{subscribeParticipantsDestination, subscribeConferencesDestination};
                    for (String item : destinations) {
                        String destinationP = String.format(item, meetingInfo.getId());
                        String destination = String.format(item, childMeetingInfo.getId());
                        //if (subscribeManageService.hasSubScribe(destinationP)) {
                        //休眠100毫秒 防止太快
                        CommonHelper.sleep(100);
                        log.info("[resumeLowSubscribe] The parent meeting contains a subscription [{}] and the child subscription [{}] needs to be reissued", destinationP, destination);
                        proxySubscribeConferencesService.subscribeChild(childMeetingInfo.getId(),
                                childMeetingInfo.getAccessCode(),
                                childMeetingInfo.getGwId(),
                                destination,
                                meetingInfo.getId(),
                                destinationP);
                        //  }
                    }
                }
            }
        }
    }

    @Override
    public AddCasChannelResp addCasChannel(AddCasChannelReq addCasChannelReq, String token) throws MyHttpException {
        MeetingInfo meetingInfo = meetingInfoManagerService.get(addCasChannelReq.getConfId());
        int cascadeNum = addCasChannelReq.getCascadeNum();
        List<ParticipantReq> participantReqs = new ArrayList<>();
        for (int index = 0; index < cascadeNum; index++) {
            participantReqs.add(addCasParticipantsHandle(index, cascadeNum, CascadeParticipantDirection.UP,
                    addCasChannelReq.getSmcVersionType(),
                    addCasChannelReq.getNodeName(),
                    addCasChannelReq.getUpAccessCode(),
                    addCasChannelReq.getGwId(), meetingInfo.getAccessCode(),false,nodeDataService.getLocal().toGwId()));
            if (addCasChannelReq.getSmcVersionType().equals(SmcVersionType.V2) && SystemConfiguration.getSmcVersion().equals(SmcVersionType.V2)) {
                break;
            }
        }
        //添加会场
        ScheduleMeetingRequest scheduleMeetingRequest = new ScheduleMeetingRequest();
        ConferenceReq conference = new ConferenceReq();
        conference.setConferenceTimeType("INSTANT_CONFERENCE");
        scheduleMeetingRequest.setConference(conference);
        addParticipantsByConferencesType(scheduleMeetingRequest, addCasChannelReq.getConfId(), participantReqs, token);

        AddCasChannelResp addCasChannelResp = new AddCasChannelResp();
        addCasChannelResp.setRemoteAccessCode(meetingInfo.getAccessCode());
        addCasChannelResp.setRemoteNodeName(nodeDataService.getLocal().getName());
        addCasChannelResp.setRemoteSmcVersionType(SystemConfiguration.getSmcVersion());
        addCasChannelResp.setGwId(nodeDataService.getLocal().toGwId());
        return addCasChannelResp;

    }

    @Override
    @Async("taskExecutor")
    public void CascadeParticipantStatusHandle(String confId, String pid, boolean main, Boolean mute, Boolean quiet, Integer videoSwitchAttribute) {
        boolean needChange = false;
        ParticipantsControlRequest participantsControlRequest = new ParticipantsControlRequest();
        ParticipantInfo participantInfo = participantInfoManagerService.getParticipant(confId, pid);

        if(participantInfo != null && participantInfo.isCascadeParticipantH323()){
            if(main){
                pid += "@1";
            }else {
                return;
            }
        }

        //如果是主级联通道
        if (main) {
            if (mute != null && mute) {
                //如果关闭就打开 false 是打开
                participantsControlRequest.setIsMute(false);
                needChange = true;
            }
            if (quiet != null && quiet) {
                //如果关闭就打开  false 是打开
                participantsControlRequest.setIsQuiet(false);
                needChange = true;
            }
        } else {
            if (mute != null && !mute) {
                //如果打开就关闭  true 是关闭
                participantsControlRequest.setIsMute(true);
                needChange = true;
            }
            if (quiet != null && !quiet) {
                //如果打开就关闭  true 是关闭
                participantsControlRequest.setIsQuiet(true);
                needChange = true;
            }
            if (SystemConfiguration.smcVersionIsV3()) {
                if (videoSwitchAttribute != null && videoSwitchAttribute == 0) {
                    //锁定视频源
                    participantsControlRequest.setVideoSwitchAttribute(1);
                    needChange = true;
                }
            }
        }
        if (needChange) {
            participantsControlRequest.setId(pid);
            try {
                smcMeetingControlService.participantsControl(confId, pid, participantsControlRequest, getSmcToken(CoreConfig.INTERNAL_USER_TOKEN));
            } catch (MyHttpException e) {
                e.printStackTrace();
            }
        }
    }
}

