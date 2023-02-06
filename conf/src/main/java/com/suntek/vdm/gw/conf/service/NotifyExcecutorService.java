package com.suntek.vdm.gw.conf.service;

import com.suntek.vdm.gw.common.pojo.ConferencesControllerStatusNotify;
import com.suntek.vdm.gw.common.pojo.ParticipantStatusNotify;
import com.suntek.vdm.gw.conf.pojo.*;

public interface NotifyExcecutorService {

    void dealConferenceStatusNotify(ConferenceStatusNotify conferenceStatusNotify);

    void dealParticipantStatusNotify(ParticipantStatusNotify participantStatusNotify, String confId);

    void dealConferencesControllerStatusNotify(ConferencesControllerStatusNotify conferencesControllerStatusNotify, String confId);

    void dealParticipantInfoNotify(ParticipantInfoNotify participantInfoNotify, String confId,String childConfId);
}
