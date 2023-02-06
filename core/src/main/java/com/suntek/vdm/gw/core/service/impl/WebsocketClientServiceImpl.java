package com.suntek.vdm.gw.core.service.impl;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.neovisionaries.ws.client.WebSocket;
import com.suntek.vdm.gw.common.pojo.TransactionId;
import com.suntek.vdm.gw.common.pojo.TransactionType;
import com.suntek.vdm.gw.common.util.MessageLogUtil;
import com.suntek.vdm.gw.common.util.TransactionManage;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.core.service.NodeLinkService;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.core.service.WebsocketClientService;
import com.suntek.vdm.gw.core.ws.client.WebsocketClient;
import com.suntek.vdm.gw.common.pojo.websocket.MessageContent;
import com.suntek.vdm.gw.common.pojo.websocket.MessageType;
import com.suntek.vdm.gw.common.pojo.websocket.SubscribeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WebsocketClientServiceImpl implements WebsocketClientService {
    @Autowired
    private NodeLinkService nodeLinkService;
    @Autowired
    private RoutManageServiceImpl routManageService;
    @Autowired
    private NodeDataService nodeDataService;


    private static Interner<String> pool = Interners.newWeakInterner();

    @Override
    public void messageHandle(String message) {
        try{
            if (StringUtils.isNotBlank(message)) {
                MessageContent messageContent = JSON.parseObject(message, MessageContent.class);
                log.info("[MessageHandle] type:{}", messageContent.getType().name());
                switch (messageContent.getType()) {
                    case OPEN_WEBSOCKET_CODE:{
                        //通知websocket连接成功防止虚假连接
                        TransactionManage.notify(new TransactionId(TransactionType.WEBSOCKET,messageContent.getBody()));
                        break;
                    }
                    default:
                }
            }
        }catch (Exception e){
              log.error("");
        }

    }

    @Override
    @Async
    public void pushSubscribeMessage(SubscribeMessage subscribeMessage, GwId sourceGwId) {
        WebSocket webSocket;
        GwId gwId = null;
        NodeData localNodeData = nodeDataService.getLocal();
        if(localNodeData.toGwId().equals(sourceGwId)){
            gwId = localNodeData.toGwId();
        }else{
            gwId = routManageService.getWayByGwId(sourceGwId);
            if (gwId == null) {
                gwId = nodeDataService.getTop().toGwId();
            }
        }
        String json = JSON.toJSONString(subscribeMessage);
        MessageLogUtil.callbackNode(json,gwId.toString());
        synchronized (pool.intern(gwId.getNodeId())) {
            if (!WebsocketClient.isOpen(gwId.getNodeId())) {
                log.info("webSocket is close need open");
                Boolean result = nodeLinkService.openNodeWebsocketById(gwId.getNodeId());
                if(!result){
                    log.error("token not found");
                    return;
                }
            }
            webSocket = WebsocketClient.getWebSocketMap().get(gwId.getNodeId());
        }
        MessageContent messageContent = new MessageContent();
        messageContent.setType(MessageType.SUBSCRIBE_MESSAGE);
        messageContent.setBody(json);
        if(json.indexOf("type:3")>0){
            webSocket.sendText(JSON.toJSONString(messageContent));
            return;
        }
        webSocket.sendText(JSON.toJSONString(messageContent));
    }
}
