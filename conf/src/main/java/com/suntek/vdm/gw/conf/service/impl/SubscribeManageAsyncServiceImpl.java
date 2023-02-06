package com.suntek.vdm.gw.conf.service.impl;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.conf.enumeration.SubscribeUserType;
import com.suntek.vdm.gw.conf.pojo.ReSubscribeInfo;
import com.suntek.vdm.gw.conf.pojo.SubscribeInfo;
import com.suntek.vdm.gw.conf.service.SubscribeManageAsyncService;
import com.suntek.vdm.gw.conf.service.SubscribeManageService;
import com.suntek.vdm.gw.conf.service.internal.InternalLinkService;
import com.suntek.vdm.gw.conf.ws.server.WsOperate;
import com.suntek.vdm.gw.core.pojo.LocalToken;
import com.suntek.vdm.gw.core.service.LocalTokenManageService;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.core.task.TaskManage;
import com.suntek.vdm.gw.smc.service.SmcOtherService;
import com.suntek.vdm.gw.smc.service.SmcSubscribeService;
import com.suntek.vdm.gw.smc.ws.stomp.MyStompSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SubscribeManageAsyncServiceImpl implements SubscribeManageAsyncService {

    @Autowired
    private SubscribeManageService subscribeManageService;

    @Async("taskExecutor")
    public void reconnect(MyStompSession myStompSession, StompSessionHandlerAdapter stompSessionHandlerAdapter) {
        subscribeManageService.reconnect(myStompSession,stompSessionHandlerAdapter);
    }
}
