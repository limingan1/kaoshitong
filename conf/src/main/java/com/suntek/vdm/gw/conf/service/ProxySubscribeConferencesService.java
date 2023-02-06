package com.suntek.vdm.gw.conf.service;

import com.suntek.vdm.gw.common.pojo.GwId;

public interface ProxySubscribeConferencesService {
     void subscribeChild(String conferenceId, String destination);

     void subscribeChild(String conferenceId, String  accessCode, GwId targetGwId, String destination, String pConferenceId, String pDestination);

     void unSubscribeChild(String conferenceId, String destination);
}
