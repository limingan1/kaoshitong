package com.suntek.vdm.gw.conf.service;

import javax.annotation.Nullable;

public interface SubscribeService {
    void conferencesStatus(String subId, String user, String sessionId);
    void conferencesControllerStatus(String conferenceId, String conferencesToken, @Nullable String subId, String user, String sessionId,boolean autoChild);
    void conferencesParticipantsStatus(String conferenceId, String conferencesToken,@Nullable String subId, String user, String sessionId,boolean autoChild);
    void meetingRoomStatus(String subId, String user, String sessionId);
    void unSubscribe(String sessionId, String sunId, String user);
}
