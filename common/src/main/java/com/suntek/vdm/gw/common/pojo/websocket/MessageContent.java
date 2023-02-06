package com.suntek.vdm.gw.common.pojo.websocket;

import lombok.Data;

@Data
public class MessageContent {
    private MessageType type;
    private String body;

    public MessageContent() {
    }

    public MessageContent(MessageType type, String body) {
        this.type = type;
        this.body = body;
    }
}
