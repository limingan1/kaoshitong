package com.suntek.vdm.gw.conf.ws.handler;


import com.suntek.vdm.gw.common.util.Encryption;
import com.suntek.vdm.gw.conf.enumeration.SubscribeUserType;
import com.suntek.vdm.gw.conf.service.SubscribeManageService;
import com.suntek.vdm.gw.conf.ws.client.CustomStompSessionHandler;
import com.suntek.vdm.gw.core.pojo.UserTickets;
import com.suntek.vdm.gw.core.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;

/**
 * 权限拦截器  用于鉴权
 */
@Slf4j
@Component
public class WebSocketInterceptor implements HandshakeInterceptor {
    @Autowired
    private UserService userService;

    @Qualifier("subscribeManageCasServiceImpl")
    @Autowired
    @Lazy
    private SubscribeManageService subscribeManageService;

    @Autowired
    private CustomStompSessionHandler customStompSessionHandler;
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler webSocketHandler, Map<String, Object> attributes) throws Exception {
        // 将ServerHttpRequest转换成request请求相关的类，用来获取request域中的用户信息
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            String authorization = httpRequest.getHeader("Authorization");
            if (authorization != null) {
                log.info("authorization is not null");
                String username = Encryption.decryptBase64(authorization.replace("Basic ", "")).split(":")[0];
                String sessionId = username + UUID.randomUUID().toString();
                String user = username + UUID.randomUUID().toString();
                subscribeManageService.connect(sessionId, user, SubscribeUserType.LOCAL, authorization,customStompSessionHandler);
                attributes.put("sessionId", sessionId);
                attributes.put("user", user);
                return true;
            } else {
                //检查用户的权限
                UserTickets userTickets = userService.websocketPermissionCheck(httpRequest.getParameterMap());
                if (userTickets != null) {
                    String username = httpRequest.getParameterMap().get("username")[0];
                    String sessionId =username + UUID.randomUUID().toString();
                    String user = username + UUID.randomUUID().toString();
                    subscribeManageService.connect(sessionId, user, SubscribeUserType.LOCAL, userTickets.getTicket(), userTickets.getToken(),customStompSessionHandler);
                    attributes.put("sessionId", sessionId);
                    attributes.put("user", user);
                    return true;
                } else {
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return false;
                }
            }

        } else {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest arg0, ServerHttpResponse arg1, WebSocketHandler arg2, Exception arg3) {

    }
}
