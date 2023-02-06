package com.suntek.vdm.gw.smc.adaptService.websocket;

import com.huawei.vdmserver.common.enums.emnus.SubscribeUrlType;
import com.huawei.vdmserver.smc2_0.subscribe.SubscribeServiceImpl;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.util.Encryption;
import com.suntek.vdm.gw.smc.adaptService.util.SubscribeUrlRegex;
import com.suntek.vdm.gw.smc.response.GetTokenResponse;
import com.suntek.vdm.gw.smc.service.SmcLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AdaptWebsocketService {
    @Autowired
    private SubscribeServiceImpl subscribeServiceImpl;
    @Autowired
    private SmcLoginService smcLoginService;

    public StompSession connect(String username, String tickets, String token, String authorization) {
        if(!StringUtils.isEmpty(authorization)){
            try {
                GetTokenResponse getTokenResponse = smcLoginService.getTokens(authorization);
                username = Encryption.decryptBase64(authorization.replace("Basic ", "")).split(":")[0];
                String sessionId = username+"_"+UUID.randomUUID().toString();
                String user = username+"_"+UUID.randomUUID().toString();
                subscribeServiceImpl.connect(sessionId, user, getTokenResponse.getUuid());
                return buildStompSession(sessionId, user);
            } catch (MyHttpException exception) {
                exception.printStackTrace();
            }
        }

        if (token != null && tickets != null && tickets.equals(token)) {
            String sessionId = username+"_"+UUID.randomUUID().toString();
            String user = username+"_"+UUID.randomUUID().toString();
            subscribeServiceImpl.connect(sessionId, user, token);
            return buildStompSession(sessionId, user);
        }
        return null;

    }

    private StompSession buildStompSession(String sessionId, String username){
        return new StompSession( ) {
            @Override
            public String getSessionId() {
                return sessionId;
            }

            @Override
            public boolean isConnected() {
                return true;
            }

            @Override
            public void setAutoReceipt(boolean b) {

            }

            @Override
            public Receiptable send(String s, Object o) {
                return null;
            }

            @Override
            public Receiptable send(StompHeaders stompHeaders, Object o) {
                return null;
            }

            @Override
            public Subscription subscribe(String s, StompFrameHandler stompFrameHandler) {

                return null;
            }

            @Override
            public Subscription subscribe(StompHeaders stompHeaders, StompFrameHandler stompFrameHandler) {
                String destination = stompHeaders.getDestination();
                String subId = stompHeaders.getId();
                String conferenceId = null;
                SubscribeUrlType subscribeUrlType = SubscribeUrlType.CONFERENCES_STATUS;
                String regex = SubscribeUrlRegex.CONFERENCES_CONTROL_STATUS_REGEX.getRegex();
                String participantsRegex = SubscribeUrlRegex.CONFERENCES_PARTICIPANT_STATUS_REGEX.getRegex();
                if(SubscribeUrlRegex.CONFERENCES_STATUS_REGEX.getRegex().equals(destination)){

                }else if(destination.matches(regex)){
                    subscribeUrlType = SubscribeUrlType.CONFERENCES_CONTROL_STATUS;
                    conferenceId = getPathVar(regex, destination, 1)[0];
                }
                if(destination.matches(participantsRegex)){
                    subscribeUrlType = SubscribeUrlType.CONFERENCES_PARTICIPANT_STATUS;
                    conferenceId = getPathVar(participantsRegex, destination, 1)[0];
                }
                subscribeServiceImpl.subscribe(sessionId, subId, subscribeUrlType, conferenceId, null);
//                保存handler，在订阅回调函数中对接
                if(destination.contains("/topic")){
                    destination = destination.substring(6);
                }
                SubscribeHandlerService.setStompFrameHandler(username, destination, stompFrameHandler);

                Subscription subscription = new Subscription(subId);
                return subscription;
            }

            @Override
            public Receiptable acknowledge(String s, boolean b) {
                return null;
            }

            @Override
            public Receiptable acknowledge(StompHeaders stompHeaders, boolean b) {
                return null;
            }

            @Override
            public void disconnect() {
                SubscribeHandlerService.removeStompFrameHandler(username);
                subscribeServiceImpl.disconnect(sessionId);
            }

            @Override
            public void disconnect(StompHeaders stompHeaders) {
                SubscribeHandlerService.removeStompFrameHandler(username);
                subscribeServiceImpl.disconnect(sessionId);
            }

            class  Subscription implements Receiptable, StompSession.Subscription {
                String subId;
                public Subscription(String subId) {
                    this.subId = subId;
                }

                public String getSubscriptionId(){
                    return subId;
                }

                public StompHeaders getSubscriptionHeaders(){
                    return null;
                }

                public void unsubscribe(){
                    subscribeServiceImpl.unSubscribe(sessionId, subId);
                }

                public void unsubscribe(@Nullable StompHeaders var1){

                }

                @Override
                public String getReceiptId() {
                    return null;
                }

                @Override
                public void addReceiptTask(Runnable runnable) {

                }

//                @Override
                public void addReceiptTask(Consumer<StompHeaders> task) {

                }


                @Override
                public void addReceiptLostTask(Runnable runnable) {

                }
            }

        };
    }
    public String[] getPathVar(String regex, String url,int paramCount) {
        String[] params = new String[paramCount];
        Pattern compile = Pattern.compile(regex);
        Matcher matcher = compile.matcher(url);
        if (matcher.find()) {
            for (int i = 1; i <= paramCount; i++) {
                params[i - 1] = matcher.group(i);
            }
            return params;
        }
        return null;
    }
}
