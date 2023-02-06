package com.suntek.vdm.gw.conf.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import com.suntek.vdm.gw.common.enums.SmcVersionType;
import com.suntek.vdm.gw.common.pojo.*;
import com.suntek.vdm.gw.common.pojo.response.meeting.GetParticipantsResponse;
import com.suntek.vdm.gw.common.pojo.websocket.CascadeChannelMessage;
import com.suntek.vdm.gw.common.pojo.websocket.SubscribeAttachInfo;
import com.suntek.vdm.gw.common.pojo.websocket.SubscribeBusinessType;
import com.suntek.vdm.gw.common.pojo.websocket.SubscribeMessage;
import com.suntek.vdm.gw.common.util.CommonHelper;
import com.suntek.vdm.gw.common.util.TransactionManage;
import com.suntek.vdm.gw.conf.enumeration.SubscribeUserType;
import com.suntek.vdm.gw.conf.pojo.*;
import com.suntek.vdm.gw.conf.service.*;
import com.suntek.vdm.gw.conf.service.internal.InternalCallBackService;
import com.suntek.vdm.gw.conf.ws.client.CustomStompSessionHandler;
import com.suntek.vdm.gw.core.pojo.LocalToken;
import com.suntek.vdm.gw.core.service.LocalTokenManageService;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.core.service.RoutManageService;
import com.suntek.vdm.gw.core.service.WebsocketClientService;
import com.suntek.vdm.gw.smc.service.SmcMeetingControlService;
import com.suntek.vdm.gw.smc.service.SmcOtherService;
import com.suntek.vdm.gw.welink.pojo.WelinkNodeData;
import com.suntek.vdm.gw.welink.service.impl.WelinkMeetingManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ProxySubscribeServiceImpl extends BaseServiceImpl implements ProxySubscribeService {
    @Autowired
    private SubscribeCallBackService subscribeCallBackService;
    @Autowired
    private SmcOtherService smcOtherService;
    @Autowired
    private SubscribeManageService subscribeManageService;
    @Autowired
    private WebsocketClientService websocketClientService;
    @Autowired
    private MeetingManagerService meetingManagerService;
    @Autowired
    private MeetingInfoManagerService meetingInfoManagerService;
    @Autowired
    private SmcMeetingControlService smcMeetingControlService;
    @Autowired
    private MeetingService meetingService;
    @Autowired
    private LocalTokenManageService localTokenManageService;
    @Autowired
    private WelinkMeetingManagerService welinkMeetingManagerService;
    @Autowired
    @Lazy
    private ProxySubscribeConferencesService proxySubscribeConferencesService;
    @Autowired
    @Lazy
    private InternalCallBackService internalCallBackService;
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    @Lazy
    private CustomStompSessionHandler customStompSessionHandler;
    @Autowired
    private VideoSourceService videoSourceService;
    @Autowired
    private RoutManageService routManageService;
    @Autowired
    private ParticipantInfoManagerService participantInfoManagerService;

    private static Interner<String> pool = Interners.newWeakInterner();


    public void connect(String sourceNodeId, String token, GwId sourceGwId) throws MyHttpException {
        if(routManageService.isLocal(sourceGwId)){
            token = CoreConfig.INTERNAL_USER_TOKEN;
        }
        LocalToken localToken = localTokenManageService.get(token);
        if(localToken == null){
            log.info("localToken is empty.");
            throw new MyHttpException(HttpStatus.UNAUTHORIZED.value(), "localToken is empty.");
        }
        //需要锁住防止同一个节点并发订阅开多个websocket
        synchronized (pool.intern(sourceNodeId)) {
            for (int i = 0; i < 3; i++) {
                SubscribeInfo subscribeInfo = null;
                boolean resubscribeFlag = false;
                try {
                    subscribeInfo = subscribeManageService.getSubscribeInfo(sourceNodeId);
                    if (subscribeInfo != null) {
                        //token换过需要重新订阅
                        if (!subscribeInfo.getToken().equals(token)) {
                            resubscribeFlag = true;
                        }
                    }
                } catch (Exception e) {

                }
                //未订阅先检查是不是在发送给本级的
                if (!subscribeManageService.isOpen(sourceNodeId) || resubscribeFlag) {
                    subscribeManageService.disconnect(sourceNodeId);
                    log.info("Proxy subscribe webSocket not open");
                    try {
                        String tickets = smcOtherService.getMeetingTickets(localToken.getUsername(), localToken.getSmcToken());
                        //延迟200毫秒
                        CommonHelper.sleep(200 * i);
                        boolean openFlag = subscribeManageService.connect(sourceNodeId, sourceGwId.toString(), SubscribeUserType.REMOTE, tickets, token, customStompSessionHandler);
                        if (openFlag) {
                            break;
                        }
                    } catch (MyHttpException e) {
                        log.error("Get Meeting Tickets fail,error：{}", e.toString());
                    }
                } else {
                    break;
                }
            }
        }
    }

    @Override
    public void subscribe(GwId sourceGwId, String destination, String backDestination, SubscribeAttachInfo info, String token) throws MyHttpException {
        try {
            subscribeAction(sourceGwId, destination, backDestination, info, token);
        }catch (MyHttpException e){
            log.info("Proxy subscribe exception destination:{} exception:{}", destination, e);
            throw e;
        }
    }

    public void subscribeAction(GwId sourceGwId, String destination, String backDestination, SubscribeAttachInfo info, String token) throws MyHttpException {
        log.info("Proxy subscribe destination:{} gwHttpId:{}", destination, token);
        if (false) {
            //已经订阅不重新订阅
            log.info("Already subscribe");
        } else {
            //先连接
            connect(sourceGwId.getNodeId(), token, sourceGwId);
            StompFrameHandler stompFrameHandler = new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return byte[].class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    try {
                        String body = null;
                        if(payload instanceof String){
                            body = (String) payload;
                        }else{
                            body = new String((byte[]) payload);
                        }
                        distributionRemote(sourceGwId, destination, backDestination, body, info);
                    } catch (Exception e) {
                        log.error("message handle frame exception：", e);
                    }
                }
            };
            StompHeaders headers = new StompHeaders();
            //判断是不是会议订阅
            if (info.getBusinessType().equals(SubscribeBusinessType.CONFERENCES)) {
                ConferencesSubscribeAttachInfo conferencesSubscribeAttachInfo = JSON.parseObject(info.getData(), ConferencesSubscribeAttachInfo.class);
                String conferenceId = conferencesSubscribeAttachInfo.getConferenceId();
                if (!meetingInfoManagerService.isOnline(conferenceId)) {
                    TransactionManage.wait(new TransactionId(TransactionType.CONFERENCES_ONLINE, conferenceId), 1000 * 10);
                }
                String confCasId = conferencesSubscribeAttachInfo.getAccessCode();
                GetParticipantsResponse getParticipantsResponse = smcMeetingControlService.getParticipants(conferenceId, 0, 1000, null, getSmcToken(token));
                if (getParticipantsResponse.exist()) {
                    ParticipantInfoNotify participantInfoNotify = new ParticipantInfoNotify();
                    participantInfoNotify.setConferenceId(conferenceId);
                    participantInfoNotify.setType(0);
                    participantInfoNotify.setConfCasId(confCasId);
                    MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
                    if(meetingInfo == null){
                        log.error("meetingInfo can not fdound: {}", conferenceId);
                        return;
                    }
                    Set<GwConferenceId> allChildConferenceIdSet = new HashSet<>();
                    if(meetingInfo.getChildMeetingInfoMap() != null){
                        for (ChildMeetingInfo item : meetingInfo.getChildMeetingInfoMap().values()) {
                            GwConferenceId gwConferenceId = new GwConferenceId(item.getId(), item.getConfCasId());
                            allChildConferenceIdSet.add(gwConferenceId);
                            Set<GwConferenceId> childConferenceIdSet = item.getChildConferenceIdSet();
                            allChildConferenceIdSet.addAll(childConferenceIdSet);
                        }
                    }
                    participantInfoNotify.setAllChildConferenceIdSet(allChildConferenceIdSet);
                    log.warn("set child confId set: {}", allChildConferenceIdSet);

                    List<ParticipantInfo> changeList = new ArrayList<>();
                    for (ParticipantDetail item : getParticipantsResponse.getContent()) {
                        changeList.add(ParticipantInfo.valueOf(item, conferenceId));
                        if(item.getGeneralParam().getSubTpParams() != null){
                            initTPParticipantInfo(conferenceId, changeList, item);
                        }
                    }
                    participantInfoNotify.setChangeList(changeList);
                    log.info("send all participant notify.");
                    meetingInfo.setSubscribeInfo(sourceGwId, destination, backDestination,info);
                    distributionRemote(sourceGwId, destination, backDestination, JSON.toJSONString(participantInfoNotify), info);
                }
                String conferencesToken = meetingManagerService.getToken(conferenceId, token);
                headers.set("token", conferencesToken);
                //超时保护
                if (!meetingInfoManagerService.isOnline(conferenceId)) {
                    meetingInfoManagerService.onlineMeeting(conferenceId);
                }
            }
            subscribeManageService.subscribe(sourceGwId.getNodeId(), destination, destination, backDestination, stompFrameHandler, headers);
            //判断是不是会议订阅
            if (info.getBusinessType().equals(SubscribeBusinessType.CONFERENCES)) {
                ConferencesSubscribeAttachInfo conferencesSubscribeAttachInfo = JSON.parseObject(info.getData(), ConferencesSubscribeAttachInfo.class);
                String conferenceId = conferencesSubscribeAttachInfo.getConferenceId();
                proxySubscribeConferencesService.subscribeChild(conferenceId, destination);
            }
        }
    }

    private void initTPParticipantInfo(String conferenceId, List<ParticipantInfo> changeList, ParticipantDetail participantDetail) {
        for(TpGeneralParam tpGeneralParam: participantDetail.getGeneralParam().getSubTpParams()){
            ParticipantInfo tpParticipantInfo = new ParticipantInfo();
            tpParticipantInfo.setConferenceId(conferenceId);
            tpParticipantInfo.setParticipantId(tpGeneralParam.getId());
            tpParticipantInfo.setUri(tpGeneralParam.getUri());
            tpParticipantInfo.setName(tpGeneralParam.getName());
            tpParticipantInfo.setOnline(false);
            if(participantDetail.getSubTpState() != null){
                for(ParticipantState participantState: participantDetail.getSubTpState()){
                    if(!participantState.getParticipantId().equals(tpGeneralParam.getId())){
                        continue;
                    }
                    tpParticipantInfo.setMultiPicInfo(participantState.getMultiPicInfo());
                    tpParticipantInfo.setOnline(participantState.getOnline());
                }
            }
            changeList.add(tpParticipantInfo);
        }
    }



    @Override
    public void unSubscribe(GwId sourceGwId, String destination, String backDestination, SubscribeAttachInfo info, String token) {
        String subId = destination;
        subscribeManageService.unSubscribe(sourceGwId.getNodeId(), subId);
        if (info.getBusinessType().equals(SubscribeBusinessType.CONFERENCES)) {
            //查询此会议的下级会议是否还有人订阅  没有的话继续取消
            ConferencesSubscribeAttachInfo conferencesSubscribeAttachInfo = JSON.parseObject(info.getData(), ConferencesSubscribeAttachInfo.class);
            String conferenceId = conferencesSubscribeAttachInfo.getConferenceId();
            proxySubscribeConferencesService.unSubscribeChild(conferenceId, destination);
        }
    }

    /**
     * 消息分发给节点
     *
     * @param sourceGwId
     * @param destination
     * @param backDestination
     * @param message
     */
    public void distributionRemote(GwId sourceGwId, String destination, String backDestination, String message, SubscribeAttachInfo info) {
        GwId targetGwId = nodeDataService.getLocal().toGwId();
        if (info.getBusinessType().equals(SubscribeBusinessType.CONFERENCES)) {
            //会议订阅需要加入级联会议号
            JSONObject outputObj = JSON.parseObject(message);
            ConferencesSubscribeAttachInfo conferencesSubscribeAttachInfo = JSON.parseObject(info.getData(), ConferencesSubscribeAttachInfo.class);
            String accessCode = conferencesSubscribeAttachInfo.getAccessCode();
            outputObj.put("confCasId", accessCode);
            //改名
            try {
                videoSourceService.changeSource(outputObj);
            }catch (Exception e){
                log.info("Exception: {}", e.getStackTrace());
            }
            message = JSON.toJSONString(outputObj, SerializerFeature.DisableCircularReferenceDetect);
        }
        SubscribeMessage subscribeMessagePush = new SubscribeMessage(destination,
                backDestination,
                sourceGwId,
                targetGwId,
                message,
                info);
        websocketClientService.pushSubscribeMessage(subscribeMessagePush, subscribeMessagePush.getSourceGwId());
    }

    @Override
    public void distributionUser(SubscribeMessage subscribeMessage) {
        distributionUser(subscribeMessage, null);
    }

    /**
     * 消息分发给用户
     *
     * @param message
     * @param userTypeBlackList 用户类型白名单 指定那些用户不推送
     */
    @Override
    public void distributionUser(SubscribeMessage message, List<SubscribeUserType> userTypeBlackList) {
        //获取那些用户订阅了此条消息 本级消息
        List<SubscribeDestinationUserInfo> subscribeDestinationUserInfos = subscribeManageService.getDestinationUser(message.getBackDestination());
        if (subscribeDestinationUserInfos != null) {
            GwId targetGwId = nodeDataService.getLocal().toGwId();
            for (SubscribeDestinationUserInfo user : subscribeDestinationUserInfos) {
                SubscribeMessage subscribeMessage = JSON.parseObject(JSON.toJSONString(message), SubscribeMessage.class);
                if (userTypeBlackList != null) {
                    if (userTypeBlackList.contains(user.getType())) {
                        continue;
                    }
                }
                //判断用户类型
                switch (user.getType()) {
                    case LOCAL: {
                        Map<String, String> headers = new HashMap<>();
                        if (subscribeMessage.getInfo().getBusinessType().equals(SubscribeBusinessType.CONFERENCES)) {
                            ConferencesSubscribeAttachInfo conferencesSubscribeAttachInfo = JSON.parseObject(subscribeMessage.getInfo().getData(), ConferencesSubscribeAttachInfo.class);
                            headers.put("conferenceId", conferencesSubscribeAttachInfo.getConferenceId());
                            String participantsPattern = "/conferences/.*?/participants/general";
                            boolean participantsIsMatch = Pattern.matches(participantsPattern, user.getBackDestination());
                            if (participantsIsMatch) {
                                JSONObject jsonObject = JSON.parseObject(subscribeMessage.getMessage());
                                Integer type = jsonObject.getInteger("type");
                                //会场全量通知
                                if (type == 0) {
                                    jsonObject.put("type", 3);
//                                    subscribeCallBackService.callback(user.getUser(), user.getSubId(), user.getBackDestination(), subscribeMessage.getMessage(), headers);
//                                    jsonObject.put("type", 1);
//                                    subscribeMessage.setMessage(jsonObject.toJSONString());
                                }
                            }
                        }
                        subscribeCallBackService.callback(user.getUser(), user.getSubId(), user.getBackDestination(), subscribeMessage.getMessage(), headers);
                        break;
                    }
                    case REMOTE: {
                        String participantsPattern = "/conferences/.*?/participants/general";
                        boolean participantsIsMatch = Pattern.matches(participantsPattern, user.getBackDestination());
                        if (participantsIsMatch) {
                            JSONObject jsonObject = JSON.parseObject(subscribeMessage.getMessage());
                            Integer type = jsonObject.getInteger("type");
                            //会场全量通知
                            if (type == 0) {
                                String[] strings = user.getBackDestination().split("/");
                                String confId = strings[2];
                                MeetingInfo meetingInfo = meetingInfoManagerService.get(confId);
                                if(meetingInfo != null){
                                    ParticipantInfoNotify participantStatusNotify = JSON.parseObject(subscribeMessage.getMessage(), ParticipantInfoNotify.class);
                                    participantStatusNotify.getAllChildConferenceIdSet().add(new GwConferenceId(confId, meetingInfo.getAccessCode()));
                                    subscribeMessage.setMessage(JSON.toJSONString(participantStatusNotify));
                                }
                            }
                        }
                        SubscribeMessage subscribeMessagePush = new SubscribeMessage(subscribeMessage.getBackDestination(),
                                user.getBackDestination(),
                                GwId.valueOf(user.getUser()),
                                targetGwId,
                                subscribeMessage.getMessage(),
                                subscribeMessage.getInfo());
                        websocketClientService.pushSubscribeMessage(subscribeMessagePush, subscribeMessagePush.getSourceGwId());
                        break;
                    }
                    case INTERNAL: {
                        internalCallBackService.callBackController(user.getSubId(), subscribeMessage.getDestination(), subscribeMessage.getMessage(), user.getBackDestination());
                        break;
                    }
                    default:
                        break;
                }
            }
        } else {
            //TODO 订阅为空为异常 需要处理
        }
    }

    @Override
    public synchronized void addWelinkCascadeChannel(CascadeChannelMessage message) {
        String smcAccessCode = message.getSmcAccessCode();
        MeetingInfo localMeeting = meetingInfoManagerService.getByCasConfId(smcAccessCode);
        if (localMeeting != null) {
            String conferenceId = localMeeting.getId();
            String nodeName = message.getNodeName();
            String accessCode = localMeeting.getAccessCode();
            Integer cascadeNum = message.getCascadeNum();
            List<ParticipantReq> participantReqs = new ArrayList<>();
            WelinkNodeData welinkNodeData = welinkMeetingManagerService.getWelinkNodeData();
            int currentnumber = localMeeting.getCuurrentCasChannelNum(message.getWelinkAccessCode());
            if(currentnumber + cascadeNum > message.getCurrentTotalNum()){
                return;
            }
            int start = currentnumber;
            for (int i = 0; i < cascadeNum; i++) {
                ParticipantReq participantReq = meetingService.addCasParticipantsHandle(start, message.getCurrentTotalNum(), CascadeParticipantDirection.DOWN,
                        SmcVersionType.Welink, nodeName, message.getWelinkAccessCode(), welinkNodeData.getGwId(),
                        accessCode, true,nodeDataService.getLocal().toGwId());
                participantReq.setDtmfInfo(message.getGuestPwd()+"#");
                participantReqs.add(participantReq);
                start++;
            }
            try {
                smcMeetingControlService.addParticipants(conferenceId, participantReqs, getSmcToken(CoreConfig.INTERNAL_USER_TOKEN));
                log.info("创建welink级联通道成功");
            } catch (MyHttpException e) {
                log.error("创建welink级联通道失败");
            }
        }
    }
}
