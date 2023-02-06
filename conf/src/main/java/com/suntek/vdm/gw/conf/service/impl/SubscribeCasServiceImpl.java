package com.suntek.vdm.gw.conf.service.impl;

import com.suntek.vdm.gw.conf.enumeration.SubscribeUserType;
import com.suntek.vdm.gw.conf.pojo.SubscribeInfo;
import com.suntek.vdm.gw.conf.service.*;
import com.suntek.vdm.gw.conf.service.internal.InternalCallBackService;
import com.suntek.vdm.gw.conf.ws.server.WsOperate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;


@Service
@Slf4j
public class SubscribeCasServiceImpl implements SubscribeService {
    @Autowired
    private SubscribeCallBackService subscribeCallBackService;

    @Qualifier("subscribeManageCasServiceImpl")
    @Autowired
    private SubscribeManageService subscribeManageService;

    @Autowired
    @Lazy
    private InternalCallBackService internalCallBackService;

    @Autowired
    @Lazy
    private ProxySubscribeConferencesService proxySubscribeConferencesService;

    @Autowired
    private ParticipantInfoManagerService participantInfoManagerService;

    @Autowired
    private VideoSourceService videoSourceService;

    @Autowired
    private WsOperate wsOperate;


    @Override
    public void conferencesStatus(String subId, String user, String sessionId) {
        String backDestination = "/conferences/status";
        String subscribeDestination = "/topic" + backDestination;
        log.info("Subscribe destination:{} user:{}", subscribeDestination, user);
        SubscribeUserType userType = subscribeManageService.getSubscribeInfo(sessionId).getType();
        StompFrameHandler stompFrameHandler = new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                try {
                    String body = null;
                    if(!(payload instanceof String)){
                        body = new String((byte[]) payload);
                    }else{
                        body = (String) payload;
                    }
                    //推送给用户需要去掉topic
                    if (userType.equals(SubscribeUserType.LOCAL)) {
                        subscribeCallBackService.callback(user, headers.getSubscription(), backDestination, body, headers.toSingleValueMap());
                    } else if (userType.equals(SubscribeUserType.INTERNAL)) {
                        internalCallBackService.callBackController(headers.getSubscription(), backDestination, body, backDestination);
                    }
                } catch (Exception e) {
                    log.error("message handle frame exception：{}", e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        //获取会议的token
        StompHeaders stompHeaders = new StompHeaders();
        subscribeManageService.subscribe(sessionId, subId, subscribeDestination, backDestination, stompFrameHandler, stompHeaders);
    }

    /**
     * 订阅会控状态
     *
     * @param conferenceId
     * @param sessionId
     */
    @Override
    public void conferencesControllerStatus(String conferenceId, String conferencesToken, @Nullable String subId, String user, String sessionId, boolean autoChild) {
        String destination = "/conferences/%s";
        conferencesSubscribeHandler(destination, conferenceId, conferencesToken, subId, user, sessionId, autoChild);
    }

    /**
     * 订阅会场状态
     *
     * @param conferenceId
     */
    @Override
    public void conferencesParticipantsStatus(String conferenceId, String conferencesToken, @Nullable String subId, String user, String sessionId, boolean autoChild) {
        String destination = "/conferences/%s/participants/general";
        conferencesSubscribeHandler(destination, conferenceId, conferencesToken, subId, user, sessionId, autoChild);
    }


    @Override
    public void meetingRoomStatus(String subId, String user, String sessionId) {
        String backDestination = "/meetingRoomStatus";
        String subscribeDestination = "/topic" + backDestination;
        log.debug("Subscribe destination:{} user:{}", subscribeDestination, user);
        SubscribeUserType userType = subscribeManageService.getSubscribeInfo(sessionId).getType();
        StompFrameHandler stompFrameHandler = new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                try {
                    String body = null;
                    if(!(payload instanceof String)){
                        body = new String((byte[]) payload);
                    }else{
                        body = (String) payload;
                    }
                    //推送给用户需要去掉topic
                    if (userType.equals(SubscribeUserType.LOCAL)) {
                        subscribeCallBackService.callback(user, headers.getSubscription(), backDestination, body, headers.toSingleValueMap());
                    } else if (userType.equals(SubscribeUserType.INTERNAL)) {
                        internalCallBackService.callBackController(headers.getSubscription(), backDestination, body, backDestination);
                    }
                } catch (Exception e) {
                    log.error("message handle frame exception：", e);
                }
            }
        };
        StompHeaders stompHeaders = new StompHeaders();
        subscribeManageService.subscribe(sessionId, subId, subscribeDestination, backDestination, stompFrameHandler, stompHeaders);
    }


