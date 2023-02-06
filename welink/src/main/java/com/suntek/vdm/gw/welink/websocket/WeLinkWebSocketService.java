package com.suntek.vdm.gw.welink.websocket;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.util.CommonHelper;
import com.suntek.vdm.gw.common.util.RandomId;
import com.suntek.vdm.gw.common.ws.client.NaiveSSLContext;
import com.suntek.vdm.gw.welink.api.service.WeLinkMeetingManagementService;
import com.suntek.vdm.gw.welink.pojo.WelinkConference;
import com.suntek.vdm.gw.welink.pojo.WelinkNodeData;
import com.suntek.vdm.gw.welink.service.WeLinkTokenManageService;
import com.suntek.vdm.gw.welink.service.impl.WelinkMeetingManagerService;
import com.suntek.vdm.gw.welink.util.WelinkScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WeLinkWebSocketService {


    @Autowired
    private WeLinkMeetingManagementService weLinkMeetingManagementService;
    @Autowired
    @Lazy
    private WelinkMeetingManagerService welinkMeetingManagerService;
    @Autowired
    private WeLinkTokenManageService weLinkTokenManageService;
    @Autowired
    private WelinkScheduler welinkScheduler;

    private static final String PREFIX = "00000000000000000";

    WebSocketFactory factory = new WebSocketFactory();
    private static final int CONNECT_TIMEOUT = 5000;

    public WeLinkWebSocketService(){
        try {
            SSLContext context = NaiveSSLContext.getInstance("TLS");
            factory.setSSLContext(context);
            factory.setVerifyHostname(false);//不检验域名

        }  catch (NoSuchAlgorithmException e) {

            log.error("[init] SSLContext error:{}", e.getMessage());
        }
    }

    @Async("taskExecutor")
    public void connect(String conferenceId){
        WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceId);
        synchronized (welinkConference){
            WebSocket ws = welinkConference.getWebSocket();
            if(ws == null){
                String wssUrl = getConnWsURl(conferenceId);
                ws = welinkConference.getWebSocket();
                if (ws == null) {
                    try {
                        if(wssUrl == null){
                            return;
                        }
                        ws = factory.createSocket(wssUrl, CONNECT_TIMEOUT)
                                .addListener(new WsListener(welinkConference));
                        String requestId = RandomId.getGUID();
                        ws.addHeader("X-Request-ID",requestId);
                        ws.addHeader("requestId",requestId);
                        log.info("requestId: {}, conferenceI：{}", requestId, conferenceId);
                        ws.connect();
                        welinkConference.setWebSocket(ws);
                        log.info("ws连接成功,conferenceId:{}",conferenceId);
                        sendSubscribeHeartbeat(ws, welinkConference.getConferenceToken());
                        //保活
                        welinkScheduler.restart(this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (WebSocketException e) {
                        log.error("ws连接失败：error:{}", e.getMessage());
                    }
                }
            }
        }
    }
    @Async("taskExecutor")
    public void disconnect(String conferenceId) {
        WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceId);
        synchronized (welinkConference) {
            WebSocket webSocket = welinkConference.getWebSocket();
            if (webSocket != null) {
                webSocket.disconnect();
                welinkConference.setWebSocket(null);
            }
        }
    }


    public void sendHeartbeat() {
        Map<String, WelinkConference> welinkConferenceMap = welinkMeetingManagerService.getWelinkConferenceMap();
        for (WelinkConference welinkConference : welinkConferenceMap.values()) {
            long timeStamp = System.currentTimeMillis();
            String heartTime = PREFIX + timeStamp;
            WebSocket webSocket = welinkConference.getWebSocket();
            if (webSocket != null) {
                webSocket.sendText("{\"sequence\": \"" + heartTime + "\", \"action\": \"HeartBeat\"}");
                log.info("websocket send HeartBeat,conferenceId:{}", welinkConference.getId());
                //刷新conf Token
                if(welinkConference.refreshTokenTimeout()){
                    timeStamp = System.currentTimeMillis();
                    String buf = PREFIX + timeStamp;
                    String refresh = "{\"action\":\"ConfControl\",\"sequence\":\""+buf+"\",\"type\":\"REFRESHTOKEN\",\"data\":\"{\\\"conferenceID\\\":\\\""+welinkConference.getId()+"\\\",\\\"confToken\\\":\\\""+welinkConference.getConferenceToken()+"\\\"}\"}";
                    webSocket.sendText(refresh);
                    log.info("websocket send refreshTokenTimeout,conferenceId:{} buf: {}", welinkConference.getId(), buf);
                }
            }
        }
    }

    /**
     * 获得websocket服务器url
     */
    ThreadLocal<Integer> retryCount = new ThreadLocal<>();
    public String getConnWsURl(String conferenceID) {
        String token;
        try {
            if (retryCount.get() == null) {
                retryCount.set(0);
            }
            token = getToken(conferenceID);
            retryCount.remove();
        } catch (MyHttpException e) {
            Integer currentTime = retryCount.get();
            currentTime++;
            retryCount.set(currentTime);
            if (currentTime > 3) {
                e.printStackTrace();
                log.error("getConnWsURl error,retry count:{}", retryCount.get());
                retryCount.remove();
                return null;
            }
            CommonHelper.sleep(500);
            log.info("retry getConnWsURl,retry count:{}", retryCount.get());
            return getConnWsURl(conferenceID);
        }
        //websocketToken
        String tmpToken = getWebSocketToken(token, conferenceID);
        String We_link_Service_Url = "";
        if (tmpToken != null && !tmpToken.isEmpty()) {
            WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceID);
            We_link_Service_Url = welinkConference.getWsURL() + "/cms/open/websocket/confctl/increment/conn?confID=" + conferenceID + "&tmpToken=" + tmpToken;
        } else {
            log.error("wss [getConnWsURl] failed");
        }
        return We_link_Service_Url;
    }

    private String getWebSocketToken(String token, String conferenceID) {
        try {
            WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceID);
            JSONObject response = weLinkMeetingManagementService.getWebSocketTemporaryToken(conferenceID, token, welinkConference.getWsURL());
            int returnCode = response.getIntValue("returnCode");
            if (returnCode == 0) {
                JSONObject data = response.getJSONObject("data");
                return data.getString("webSocketToken");
            }
            return null;
        } catch (MyHttpException e) {
            e.printStackTrace();
            log.error("getWebSocketToken error: body:{}", e.getBody());
            return null;
        }
    }

    public String getToken(String conferenceID) throws MyHttpException {
        WelinkNodeData welinkNodeData = welinkMeetingManagerService.getWelinkNodeData();
        String[] resultArray = getConferencePwd(conferenceID);
        String password = null;
        if (resultArray != null && resultArray.length > 0) {
            password = resultArray[0];
        }
        if (null == password) {
            log.error("get [conference.getToken] password false");
            return null;
        }
        WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceID);
        JSONObject result = weLinkMeetingManagementService.getMeetingControlToken(conferenceID, password, "1", welinkNodeData.getIp());
