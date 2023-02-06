package com.suntek.vdm.gw.welink.websocket;

import com.alibaba.fastjson.JSON;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.suntek.vdm.gw.welink.pojo.WelinkConference;
import com.suntek.vdm.gw.welink.service.impl.WelinkUtilService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 继承默认的监听空实现WebSocketAdapter,重写我们需要的方法
 * onTextMessage 收到文字信息
 * onConnected 连接成功
 * onConnectError 连接失败
 * onDisconnected 连接关闭
 */
@Slf4j
public class WsListener extends WebSocketAdapter {

    private WelinkConference welinkConference;
    public WsListener() {
    }
    public WsListener(WelinkConference welinkConference) {
        this.welinkConference = welinkConference;
    }



    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        super.onTextMessage(websocket, text);
//        log.info("Websocket on textMessage {}", text);
//        websocketClientService.messageHandle(text);
        WelinkUtilService.dealWebsocketJson(JSON.parseObject(text),welinkConference);
    }



    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers)
            throws Exception {
        super.onConnected(websocket, headers);
        log.info("websocket connection succeeded");
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception)
            throws Exception {
        super.onConnectError(websocket, exception);
        log.info("websocket on connect error error:{}", exception.getError());
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer)
            throws Exception {
        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
        log.info("connect welink websocket disconnect");
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        super.onError(websocket, cause);
        log.error("connect welink websocket error");
    }
}
