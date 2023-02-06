package com.suntek.vdm.gw.conf.service;

import com.suntek.vdm.gw.smc.ws.stomp.MyStompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

public interface SubscribeManageAsyncService {
    void reconnect(MyStompSession myStompSession, StompSessionHandlerAdapter stompSessionHandlerAdapter);
}
