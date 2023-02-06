package com.suntek.vdm.gw.conf.pojo;

import com.suntek.vdm.gw.conf.enumeration.SubscribeUserType;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class SubscribeInfo {
    private String sessionId;
    private String smcSessionId;
    private String user;
    private String token;
    private SubscribeUserType type;
    private Map<String, SubscribeDetail> subscribeDetailMap;


    public SubscribeInfo(String sessionId, String smcSessionId, String user, String token, SubscribeUserType type) {
        this.sessionId = sessionId;
        this.smcSessionId = smcSessionId;
        this.user = user;
        this.token = token;
        this.type = type;
        this.subscribeDetailMap = new ConcurrentHashMap<>();
    }

    public void addSubscribe(String subId, String destination) {
        subscribeDetailMap.put(subId, new SubscribeDetail(subId, destination));
    }

    public void delSubscribe(String subId) {
        subscribeDetailMap.remove(subId);
    }

}
