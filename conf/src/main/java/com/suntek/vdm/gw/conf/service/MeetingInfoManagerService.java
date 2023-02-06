package com.suntek.vdm.gw.conf.service;

import com.suntek.vdm.gw.conf.pojo.ChildConferenceWait;
import com.suntek.vdm.gw.conf.pojo.ChildMeetingInfo;
import com.suntek.vdm.gw.conf.pojo.MeetingInfo;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.ConferenceState;

import java.util.List;
import java.util.Map;

public interface MeetingInfoManagerService {

    MeetingInfo get(String id);

    ChildMeetingInfo getChildByConferenceId(String id, String childConferenceId);

    ChildMeetingInfo getChildByCasConfId(String id, String confCasId);

    Map<GwId, List<ChildConferenceWait>> getChildConferenceWaits();

    boolean contains(String id);

    void create(String id, String accessCode, String name, Boolean startFlag, ConferenceState conferenceState);

    void createChild(String id, String childConferenceId, String accessCode, String name, GwId gwId);


    void del(String id);

    MeetingInfo getByCasConfId(String confCasId);

    MeetingInfo getByChildConfId(String lowerConfId);

    void onlineMeeting(String id);

    boolean isOnline(String id);

    Boolean childIsOnline(String id, String childConfId);
}
