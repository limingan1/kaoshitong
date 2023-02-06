package com.suntek.vdm.gw.conf.ws.server;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.pojo.websocket.MessageContent;
import com.suntek.vdm.gw.common.pojo.websocket.MessageType;
import com.suntek.vdm.gw.conf.service.WebsocketServerService;
import com.suntek.vdm.gw.core.pojo.LocalToken;
import com.suntek.vdm.gw.core.service.LocalTokenManageService;
import com.suntek.vdm.gw.smc.ws.stomp.websocket.SmcWebsocketClient;
import com.suntek.vdm.gw.welink.service.impl.WelinkMeetingManagerService;
import com.suntek.vdm.gw.welink.websocket.WelinkWebSocketClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/gw/websocket/{token}/{code}")
@Component
@Slf4j
public class WebSocketServer {
    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static int onlineCount = 0;

    public static ConcurrentHashMap<String, WebSocketServer> getWebSocketMap() {
        return webSocketMap;
    }

    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
     */
    private static ConcurrentHashMap<String, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();
    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
    /**
     * 接收code
     */
    private String nodeId = "";

    public static WebsocketServerService websocketServerService;

    public static LocalTokenManageService localTokenManageService;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token,@PathParam("code") String code) {
        this.session = session;
        this.session.setMaxTextMessageBufferSize(4*1024*1024);
        log.info("gwHttpId{} code:{}", token,code);

        LocalToken localToken = localTokenManageService.get(token);
        String nodeId1=localTokenManageService.getNodeId(token);
        if (localToken == null || nodeId1 == null) {
            String welinkWsToken = WelinkWebSocketClient.getToken();
            if (token.equals(welinkWsToken)) {
                nodeId1 = token;
                this.nodeId = nodeId1;
                log.info("welink success and id:{}", this.nodeId);
            } else if(token.equals(SmcWebsocketClient.getToken())){
                nodeId1 = nodeId;
                this.nodeId = nodeId1;
                log.info("smc success and id:{}", this.nodeId);
            } else {
                log.error("websocket open check error localgwHttpId{} nodeId:{}", localToken == null, nodeId1 == null);
                try {
                    log.info("close");
                    session.close();
                } catch (IOException e) {
                    log.error("session close error");
                }
            }
        } else {
            this.nodeId = nodeId1;
            log.info("success and id:{}", this.nodeId);
        }
        if (webSocketMap.containsKey(nodeId1)) {
            webSocketMap.remove(nodeId1);
            webSocketMap.put(nodeId1, this);
            //加入set中
        } else {
            webSocketMap.put(nodeId1, this);
            //加入set中
            addOnlineCount();
            //在线数加1
        }
        log.info("node connection:{},The current number online is:{}", nodeId1, getOnlineCount());
        try {
            MessageContent messageContent=new MessageContent();
            messageContent.setType(MessageType.OPEN_WEBSOCKET_CODE);
            messageContent.setBody(code);
            //不允许更改 这是下级判断打开的标志位
            sendMessage(JSON.toJSONString(messageContent));
        } catch (IOException e) {
            log.error("node:{},network anomaly!!!!!!", nodeId1);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if (webSocketMap.containsKey(nodeId)) {
            webSocketMap.remove(nodeId);
            //从set中删除
            subOnlineCount();
        }
        log.info("node exit:{},The current number online is:{}", nodeId, getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try{
            websocketServerService.messageHandle(message, session, this.nodeId);
        }catch (Exception e){
            log.error("websocket deal message failed. {}", e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("node error:{},reason:{}", nodeId, error.getMessage());
        // error.printStackTrace();
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }


    /**
     * 发送自定义消息
     */
    public static void sendInfo(String message, String code) throws IOException {
        log.info("code :{}，message:{}", code, message);
        if (StringUtils.isNotBlank(code) && webSocketMap.containsKey(code)) {
            webSocketMap.get(code).sendMessage(message);
        } else {
            log.error("code{},not online", code);
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }
}

