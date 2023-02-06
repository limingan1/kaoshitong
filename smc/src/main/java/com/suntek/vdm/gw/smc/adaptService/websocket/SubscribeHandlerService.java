package com.suntek.vdm.gw.smc.adaptService.websocket;

import org.springframework.messaging.simp.stomp.StompFrameHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SubscribeHandlerService {
    static Map<String,Map<String, StompFrameHandler>> subSccribeMap = new ConcurrentHashMap<>();
    public static StompFrameHandler getStompFrameHandler(String username, String destination){
        Map<String, StompFrameHandler> stompFrameHandlerMap = subSccribeMap.get(username);
        if(stompFrameHandlerMap == null){
            return null;
        }
        return stompFrameHandlerMap.get(destination);
    }

    public static void setStompFrameHandler(String username, String destination, StompFrameHandler stompFrameHandler){
        Map<String, StompFrameHandler> stompFrameHandlerMap = subSccribeMap.get(username);
        if(stompFrameHandlerMap == null){
            stompFrameHandlerMap = new ConcurrentHashMap<>();
            subSccribeMap.put(username, stompFrameHandlerMap);
        }
        stompFrameHandlerMap.put(destination, stompFrameHandler);
    }

    public static void removeStompFrameHandler(String username){
        subSccribeMap.remove(username);
    }

    public static Map<String,Map<String, StompFrameHandler>> getSubSccribeMap(){
        return subSccribeMap;
    }
}
