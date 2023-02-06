package com.suntek.vdm.gw.conf.service.impl;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.api.request.MeetingControlRequest;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.CascadeChannelNotifyInfo;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.common.pojo.websocket.CascadeChannelMessage;
import com.suntek.vdm.gw.common.pojo.websocket.MessageContent;
import com.suntek.vdm.gw.common.pojo.websocket.SubscribeMessage;
import com.suntek.vdm.gw.common.util.MessageLogUtil;
import com.suntek.vdm.gw.common.util.SystemConfiguration;
import com.suntek.vdm.gw.conf.pojo.MeetingInfo;
import com.suntek.vdm.gw.conf.service.*;
import com.suntek.vdm.gw.core.service.RoutManageService;
import com.suntek.vdm.gw.core.service.WebsocketClientService;
import com.suntek.vdm.gw.smc.service.SmcMeetingControlService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.Session;

@Service
@Slf4j
public class WebsocketServerServiceImpl extends BaseServiceImpl implements WebsocketServerService {

    @Autowired
    private ProxySubscribeService proxySubscribeService;
    @Autowired
    private WebsocketClientService websocketClientService;
    @Autowired
    private RoutManageService routManageService;
    @Autowired
    private CascadeChannelNotifyService cascadeChannelNotifyService;
    @Autowired
    private SmcMeetingControlService smcMeetingControlService;
    @Autowired
    private MeetingControlService meetingControlService;
    @Autowired
    private MeetingInfoManagerService meetingInfoManagerService;


    @Override
    public void messageHandle(String message, Session session, String nodeId) {
        if (StringUtils.isNotBlank(message)) {
            MessageContent messageContent = JSON.parseObject(message, MessageContent.class);
            MessageLogUtil.callbackRemote(nodeId, message);
            switch (messageContent.getType()) {
                case SUBSCRIBE_MESSAGE: {
                    subscribeMessageHandle(messageContent.getBody());
                    break;
                }
                case ADD_WELINK_CASCADE_CHANNEL: {
                    addWelinkCascadeChannel(messageContent.getBody());
                    break;
                }
                case NOTIFY_SOURCE: {
                    notifyWelinkCascadeSourceChannel(messageContent.getBody());
                    break;
                }
                case CANCEL_BROADCAST:{
                    cancelBroadCast(messageContent.getBody());
                    break;
                }
                default:
            }
        }
    }

    private void cancelBroadCast(String body){
        CascadeChannelNotifyInfo cascadeChannelNotifyInfo = JSON.parseObject(body, CascadeChannelNotifyInfo.class);
        MeetingInfo meetingInfo = meetingInfoManagerService.getByCasConfId(cascadeChannelNotifyInfo.getConfCasId());
        MeetingControlRequest meetingControlRequest = new MeetingControlRequest();
        meetingControlRequest.setBroadcaster("");
        try {
            smcMeetingControlService.meetingControl(meetingInfo.getId(), meetingControlRequest, getSmcToken(CoreConfig.INTERNAL_USER_TOKEN));
        } catch (MyHttpException e) {
            e.printStackTrace();
        }
        try {
            meetingControlService.sendChild(meetingInfo.getId(), meetingControlRequest);
            meetingControlService.sendTop(meetingInfo.getId(), meetingControlRequest);
        } catch (MyHttpException exception) {
            exception.printStackTrace();
        }

    }

    private void notifyWelinkCascadeSourceChannel(String body) {
        CascadeChannelNotifyInfo cascadeChannelNotifyInfo = JSON.parseObject(body, CascadeChannelNotifyInfo.class);
        cascadeChannelNotifyService.notifyHandle(cascadeChannelNotifyInfo);
    }

    public void subscribeMessageHandle(String body) {
        SubscribeMessage subscribeMessage = JSON.parseObject(body, SubscribeMessage.class);
//        MessageLogUtil.callbackRemote(subscribeMessage.getTargetGwId().toString(), body);
        if (routManageService.isLocal(subscribeMessage.getSourceGwId())) {
            proxySubscribeService.distributionUser(subscribeMessage);
        } else {
            websocketClientService.pushSubscribeMessage(subscribeMessage, subscribeMessage.getSourceGwId());
        }
    }

    private void addWelinkCascadeChannel(String body) {
        CascadeChannelMessage subscribeMessage = JSON.parseObject(body, CascadeChannelMessage.class);
        proxySubscribeService.addWelinkCascadeChannel(subscribeMessage);
    }
}
