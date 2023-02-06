package com.suntek.vdm.gw.smc.ws.stomp;


import com.suntek.vdm.gw.common.util.Encryption;
import com.suntek.vdm.gw.common.util.SystemConfiguration;
import com.suntek.vdm.gw.smc.service.impl.SmcHttpServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import javax.net.ssl.*;
import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class SmcStompClient {
    private static WebSocketStompClient stompClient;

    private static Map<String, MyStompSession> websocketMap = new ConcurrentHashMap<>();

    public static Map<String, MyStompSession> getWebsocketMap() {
        if (websocketMap == null) {
            websocketMap = new ConcurrentHashMap<>();
        }
        return websocketMap;
    }

    public StompSession getClient(String userName, String ticket, String token, StompSessionHandlerAdapter stompSessionHandlerAdapter) {
        try {
            String url = getUrl(userName, ticket, token);
            StompSession stompSession = connect(url, new WebSocketHttpHeaders(), stompSessionHandlerAdapter);
            return stompSession;
        } catch (Exception e) {
            log.warn("open SMC websocket fail error:" + e);
            return null;
        }
    }

    public String getUrl(String userName, String ticket, String token) {
        String url = "wss://%s/conf-portal/websocket?timestamp=%s&signature=%s&username=%s";
        long timestamp = System.currentTimeMillis();
        String signature = Encryption.smcSignature(String.valueOf(timestamp), userName, ticket, token);
        url = String.format(url, SmcHttpServiceImpl.ip, timestamp, signature, userName);
        return url;
    }


    public StompSession getClient(String authorization, StompSessionHandlerAdapter stompSessionHandlerAdapter) {
        try {
            WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();
            String url = "wss://%s/conf-portal/websocket";
            url = String.format(url, SmcHttpServiceImpl.ip);
            webSocketHttpHeaders.add("Authorization", authorization);
            StompSession stompSession = connect(url, webSocketHttpHeaders, stompSessionHandlerAdapter);
            return stompSession;
        } catch (Exception e) {
            log.warn("open SMC websocket fail error:" + e);
            return null;
        }
    }


    public synchronized WebSocketStompClient getStompClient() {
        if (this.stompClient == null) {
            try {
                Map<String, Object> properties = new HashMap<>();
                if (SmcHttpServiceImpl.ssl) {
                    //绕过SSL
                    SSLContext sc = SSLContext.getInstance("SSL");
                    sc.init(null, getTrustAllCerts(), new java.security.SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                    properties.put("org.apache.tomcat.websocket.SSL_CONTEXT", sc);//websocket必须带
                }
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                int MAX_TEXT_MESSAGE_BUFFER_SIZE = 3000 * 500;
                container.setDefaultMaxTextMessageBufferSize(MAX_TEXT_MESSAGE_BUFFER_SIZE);
                StandardWebSocketClient simpleWebSocketClient = new StandardWebSocketClient(container);
                simpleWebSocketClient.setUserProperties(properties);
                this.stompClient = new WebSocketStompClient(simpleWebSocketClient);
                this.stompClient.setDefaultHeartbeat(new long[]{5000, 0});
                ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
                taskScheduler.afterPropertiesSet();
                this.stompClient.setTaskScheduler(taskScheduler);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
            return this.stompClient;
        } else {
            return this.stompClient;
        }
    }

    public StompSession connect(String url, WebSocketHttpHeaders webSocketHttpHeaders, StompSessionHandlerAdapter stompSessionHandlerAdapter) {// 定义连接函数


        try {
            webSocketHttpHeaders.add("Origin", "https://" + SmcHttpServiceImpl.ip);//SMC3.0必传
            if (SystemConfiguration.smcVersionIsV2()) {
                webSocketHttpHeaders.add("allSubscribe", "true");
            }
            StompHeaders stompHeaders = new StompHeaders();
            if (!SmcHttpServiceImpl.ssl) {
                url = url.replace("wss://", "ws://");
            }
            log.warn("websocket URL:{}", url);
            StompSession stompSession = getStompClient().connect(url, webSocketHttpHeaders,
                    stompHeaders, stompSessionHandlerAdapter).get();
            stompSession.setAutoReceipt(true);
            log.warn("websocket connect success:{}", url);
            return stompSession;
        }catch (ExecutionException executionException){
            log.error("IllegalStateException", executionException);
        }
        catch (IllegalStateException e){
            log.error("IllegalStateException", e);
        } catch (Exception e) {
            log.error("exception", e);
        }
        return null;
    }

    public static TrustManager[] getTrustAllCerts() {
        return new TrustManager[]{new X509ExtendedTrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {

            }

            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {

            }

            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }
        };
    }
}
