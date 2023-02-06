package com.suntek.vdm.gw.smc.service.impl;

import com.suntek.vdm.gw.common.util.Encryption;
import com.suntek.vdm.gw.smc.adaptService.websocket.AdaptWebsocketService;
import com.suntek.vdm.gw.smc.service.SmcSubscribeService;
import com.suntek.vdm.gw.smc.ws.stomp.MyStompSession;
import com.suntek.vdm.gw.smc.ws.stomp.MySubscription;
import com.suntek.vdm.gw.smc.ws.stomp.SmcStompClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@Slf4j
public class SmcSubscribeCasServiceImpl implements SmcSubscribeService {
    @Autowired
    private SmcStompClient smcStompClient;

    @Value("${useAdapt}")
    private Boolean useAdapt;

    @Autowired
    AdaptWebsocketService adaptWebsocketService;

    @Override
    public String connect(String username, String tickets, String token, StompSessionHandlerAdapter stompSessionHandlerAdapter) {
        StompSession stompSession = null;
        if(useAdapt){
            stompSession = adaptWebsocketService.connect(username, tickets, token, null);
        }else {
            stompSession = smcStompClient.getClient(username, tickets, token, stompSessionHandlerAdapter);
        }
        return connectAfter(username, token, null, stompSession);
    }

    @Override
    public String connect(String authorization, StompSessionHandlerAdapter stompSessionHandlerAdapter) {
        String username = Encryption.decryptBase64(authorization.replace("Basic ", "")).split(":")[0];
        StompSession stompSession = null;
        if(useAdapt){
            stompSession = adaptWebsocketService.connect(username, null, null, authorization);
        }else{
            stompSession = smcStompClient.getClient(authorization, stompSessionHandlerAdapter);
        }
        return connectAfter(username, null, authorization, stompSession);
    }

    public String connectAfter(String username, String token, String authorization, StompSession stompSession) {
        if (stompSession == null) {
            return null;
        }
        log.info("smc connect by username:{}",username);
        stompSession.isConnected();
        SmcStompClient.getWebsocketMap().put(stompSession.getSessionId(), new MyStompSession(stompSession, new HashMap<>()));//加入缓存
        return stompSession.getSessionId();
    }

    @Override
    public void disconnect(String sessionId) {
        if (sessionId==null){
        return;
    }
        StompSession stompSession = getStompSession(sessionId);
        //先删除再关闭  代表正常关闭
        SmcStompClient.getWebsocketMap().remove(sessionId);
        if (stompSession != null) {
            stompSession.disconnect();
        }
    }

    @Override
    public void subscribe(String sessionId, String destination, String subId, StompFrameHandler handler, StompHeaders headers) {
        if (headers == null) {
            headers = new StompHeaders();
        }
        headers.setDestination(destination);
        headers.setId(subId);
        log.info("smc subscribe sessionId:{} destination:{} subId:{}",sessionId,destination,subId);

//        内存相关
        MyStompSession myStompSession = getMyStompSession(sessionId);
        if (myStompSession.getStompSession() != null) {
            //同一个stompSession并发订阅会抛异常
            synchronized (myStompSession.getStompSession()) {
//                订阅
                StompSession.Subscription subscription = myStompSession.getStompSession().subscribe(headers, handler);
                MySubscription mySubscription = new MySubscription(subscription, handler);
                myStompSession.getSubscriptionMap().put(subscription.getSubscriptionId(), mySubscription);
            }
        }
    }

    @Override
    public void unSubscribe(String sessionId, String subId) {
        log.info("smc unsubscribe sessionId:{}  subId:{}",sessionId,subId);
        if(sessionId == null){
            return;
        }
        MyStompSession myStompSession = SmcStompClient.getWebsocketMap().get(sessionId);
        if (myStompSession != null) {
            StompSession.Subscription subscription = myStompSession.getSubscriptionMap().get(subId).getSubscription();
            if (subscription != null) {
                subscription.unsubscribe();
            }
        }
    }

    @Override
    public boolean isOpen(String sessionId) {
        StompSession stompSession = getStompSession(sessionId);
        if (stompSession != null) {
            return stompSession.isConnected();
        }
        return false;
    }

    private StompSession getStompSession(String sessionId) {
        MyStompSession myStompSession = getMyStompSession(sessionId);
        if(myStompSession == null){
            return null;
        }
        return myStompSession.getStompSession();
    }

    private MyStompSession getMyStompSession(String sessionId) {
        MyStompSession myStompSession = SmcStompClient.getWebsocketMap().get(sessionId);
        if (myStompSession != null) {
            return myStompSession;
        }
        return null;
    }
}
