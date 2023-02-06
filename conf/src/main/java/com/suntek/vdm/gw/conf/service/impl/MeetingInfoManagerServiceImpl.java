package com.suntek.vdm.gw.conf.service.impl;

import com.suntek.vdm.gw.common.pojo.GwConferenceId;
import com.suntek.vdm.gw.conf.pojo.*;
import com.suntek.vdm.gw.conf.service.MeetingInfoManagerAsyncService;
import com.suntek.vdm.gw.conf.service.MeetingInfoManagerService;
import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.core.enumeration.NodeBusinessType;
import com.suntek.vdm.gw.core.service.NodeManageService;
import com.suntek.vdm.gw.core.service.RemoteGwService;
import com.suntek.vdm.gw.core.service.RoutManageService;
import com.suntek.vdm.gw.common.pojo.ConferenceState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MeetingInfoManagerServiceImpl implements MeetingInfoManagerService {
    @Autowired
    private MeetingInfoManagerAsyncService meetingInfoManagerAsyncService;
    @Autowired
    private RemoteGwService remoteGwService;
    @Autowired
    private RoutManageService routManageService;
    @Autowired
    private NodeManageService nodeManageService;

    private static Map<String, MeetingInfo> meetingInfoMap = new ConcurrentHashMap<>();




    public static Map<GwId, List<ChildConferenceWait>> childConferenceWaits = new ConcurrentHashMap<>();

    public Map<GwId, List<ChildConferenceWait>> getChildConferenceWaits() {
        return childConferenceWaits;
    }


    public static Map<String, MeetingInfo> getMeetingInfoMap() {
        return meetingInfoMap;
    }

    @Override
    public MeetingInfo get(String id) {
        MeetingInfo meetingInfo = meetingInfoMap.get(id);
        if (meetingInfo != null) {
            for (ChildMeetingInfo item : meetingInfo.getChildMeetingInfoMap().values()) {
                meetingInfoManagerAsyncService.initCasConf(item);
            }
        }
        return meetingInfo;
    }

    @Override
    public ChildMeetingInfo getChildByConferenceId(String id, String childConferenceId) {
        return get(id).getChildByConferenceId(childConferenceId);
    }

    @Override
    public ChildMeetingInfo getChildByCasConfId(String id, String confCasId) {
        MeetingInfo meetingInfo = get(id);
        if (meetingInfo == null) {
            return null;
        }
        ChildMeetingInfo childMeetingInfo = meetingInfo.getChildMeetingInfoMap().get(confCasId);
        if (childMeetingInfo == null) {
            for (ChildMeetingInfo item : meetingInfo.getChildMeetingInfoMap().values()) {
                for (GwConferenceId gwConferenceId : item.getChildConferenceIdSet()) {
                    if (gwConferenceId.getConfCasId().equals(confCasId)) {
                        childMeetingInfo = item;
                        break;
                    }
                }
            }
        }
        if (childMeetingInfo == null) {
            return null;
        }
        childMeetingInfo.loadInfo(remoteGwService);
        return childMeetingInfo;
    }


    @Override
    public MeetingInfo getByCasConfId(String confCasId) {
        for (MeetingInfo meetingInfo : meetingInfoMap.values()) {
            if (confCasId.equals(meetingInfo.getAccessCode())) {
                return meetingInfo;
            }
        }
        return null;
    }

    @Override
    public MeetingInfo getByChildConfId(String lowerConfId) {
        for (MeetingInfo item : meetingInfoMap.values()) {
            ChildMeetingInfo childMeetingInfo = item.getChildByConferenceId(lowerConfId);
            if (childMeetingInfo != null) {
                return item;
            }
        }
        return null;
    }

    @Override
    public boolean contains(String id) {
        return meetingInfoMap.containsKey(id);
    }

    @Override
    public void create(String id, String accessCode, String name, Boolean startFlag, ConferenceState conferenceState) {
        log.info("create meeting cache id:{},accessCode:{},name:{}", id, accessCode, name);
        meetingInfoMap.put(id, new MeetingInfo(id, accessCode, name, startFlag, conferenceState));
    }

    @Override
    public void createChild(String id, String childConferenceId, String accessCode, String name, GwId gwId) {
        log.info("Create child meeting cache id:{},childConferenceId:{},accessCode:{},name:{},gwId:{}", id, childConferenceId, accessCode, name, gwId);
        if (gwId.inComplete()) {
            GwId gwIdWay = routManageService.getCompleteGwIdBy(gwId);
            if (gwIdWay != null) {
                gwId = gwIdWay;
            }
        }
        if (childConferenceId == null) {
            ChildConferenceWait childConferenceWait = new ChildConferenceWait(gwId, id, accessCode);
            if (!childConferenceWaits.containsKey(gwId)) {
                childConferenceWaits.put(gwId, new ArrayList<>());
            }
            childConferenceWaits.get(gwId).add(childConferenceWait);
        }
        MeetingInfo meetingInfo = get(id);
        ChildMeetingInfo find = meetingInfo.getChild(accessCode);
        if (find != null) {
            if (!find.initialized() && childConferenceId != null) {
                find.setId(childConferenceId);
            }
        } else {
            ChildMeetingInfo childMeetingInfo = new ChildMeetingInfo(accessCode, childConferenceId, gwId, meetingInfo.getAccessCode());
            meetingInfo.addChild(accessCode, childMeetingInfo);
            //下级是否welink
            NodeBusinessType nodeBusinessType = nodeManageService.getNodeBusinessType(gwId.getNodeId());
            if(NodeBusinessType.WELINK.equals(nodeBusinessType) || NodeBusinessType.CLOUDLINK.equals(nodeBusinessType)){
                childMeetingInfo.setWelink(true);
            }
        }
    }

    public Boolean childIsOnline(String id, String childConfId) {
        MeetingInfo meetingInfo = get(id);
        Map<String, ParticipantInfo> localParticipant = meetingInfo.getLocalCasParticipant(CascadeParticipantDirection.DOWN, childConfId, 0);
        if (localParticipant.size() > 0) {
            for (ParticipantInfo item : localParticipant.values()) {
                return item.getOnline();
            }
        }
        return false;
    }


    @Override
    public void del(String id) {
        meetingInfoMap.remove(id);
    }


    public void onlineMeeting(String id) {
        log.info("start meeting id:{}", id);
        if (meetingInfoMap.containsKey(id)) {
            meetingInfoMap.get(id).setStartFlag(true);
        }
    }

    public boolean isOnline(String id) {
        if (meetingInfoMap.containsKey(id)) {
            Boolean startFlag = meetingInfoMap.get(id).getStartFlag();
            log.info("start meeting id:{} flag:{}", id, startFlag);
            if (startFlag == null) {
                return false;
            }
            return startFlag;
        }
        return false;
    }
}
