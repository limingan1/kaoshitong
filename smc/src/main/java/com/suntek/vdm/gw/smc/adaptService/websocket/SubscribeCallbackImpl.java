package com.suntek.vdm.gw.smc.adaptService.websocket;

import com.huawei.vdmserver.smc.core.service.subscribe.SubscribeCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class SubscribeCallbackImpl implements SubscribeCallback {
    @Override
    public void callback(String websocketUser, String url, String body, Map<String, Object> headers) {
        StompFrameHandler stompFrameHandler = SubscribeHandlerService.getStompFrameHandler(websocketUser, url);
        if(stompFrameHandler == null){
            log.error("callback handler for user: {} is empty.", websocketUser);
            return;
        }
        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.setDestination(url);
        stompFrameHandler.handleFrame(stompHeaders, body);
    }

    @Override
    public void callback(String url, String body, Map<String, Object> headers) {
        Map<String,Map<String, StompFrameHandler>> subSccribeMap = SubscribeHandlerService.getSubSccribeMap();
        for(Map<String, StompFrameHandler> map : subSccribeMap.values()){
            StompFrameHandler stompFrameHandler = map.get(url);
            if(stompFrameHandler == null){
                continue;
            }
            StompHeaders stompHeaders = new StompHeaders();
            stompHeaders.setDestination(url);
            stompFrameHandler.handleFrame(stompHeaders, body);
        }
    }
}
