package com.suntek.vdm.gw.welink.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.suntek.vdm.gw.welink.pojo.WelinkConference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WelinkUtilService {

    public static void dealWebsocketJson(JSONObject jsonObject,WelinkConference welinkConference) {
        String action = jsonObject.getString("action");
        String conferenceId = jsonObject.getString("confID");
        if ("ConfControl".equals(action)) {
            log.info("ConfControl : {}", jsonObject);
            welinkConference.refreshToken(jsonObject.getJSONObject("data"));
            return;
        }
        if ("HeartBeat".equals(action)) {
            log.debug(System.nanoTime() + " <-- wss HeartBeat");
            return;
        }
        String msgMode = jsonObject.getString("msgMode");
        String isAllMessage = "0";
        JSONArray data;
        switch (action) {
            case "AttendeesNotify":
                data = jsonObject.getJSONArray("data");
                if (isAllMessage.equals(msgMode)) {
                    welinkConference.dealAttendAllMessage(data);
                } else {
                    welinkConference.dealAttendIncreaseMessage(data,conferenceId);
                }
                break;
            case "ChannelAndParticipantsNotify":
                if (isAllMessage.equals(msgMode)) {
                    welinkConference.dealParticipantAllMessage(jsonObject);
                    welinkConference.sendAllMessage(conferenceId);
                } else {
                    welinkConference.dealParticipantIncreaseMessage(jsonObject,conferenceId);
                }
                break;
            case "SpeakerChangeNotify":
                //处理发言方信息
                data = jsonObject.getJSONArray("data");
                welinkConference.dealSpeakStatus(data);
                break;
            case "InviteResultNotify":
                data = jsonObject.getJSONArray("data");
                welinkConference.dealInviteMessage(data);
                break;
            default:
                log.info("[default]: " + jsonObject);
        }
    }
}
