package com.suntek.vdm.gw.conf.ws.handler;

import com.suntek.vdm.gw.conf.pojo.WebSocketUserAuthentication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * 设置用户信息
 **/
@Component
@Slf4j
public class MyPrincipalHandshakeHandler extends DefaultHandshakeHandler {


    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {


        String user = attributes.get("user").toString();
        return new WebSocketUserAuthentication(user);
    }
}
