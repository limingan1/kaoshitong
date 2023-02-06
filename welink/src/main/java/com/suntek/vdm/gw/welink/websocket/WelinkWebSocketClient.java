package com.suntek.vdm.gw.welink.websocket;

import com.alibaba.fastjson.JSON;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.suntek.vdm.gw.common.pojo.TransactionId;
import com.suntek.vdm.gw.common.pojo.TransactionType;
import com.suntek.vdm.gw.common.pojo.websocket.MessageContent;
import com.suntek.vdm.gw.common.util.TransactionManage;
import com.suntek.vdm.gw.common.ws.client.NaiveSSLContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class WelinkWebSocketClient {
    /**
     * WebSocket config
     */
    private static final int FRAME_QUEUE_SIZE = 5;
    private static final int CONNECT_TIMEOUT = 5000;
    private static WebSocket webSocket;

    private static String token;

    public static String getToken() {
        return token;
    }

    public static WebSocket getWebSocket(){
        return webSocket;
    }

    public static WebSocket openNodeWebsocket(){
        token = UUID.randomUUID().toString();
        connect("127.0.0.1", "/gw/websocket/"+token, true, "5443", null);
        return getWebSocket();
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
            webSocket = factory.createSocket(wssUrl, CONNECT_TIMEOUT);
            webSocket.setFrameQueueSize(FRAME_QUEUE_SIZE);
            webSocket.setMissingCloseFrameAllowed(false);//设置不允许服务端关闭连接却未发送关闭帧
            webSocket.addListener(new WsListener(){
                @Override
                public void onTextMessage(WebSocket websocket, String text) {
//                    super.onTextMessage(websocket, text);
                    MessageContent messageContent = JSON.parseObject(text, MessageContent.class);
                    TransactionManage.notify(new TransactionId(TransactionType.WEBSOCKET,messageContent.getBody()));
                }
                @Override
                public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                    super.onConnected(websocket, headers);
                    log.info("websocket connection succeeded");
                }
                @Override
                public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
                    super.onConnectError(websocket, exception);
                    log.info("websocket on connect error error:{}", exception.getError());
                }
                @Override
                public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                    super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
                    log.info("websocket onDisconnected");
                }
                @Override
                public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                    super.onError(websocket, cause);
                    log.error("websocket onError");
                }
            });//添加回调监听
            webSocket.setPingInterval(5 * 1000);
            long maxTime = 1000 * 7;
            long start = System.currentTimeMillis();
            //等待连接成功通知 超时关闭
            webSocket.connectAsynchronously();
            TransactionManage.wait(new TransactionId(TransactionType.WEBSOCKET, code), maxTime);
            long end = System.currentTimeMillis();
            //如果等待时间大于等于 最大等待时间 就关闭连接
            if (end - start >= maxTime) {
                log.warn("websocket connection succeeded verified fail start:{} end:{}", start, end);
                webSocket.disconnect();
            } else {
                log.info("websocket connection succeeded verified success start:{} end:{}", start, end);
            }
        } catch (IOException e) {
            log.error("websocket io exception:{}", e.getMessage());
        }
    }

}
