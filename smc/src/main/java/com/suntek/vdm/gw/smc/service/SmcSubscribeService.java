package com.suntek.vdm.gw.smc.service;

import com.suntek.vdm.gw.smc.ws.stomp.MyStompSession;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

public interface SmcSubscribeService {

    String connect(String authorization, StompSessionHandlerAdapter stompSessionHandlerAdapter);

    String connect(String username, String tickets, String token, StompSessionHandlerAdapter stompSessionHandlerAdapter);

    void disconnect(String sessionId);

    void subscribe(String sessionId, String destination, String subId, StompFrameHandler handler, StompHeaders headers);

    void unSubscribe(String sessionId, String subId);

    boolean isOpen(String sessionId);
}
