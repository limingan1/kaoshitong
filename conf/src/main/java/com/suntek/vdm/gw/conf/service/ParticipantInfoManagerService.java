package com.suntek.vdm.gw.conf.service;

import com.suntek.vdm.gw.conf.pojo.ParticipantInfo;
import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;

import java.util.Map;

public interface ParticipantInfoManagerService {

    void addParticipant(String id, ParticipantInfo participantInfo);

    void delParticipant(String id, String participantId);

    ParticipantInfo getParticipant(String id, String participantId);

    Map<String, ParticipantInfo> getAllParticipant(String id);

    Map<String, ParticipantInfo> getLocalNotCasParticipant(String id);

    Map<String, ParticipantInfo> getLocalCasParticipant(String id);

    Map<String, ParticipantInfo> getLocalCasParticipant(String id, CascadeParticipantDirection direction);

    Map<String, ParticipantInfo> getLocalCasParticipant(String id, CascadeParticipantDirection direction, String childConfId);

    Map<String, ParticipantInfo> getLocalCasParticipant(String id, CascadeParticipantDirection direction, String childConfId, Integer index);

    ParticipantInfo getLocalCasParticipantFirst(String id, CascadeParticipantDirection direction, String childConfId, Integer index);




}
