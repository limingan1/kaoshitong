package com.suntek.vdm.gw.conf.ws.config;


import com.suntek.vdm.gw.conf.service.SubscribeManageService;
import com.suntek.vdm.gw.conf.service.SubscribeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Component
@Slf4j
public class WebsocketListener {
    @Qualifier("subscribeManageCasServiceImpl")
    @Autowired
    private SubscribeManageService subscribeManageService;
    @Autowired
    private SubscribeService subscribeService;


    @EventListener(SessionConnectEvent.class)
    public void sessionConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionAttributes().get("sessionId").toString();
        log.info("Stomp connect by user:{} ", accessor.getUser().getName());
        try {
            subscribeManageService.modifyKey(sessionId, accessor.getSessionId());
        }catch (Exception e){
            log.info("Stomp connect error by user:{} ", accessor.getUser().getName());
            SimpMessageHeaderAccessor.create(SimpMessageType.DISCONNECT);
            subscribeManageService.disconnect(accessor.getSessionId());
        }
    }

    @EventListener(SessionDisconnectEvent.class)
    public void sessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("SubScribe disconnect by user:{} ", accessor.getUser().getName());
        subscribeManageService.disconnect(accessor.getSessionId());
    }


    @EventListener(SessionSubscribeEvent.class)
    public void sessionSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("SubScribe by user:{}  destination:{}", accessor.getUser().getName(), accessor.getDestination());
    }

    @EventListener(SessionUnsubscribeEvent.class)
    public void sessionUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("UnSubScribe by User:{}  destinationId:{}", accessor.getUser().getName(), accessor.getSubscriptionId());
        subscribeService.unSubscribe(accessor.getSessionId(), accessor.getSubscriptionId(), accessor.getUser().getName());
    }
}
