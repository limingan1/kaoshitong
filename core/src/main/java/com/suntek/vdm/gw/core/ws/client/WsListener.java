package com.suntek.vdm.gw.core.ws.client;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.suntek.vdm.gw.core.service.WebsocketClientService;
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


    public static WebsocketClientService websocketClientService;

    @Override
    public void onTextMessage(WebSocket websocket, String text) throws Exception {
        super.onTextMessage(websocket, text);
        log.info("Websocket on textMessage {}", text);
        websocketClientService.messageHandle(text);
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
        log.info("websocket disconnect");
    }

    @Override
    public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {
        super.onMessageError(websocket, cause, frames);
        log.error("websocket onMessageError, cause {}", cause.getMessage());
        cause.printStackTrace();
    }

    @Override
    public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
        super.onSendError(websocket, cause, frame);
        log.error("websocket onSendError, cause {}", cause.getMessage());
        cause.printStackTrace();
    }

    @Override
    public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {
        super.onUnexpectedError(websocket, cause);
        log.info("websocket onUnexpectedError, cause {}", cause.getMessage());
        cause.printStackTrace();
    }

    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        super.onError(websocket, cause);
        log.error("websocket onError, cause {}", cause.getMessage());
        cause.printStackTrace();
    }

    @Override
    public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) throws Exception {
        super.onTextMessageError(websocket, cause, data);
        log.error("websocket onTextMessageError, cause {}", cause.getMessage());
        cause.printStackTrace();
    }
}
