package com.suntek.vdm.gw.conf.service;

import com.suntek.vdm.gw.conf.enumeration.SubscribeUserType;
import com.suntek.vdm.gw.conf.pojo.ReSubscribeInfo;
import com.suntek.vdm.gw.conf.pojo.SubscribeDestinationUserInfo;
import com.suntek.vdm.gw.conf.pojo.SubscribeInfo;
import com.suntek.vdm.gw.smc.ws.stomp.MyStompSession;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.util.List;
import java.util.Queue;

public interface SubscribeManageService {
    SubscribeInfo getSubscribeInfo(String sessionId);

    boolean connect(String sessionId, String user, SubscribeUserType type, String authorization, StompSessionHandlerAdapter stompSessionHandlerAdapter);

    boolean connect(String sessionId, String user, SubscribeUserType type, String tickets, String token,StompSessionHandlerAdapter stompSessionHandlerAdapter);

    void disconnect(String sessionId);

    void subscribe(String sessionId, @Nullable String subId, String destination, String backDestination, StompFrameHandler handler, StompHeaders headers);

    void unSubscribe(String sessionId, String subId);

    void modifyKey(String oldKey, String newKey);

    boolean isOpen(String sessionId);

    String getDestination(String sessionId, String subId);

    boolean hasSubScribe(String sessionId, String destination);
     boolean hasSubScribe(String destination);
    List<SubscribeDestinationUserInfo> getDestinationUser(String destination);

    boolean reconnect(String smcSessionId, String authorization, MyStompSession myStompSession,StompSessionHandlerAdapter stompSessionHandlerAdapter);

    boolean reconnect(String smcSessionId, String username, String tickets, String smcToken, MyStompSession myStompSession,StompSessionHandlerAdapter stompSessionHandlerAdapter);

     void reconnect(MyStompSession myStompSession,StompSessionHandlerAdapter stompSessionHandlerAdapter);

    public Queue<ReSubscribeInfo> getReSubscribeInfoQueue();
}
