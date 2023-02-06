
package com.suntek.vdm.gw.conf.service.impl;

import com.suntek.vdm.gw.common.util.MessageLogUtil;
import com.suntek.vdm.gw.conf.service.ParticipantInfoManagerService;
import com.suntek.vdm.gw.conf.service.SubscribeCallBackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SubscribeCallBackServiceImpl implements SubscribeCallBackService {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void callback(String user, String subId, String url, String body, Map<String, String> headers) {
        MessageLogUtil.callback(user, subId, url, body);
        Map<String, Object> objectMap = new HashMap<>();
        for (Map.Entry<String, String> item : headers.entrySet()) {
            objectMap.put(item.getKey(), item.getValue());
        }
        simpMessagingTemplate.convertAndSendToUser(user, url, body, objectMap);
    }


    @Override
    public void callback(String url, String body, Map<String, String> headers) {
        MessageLogUtil.callback(url, body);
        Map<String, Object> objectMap = new HashMap<>();
        for (Map.Entry<String, String> item : headers.entrySet()) {
            objectMap.put(item.getKey(), item.getValue());
        }
        simpMessagingTemplate.convertAndSend(url, body, objectMap);
    }
}
