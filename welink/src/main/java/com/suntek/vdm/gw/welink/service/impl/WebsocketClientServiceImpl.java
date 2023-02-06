package com.suntek.vdm.gw.welink.service.impl;

import com.alibaba.fastjson.JSON;
import com.neovisionaries.ws.client.WebSocket;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.websocket.MessageContent;
import com.suntek.vdm.gw.common.pojo.websocket.MessageType;
import com.suntek.vdm.gw.common.pojo.websocket.SubscribeMessage;
import com.suntek.vdm.gw.common.util.MessageLogUtil;
import com.suntek.vdm.gw.welink.service.WebsocketClientService;

public class WebsocketClientServiceImpl implements WebsocketClientService {

    @Override
    public void pushSubscribeMessage(SubscribeMessage subscribeMessage, GwId sourceGwId) {
        WebSocket webSocket;
//        GwId gwId = routManageService.getWayByGwId(sourceGwId);
//        if (gwId == null) {
//            gwId = nodeDataService.getTop().toGwId();
//        }
        String json = JSON.toJSONString(subscribeMessage);
//        MessageLogUtil.callbackNode(json,gwId.toString());
//        synchronized (pool.intern(gwId.getNodeId())) {
//            if (!WebsocketClient.isOpen(gwId.getNodeId())) {
//                log.info("webSocket is close need open");
//                nodeLinkService.openNodeWebsocketById();
//            }
//            webSocket = WebsocketClient.getWebSocketMap().get(gwId.getNodeId());
//        }
//        MessageContent messageContent = new MessageContent();
//        messageContent.setType(MessageType.SUBSCRIBE_MESSAGE);
//        messageContent.setBody(json);
//        if(json.indexOf("type:3")>0){
//            webSocket.sendText(JSON.toJSONString(messageContent));
//        }
//        webSocket.sendText(JSON.toJSONString(messageContent));
    }
}
