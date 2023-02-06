package com.suntek.vdm.gw.conf.ws.server;

import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler;

import javax.annotation.Resource;

@Component
public class WsOperate {

    @Resource(name ="subProtocolWebSocketHandler")
    @Lazy
    private SubProtocolWebSocketHandler subProtocolWebSocketHandler;

    public void closeUser(String sessionId) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
        headerAccessor.setSessionId(sessionId);
        Message<byte[]> createMessage = MessageBuilder.createMessage(new byte[0], headerAccessor.getMessageHeaders());
        subProtocolWebSocketHandler.handleMessage(createMessage);
    }
}
