package com.suntek.vdm.gw.core.service;

import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.websocket.SubscribeMessage;

public interface WebsocketClientService {
    void messageHandle(String text);
//    void pushSubscribeMessage(String orgId,String destination,String message);
    void pushSubscribeMessage(SubscribeMessage subscribeMessage, GwId sourceGwId);
}