    /**
     * 会议订阅的公共处理
     * 下级会自动订阅
     *
     * @param destination
     * @param conferenceId
     * @param conferencesToken
     * @param subId
     * @param user
     * @param sessionId
     */
    public void conferencesSubscribeHandler(String destination, String conferenceId, String conferencesToken, @Nullable String subId, String user, String sessionId, boolean autoChild) {
        String topic = "/topic";
        String backDestination = String.format(destination, conferenceId);
        String subscribeDestination = topic + backDestination;
        log.info("Subscribe destination:{} user:{}", subscribeDestination, user);
        SubscribeInfo subscribeInfo = subscribeManageService.getSubscribeInfo(sessionId);
        if(subscribeInfo == null){
            wsOperate.closeUser(sessionId);
            log.info("Subscribe sessionId to get SubscribeInfo is empty: {}", sessionId);
            return;
        }
        SubscribeUserType userType = subscribeManageService.getSubscribeInfo(sessionId).getType();
        StompFrameHandler stompFrameHandler = new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                try {
                    String body = null;
                    if(!(payload instanceof String)){
                        body = new String((byte[]) payload);
                    }else{
                        body = (String) payload;
                    }

                    //推送给用户需要去掉topic
                    if (userType.equals(SubscribeUserType.LOCAL)) {
                        if (destination.contains("/participants/general")) {
                            body = videoSourceService.changeSource(body);
                        }
                        List<String> list = headers.get("conferenceId");
                        if(list == null || list.isEmpty()){
                            headers.set("conferenceId", conferenceId);
                        }
                        subscribeCallBackService.callback(user, headers.getSubscription(), backDestination, body, headers.toSingleValueMap());
                    } else if (userType.equals(SubscribeUserType.INTERNAL)) {
                        internalCallBackService.callBackController(headers.getSubscription(), backDestination, body, backDestination);
                    }
                } catch (Exception e) {
                    log.error("message handle frame exception：", e);
                }
            }
        };
        //获取会议的token
        StompHeaders headers = new StompHeaders();
        headers.add("token", conferencesToken);
        subscribeManageService.subscribe(sessionId, subId, subscribeDestination, backDestination, stompFrameHandler, headers);
        //自动订阅下级
        if (autoChild) {
            proxySubscribeConferencesService.subscribeChild(conferenceId, subscribeDestination);
        }
    }


    /**
     * 取消订阅
     *
     * @param sessionId
     * @param sunId
     * @param user
     */
    public void unSubscribe(String sessionId, String sunId, String user) {
        String destination = subscribeManageService.getDestination(sessionId, sunId);
        log.info("User {} unSubScribe by destination {}", user, destination);
        //先取消本级的订阅
        subscribeManageService.unSubscribe(sessionId, sunId);
        if(destination == null){
            return;
        }
        //处理下级的订阅
        String[] destinationSplit = destination.split("/");
        if (destinationSplit.length < 3) {
            return;
        } else {
            String conferenceId = destinationSplit[3];
            if (conferenceId.equals("status")) {
                return;
            } else {
                proxySubscribeConferencesService.unSubscribeChild(conferenceId, destination);
            }
        }
    }
}