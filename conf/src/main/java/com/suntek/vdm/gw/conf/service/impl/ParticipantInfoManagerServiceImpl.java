package com.suntek.vdm.gw.conf.service.impl;

import com.suntek.vdm.gw.conf.pojo.MeetingInfo;
import com.suntek.vdm.gw.conf.pojo.ParticipantInfo;
import com.suntek.vdm.gw.conf.service.MeetingInfoManagerService;
import com.suntek.vdm.gw.conf.service.ParticipantInfoManagerService;
import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ParticipantInfoManagerServiceImpl implements ParticipantInfoManagerService {
    @Autowired
    private MeetingInfoManagerService meetingInfoManagerService;

    public void addParticipant(String id, ParticipantInfo participantInfo) {
        MeetingInfo meetingInfo = meetingInfoManagerService.get(id);
        meetingInfo.addParticipant(participantInfo);
    }

    public ParticipantInfo getParticipant(String id, String participantId) {
        //默认会场设置
        if ("00000000-0000-0000-0000-000000000000".equals(participantId)) {
            ParticipantInfo defaultParticipantInfo = new ParticipantInfo();
            defaultParticipantInfo.setConferenceId(id);
            defaultParticipantInfo.setParticipantId(participantId);
            defaultParticipantInfo.setOnline(true);
            return defaultParticipantInfo;
        } else {
            if (!meetingInfoManagerService.contains(id)){
                return null;
            }
            return getAllParticipant(id).get(participantId);
        }
    }

    public Map<String, ParticipantInfo> getAllParticipant(String id) {
        if (!meetingInfoManagerService.contains(id)){
            return null;
        }
        Map<String, ParticipantInfo> map = meetingInfoManagerService.get(id).getAllParticipantMap();
        return map;
    }

    private Map<String, ParticipantInfo> getLocalParticipant(String id) {
        Map<String, ParticipantInfo> all = getAllParticipant(id);
        Map<String, ParticipantInfo> localParticipant = new ConcurrentHashMap<>();
        for (ParticipantInfo item : all.values()) {
            if (item.getConferenceId().equals(id)) {
                localParticipant.put(item.getParticipantId(), item);
            }
        }
        return localParticipant;
    }

    public Map<String, ParticipantInfo> getLocalNotCasParticipant(String id) {
        Map<String, ParticipantInfo> localParticipant = getLocalParticipant(id);
        Map<String, ParticipantInfo> localNotCasParticipant = new ConcurrentHashMap<>();
        for (ParticipantInfo item : localParticipant.values()) {
            if (!item.isCascadeParticipant()) {
                localNotCasParticipant.put(item.getParticipantId(), item);
            }
        }
        return localNotCasParticipant;
    }

    public Map<String, ParticipantInfo> getLocalCasParticipant(String id) {
        return getLocalCasParticipant(id, null, null, null);
    }

    public Map<String, ParticipantInfo> getLocalCasParticipant(String id, CascadeParticipantDirection direction) {
        return getLocalCasParticipant(id, direction, null, null);
    }

    public Map<String, ParticipantInfo> getLocalCasParticipant(String id, CascadeParticipantDirection direction, String childConfId) {
        return getLocalCasParticipant(id, direction, childConfId, null);
    }

    public ParticipantInfo getLocalCasParticipantFirst(String id, CascadeParticipantDirection direction, String childConfId, Integer index) {
        Map<String, ParticipantInfo> localParticipant = getLocalCasParticipant(id, direction, childConfId, index);
        if (localParticipant.size() > 0) {
            for (ParticipantInfo item : localParticipant.values()) {
                return item;
            }
        }
        return null;
    }

    public Map<String, ParticipantInfo> getLocalCasParticipant(String id, CascadeParticipantDirection direction, String childConfId, Integer index) {
        MeetingInfo meetingInfo = meetingInfoManagerService.get(id);
        if(meetingInfo == null){
            return null;
        }
        return meetingInfo.getLocalCasParticipant(direction, childConfId, index);
    }

    public void delParticipant(String id, String participantId) {
        MeetingInfo meetingInfo = meetingInfoManagerService.get(id);
        meetingInfo.delParticipant(participantId);
    }
}
