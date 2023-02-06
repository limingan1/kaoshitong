package com.suntek.vdm.gw.conf.ws.config;

import com.suntek.vdm.gw.conf.service.WebsocketServerService;
import com.suntek.vdm.gw.conf.ws.server.WebSocketServer;
import com.suntek.vdm.gw.core.service.LocalTokenManageService;
import com.suntek.vdm.gw.core.service.WebsocketClientService;
import com.suntek.vdm.gw.core.ws.client.WsListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {
    /**
     * ServerEndpointExporter 作用
     * <p>
     * 这个Bean会自动注册使用@ServerEndpoint注解声明的websocket endpoint
     *
     * @return
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    /**
     * 因 SpringBoot WebSocket 对每个客户端连接都会创建一个 WebSocketServer（@ServerEndpoint 注解对应的） 对象，Bean 注入操作会被直接略过，因而手动注入一个全局变量
     *
     * @param websocketServerService
     */
    @Autowired
    public void setWebsocketServerService(WebsocketServerService websocketServerService) {
        WebSocketServer.websocketServerService = websocketServerService;
    }

    @Autowired
    public void setLocalTokenManageService(LocalTokenManageService localTokenManageService) {
        WebSocketServer.localTokenManageService = localTokenManageService;
    }

    @Autowired
    public void setWebsocketClientService(WebsocketClientService websocketClientService) {
        WsListener.websocketClientService = websocketClientService;
    }
}
