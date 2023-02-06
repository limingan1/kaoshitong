package com.suntek.vdm.gw.conf.service.internal.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.suntek.vdm.gw.common.util.MessageLogUtil;
import com.suntek.vdm.gw.conf.pojo.ConferenceStatusNotify;
import com.suntek.vdm.gw.common.pojo.ConferencesControllerStatusNotify;
import com.suntek.vdm.gw.conf.pojo.ParticipantInfoNotify;
import com.suntek.vdm.gw.common.pojo.ParticipantStatusNotify;
import com.suntek.vdm.gw.conf.service.NotifyExcecutorService;
import com.suntek.vdm.gw.conf.service.impl.BaseServiceImpl;
import com.suntek.vdm.gw.conf.service.internal.InternalCallBackService;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@Slf4j
public class InternalCallBackServiceImpl extends BaseServiceImpl implements InternalCallBackService {
    @Autowired
    private NotifyExcecutorService notifyExcecutorService;

    @Override
    @Async("taskExecutor")
    public void callBackController(String subId, String destination, String jsonData, String backDestination) {
        MessageLogUtil.callbackLocal(CoreConfig.INTERNAL_SUBSCRIBE_USER, subId, destination, jsonData);
        if ("/conferences/status".equals(backDestination)) {
            ConferenceStatusNotify conferenceStatusNotify = JSON.parseObject(jsonData, ConferenceStatusNotify.class);
            if (conferenceStatusNotify != null) {
                notifyExcecutorService.dealConferenceStatusNotify(conferenceStatusNotify);
            }
            return;
        } else {
            String participantsPattern = "/conferences/.*?/participants/general";
            boolean participantsIsMatch = Pattern.matches(participantsPattern, backDestination);
            if (participantsIsMatch) {
                JSONObject jsonObject = JSON.parseObject(jsonData);
                Integer type = jsonObject.getInteger("type");
                //会场全量通知
                if (type == 0) {
                    log.info("conferences all notify");
                    ParticipantInfoNotify participantStatusNotify = JSON.parseObject(jsonData, ParticipantInfoNotify.class);
                    if (participantStatusNotify.getConferenceId() != null) {
                        String[] strings = backDestination.split("/");
                        String confId = strings[2];
                        String[] stringsChild = destination.split("/");
                        String childConfId = stringsChild[3];
                        notifyExcecutorService.dealParticipantInfoNotify(participantStatusNotify, confId, childConfId);
                    }
                    return;
                } else {
                    ParticipantStatusNotify participantStatusNotify = JSON.parseObject(jsonData, ParticipantStatusNotify.class);
                    if (participantStatusNotify.getConferenceId() != null) {
                        String[] strings = backDestination.split("/");
                        String confId = strings[2];
                        notifyExcecutorService.dealParticipantStatusNotify(participantStatusNotify, confId);
                    }
                    return;
                }
            }
            String conferencesPattern = "/conferences/.*?";
            boolean conferencesIsMatch = Pattern.matches(conferencesPattern, backDestination);
            if (conferencesIsMatch) {
                String[] strings = backDestination.split("/");
                String confId = strings[2];
                ConferencesControllerStatusNotify conferencesControllerStatusNotify = JSON.parseObject(jsonData, ConferencesControllerStatusNotify.class);
                if(conferencesControllerStatusNotify == null){
                    return;
                }
                notifyExcecutorService.dealConferencesControllerStatusNotify(conferencesControllerStatusNotify, confId);
                return;
            }
        }
    }
}