//        if (result.getIntValue("statusCode") == 200) { //此处返回没有statusCode属性
        if (result != null) {
            JSONObject tokenDataJson = result.getJSONObject("data");
            welinkConference.setWsURL(tokenDataJson.getString("wsURL"));
            String token = tokenDataJson.getString("token");
            welinkConference.assertTokenNotNull(token == null ? "" : token);
            welinkConference.setConfTokenExpireTime(tokenDataJson.getIntValue("tokenDataJson"));
        } else {
            log.error("get [conference.getToken] Token false");
        }
        return welinkConference.getConferenceToken();
    }

    public String[] getConferencePwd(String conferenceID) throws MyHttpException {
        WelinkNodeData welinkNodeData = welinkMeetingManagerService.getWelinkNodeData();
        String token = weLinkTokenManageService.getToken();
        JSONObject jsonObject = weLinkMeetingManagementService.getConference(token, conferenceID, welinkNodeData.getIp());
        int statusCode = jsonObject.getIntValue("statusCode");
        if (statusCode != 0) {
            log.error("getMeetingRealTimeInfo failed: " + jsonObject);
            return null;
        }
        JSONObject jsonObject1 = jsonObject.getJSONObject("conferenceData");
        WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceID);
        String confMode = jsonObject1.getString("confMode");
        welinkConference.setConfMode(confMode);
        JSONArray passwordEntry = jsonObject1.getJSONArray("passwordEntry");
        int tempPasswordSize = passwordEntry.size();
        if (passwordEntry.size() <= 1) {
            tempPasswordSize++;
        }
        String[] password = new String[tempPasswordSize];
        for (Object entry : passwordEntry) {
            JSONObject passwordObject = (JSONObject) entry;
            if (passwordObject.getString("conferenceRole").equals("chair")) {
                password[0] = passwordObject.getString("password");
            } else {
                password[1] = passwordObject.getString("password");
            }
        }
        return password;
    }

    public void sendSubscribeHeartbeat(WebSocket ws, String confToken) {
        long timeStamp = System.currentTimeMillis();
        String buf = PREFIX + timeStamp;
        log.info("ws发送订阅信息");
        ws.sendText("{\"sequence\":\"" + buf + "\",\"action\":\"Subscribe\",\"data\":\"{\\\"subscribeType\\\":[\\\"AttendeesNotify\\\",\\\"InviteResultNotify\\\",\\\"ChannelAndParticipantsNotify\\\",\\\"SpeakerChangeNotify\\\"],\\\"confToken\\\":\\\"" + confToken + "\\\"}\"}");
        log.info("wss [We_linkWebSocketService.sendSubscribeHeartbeat] send --> " + buf);
    }

}
