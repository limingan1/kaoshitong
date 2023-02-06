package com.suntek.vdm.gw.conf.ws.client;


import com.suntek.vdm.gw.conf.service.SubscribeManageAsyncService;
import com.suntek.vdm.gw.smc.ws.stomp.MyStompSession;
import com.suntek.vdm.gw.smc.ws.stomp.SmcStompClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Slf4j
@Component
public class CustomStompSessionHandler extends StompSessionHandlerAdapter {

    @Autowired
    @Lazy
    private SubscribeManageAsyncService subscribeManageAsyncService;

    public CustomStompSessionHandler() {

    }

    @Override
    public void afterConnected(final StompSession session, StompHeaders connectedHeaders) {
        log.info("StompHeaders: " + connectedHeaders.toString());
    }


    @Override
    public Type getPayloadType(StompHeaders headers) {
        return byte[].class;
    }

    @Override
    public void handleFrame(StompHeaders headers, @Nullable Object payload) {
        if (payload != null) {
            log.info("Stomp Message:{}", new String((byte[]) payload));
        }
    }


    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload,
                                Throwable exception) {
        try {
            log.error("websocket exception sessionId：{} and exception:{}", session.getSessionId(), exception.getMessage());
        } catch (Exception e) {
            log.error("exception",e);
        }
    }


    /**
     * 处理来自底层 WebSocket 消息传输的错误。
     * 用于传输级别的错误，包括ConnectionLostException
     * @param session
     * @param exception
     */
    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        try {
            MyStompSession myStompSession = SmcStompClient.getWebsocketMap().get(session.getSessionId());
            if(myStompSession == null){
                log.error("MyStompSession is empty.");
                log.error("websocket error sessionId：{} and error:{} stacktrace:{}", session.getSessionId(), exception.getMessage(),exception.getStackTrace());
                return;
            }
            log.error("websocket error sessionId：{} and error:{} stacktrace:{}", session.getSessionId(), exception.getMessage(),exception.getStackTrace());
            //延迟半秒  防止太快SMC 拒掉连接
            try {
                Thread.sleep(1000); //1000 毫秒，也就是1秒.
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            subscribeManageAsyncService.reconnect(myStompSession,this);
        } catch (Exception e) {
            log.error("exception",e);
        }
    }
}

