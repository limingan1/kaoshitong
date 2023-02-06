package com.suntek.vdm.gw.welink.service;

import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.websocket.SubscribeMessage;

public interface WebsocketClientService {
    void pushSubscribeMessage(SubscribeMessage subscribeMessage, GwId sourceGwId);
}
