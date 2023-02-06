package com.suntek.vdm.gw.welink.api.ws.client;


import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketState;
import com.suntek.vdm.gw.common.ws.client.NaiveSSLContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Component
public class WeLinkWebsocketClient {
    /**
     * WebSocket config
     */
    private static final int FRAME_QUEUE_SIZE = 5;
    private static final int CONNECT_TIMEOUT = 5000;

    private static ConcurrentHashMap<String, WebSocket> webSocketMap;


    public static ConcurrentHashMap<String, WebSocket> getWebSocketMap() {
        if (webSocketMap == null) {
            webSocketMap = new ConcurrentHashMap<>();
        }
        return webSocketMap;
    }


    public static boolean isOpen(String id) {
        WebSocket webSocket = getWebSocketMap().get(id);
        if (webSocket == null) {
            return false;
        } else {
            WebSocketState webSocketState = webSocket.getState();
            if (webSocketState.equals(WebSocketState.OPEN)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static void connect(String ip, String url, boolean ssl, String port, String id) {
        /**
         * configUrl其实是缓存在本地的连接地址
         * 这个缓存本地连接地址是app启动的时候通过http请求去服务端获取的,
         * 每次app启动的时候会拿当前时间与缓存时间比较,超过6小时就再次去服务端获取新的连接地址更新本地缓存
         */
        try {
            String code = UUID.randomUUID().toString();
            url += "/" + code;
            StringBuilder sb = new StringBuilder();
            sb.append(ssl ? "wss://" : "ws://");
            sb.append(ip);
            sb.append(":");
            WebSocketFactory factory = new WebSocketFactory();
            if (ssl) {
                try {
                    SSLContext context = NaiveSSLContext.getInstance("TLS");
                    factory.setSSLContext(context);
                    factory.setVerifyHostname(false);//不检验域名
                } catch (NoSuchAlgorithmException e) {
                    log.error("[init] SSLContext error:{}", e.getMessage());
                }
            }
            sb.append(port);
            sb.append(url);
            String wssUrl = sb.toString();
            log.info("websocket url:{}", wssUrl);
            WebSocket ws = factory.createSocket(wssUrl, CONNECT_TIMEOUT);
            ws.setFrameQueueSize(FRAME_QUEUE_SIZE);
            ws.setMissingCloseFrameAllowed(false);//设置不允许服务端关闭连接却未发送关闭帧
            WsListener wsListener = new WsListener();
            ws.addListener(wsListener);//添加回调监听
            ws.setPingInterval(5 * 1000);
            try{
                ws.connect();
                webSocketMap.put(id, ws);
            }catch (WebSocketException e){
                log.error("web socket exception:{}", e.getMessage());
            }
        } catch (IOException e) {
            log.error("websocket io exception:{}", e.getMessage());
        }
    }
}

