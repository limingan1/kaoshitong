package com.suntek.vdm.gw.welink.service.impl;

import com.neovisionaries.ws.client.WebSocket;
import com.suntek.vdm.gw.common.pojo.CascadeChannelInfo;
import com.suntek.vdm.gw.welink.api.pojo.ParticipantInfo;
import com.suntek.vdm.gw.welink.pojo.WelinkConference;
import com.suntek.vdm.gw.welink.pojo.WelinkNodeData;
import com.suntek.vdm.gw.welink.websocket.WeLinkWebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class WelinkMeetingManagerService {
    private WelinkNodeData welinkNodeData;
    @Autowired
    private WeLinkWebSocketService weLinkWebSocketService;

    private Map<String, WelinkConference> welinkConferenceMap = new ConcurrentHashMap<>(32);

    public WelinkNodeData getWelinkNodeData() {
        return welinkNodeData;
    }
    public void setWelinkNodeData(WelinkNodeData welinkNodeData) {
        this.welinkNodeData = welinkNodeData;
    }

    public Map<String, WelinkConference> getWelinkConferenceMap() {
        return welinkConferenceMap;
    }
    public WelinkConference getWelinkConference(String conferenceId) {
        return welinkConferenceMap.get(conferenceId);
    }

    public WelinkConference getWelinkConferenceByAccessCode(String accessCode) {
        for(WelinkConference welinkConference: welinkConferenceMap.values()){
            if(welinkConference.getAccessCode() != null && welinkConference.getAccessCode().equals(accessCode)){
                return welinkConference;
            }
        }
        return null;
    }

    public Map<String, CascadeChannelInfo> getWelinkConferenceCascadeChannelMap(String conferenceId) {
        return welinkConferenceMap.get(conferenceId).getCascadeChannelInfoMap();
    }

    public Map<String, ParticipantInfo> getLocalCasParticipant(String conferenceId) {
        return welinkConferenceMap.get(conferenceId).getAllParticipantMap();
    }
    public synchronized void createWelinkConference(String conferenceID, String smcAccessCode,String welinkNodeName,String accessCode,int maxCascadeNum) {
        if (!welinkConferenceMap.containsKey(conferenceID)) {
            WelinkConference welinkConference = new WelinkConference(conferenceID, accessCode,welinkNodeName,smcAccessCode,maxCascadeNum,  weLinkWebSocketService);
            welinkConferenceMap.put(conferenceID, welinkConference);
        }
    }

    public void delMeeting(String conferenceId) {
        WelinkConference delConference = welinkConferenceMap.get(conferenceId);
        if (delConference != null) {
            WebSocket webSocket = delConference.getWebSocket();
            if(webSocket != null){
                webSocket.disconnect();
            }
            welinkConferenceMap.remove(conferenceId);
        }
    }
}
