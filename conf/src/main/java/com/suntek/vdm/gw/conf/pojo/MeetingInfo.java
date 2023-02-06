package com.suntek.vdm.gw.conf.pojo;

import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import com.suntek.vdm.gw.common.enums.MeetingControlType;
import com.suntek.vdm.gw.common.pojo.*;
import com.suntek.vdm.gw.common.pojo.websocket.SubscribeAttachInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Slf4j
public class MeetingInfo {
    private String id;
    private String accessCode;
    private String name;

    private GwId sourceGwId;
    private String destination;
    private String backDestination;
    private SubscribeAttachInfo info;
    /**
     * 开始状态
     */
    private Boolean startFlag;
    /**
     * welink同步广播标志
     */
    private Boolean bSyncBroadcastFlag;
    /**
     * welink同步广播标志完成
     */
    private Boolean bSyncBroadcastCompleteFlag = false;
    /**
     * welink同步广播会场状态标志（0：主级联通道视频源打开，1：普通会场入会）
     */
    private Integer bSyncBroadcasOnlineFlag;
    private Integer bSyncBroadcastOnlineChannelFlag;

    /**
     * 会议状态
     */
    private ConferenceState conferenceState;
    /**
     * 会议旧状态
     */
    private Map<MeetingControlType, ConferenceStateOld> conferenceStateOldMap;
    /**
     * 下级会议基本信息
     */
    private Map<String, ChildMeetingInfo> childMeetingInfoMap;
    /**
     * 全部会场
     */
    private Map<String, ParticipantInfo> allParticipantMap;
    /**
     * 级联通道状态
     */
    private Map<String, CascadeChannelInfo> cascadeChannelInfoMap;

    /**
     * 会场真实视频源缓存
     */
    private Map<String, MultiPicInfo> cascadeMultiPicInfoMap;


    public String getConfCasId() {
        return accessCode;
    }


    public MeetingInfo(String id, String accessCode, String name, Boolean startFlag, ConferenceState conferenceState) {
        this.id = id;
        this.accessCode = accessCode;
        this.name = name;
        this.startFlag = startFlag;
        this.conferenceStateOldMap = new HashMap<>();
        this.childMeetingInfoMap = new ConcurrentHashMap<>();
        this.allParticipantMap = new ConcurrentHashMap<>();
        this.cascadeChannelInfoMap = new ConcurrentHashMap<>();
        this.cascadeMultiPicInfoMap = new ConcurrentHashMap<>();
        setConferenceState(conferenceState);
    }

    public ChildMeetingInfo getChild(String childConfCasId) {
        if(childConfCasId.contains("**")){
            childConfCasId = childConfCasId.split("\\*\\*")[0];
        }
        return this.childMeetingInfoMap.get(childConfCasId);
    }

    public ChildMeetingInfo getChildByConferenceId(String childConferenceId) {
        for (ChildMeetingInfo item : this.childMeetingInfoMap.values()) {
            if (childConferenceId.equals(item.getId())) {
                return item;
            } else {
                for (GwConferenceId gwConferenceId : item.getChildConferenceIdSet()) {
                    if (gwConferenceId.getConferenceId().equals(childConferenceId)) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    public void addChild(String confCasId, ChildMeetingInfo childMeetingInfo) {
        log.info("add child conf id:{},confCasId:{},init:{},localId:{}", childMeetingInfo.getId(), confCasId, childMeetingInfo.initialized(), this.id);
        this.childMeetingInfoMap.put(confCasId, childMeetingInfo);
    }


    public boolean hasChild() {
        if (childMeetingInfoMap.size() > 0) {
            return true;
        } else {
            return false;
        }
    }


    public Map<String, ParticipantInfo> getLocalParticipant() {
        Map<String, ParticipantInfo> localParticipant = new ConcurrentHashMap<>();
        for (ParticipantInfo item : this.allParticipantMap.values()) {
            if (item.getConferenceId().equals(this.id)) {
                localParticipant.put(item.getParticipantId(), item);
            }
        }
        return localParticipant;
    }

    public Map<String, ParticipantInfo> getLocalCasParticipant() {
        Map<String, ParticipantInfo> localCasParticipantFilter = new ConcurrentHashMap<>();
        for (ParticipantInfo item : this.allParticipantMap.values()) {
            if (item.getConferenceId().equals(this.id)) {
                if (item.isCascadeParticipant()) {
                    localCasParticipantFilter.put(item.getParticipantId(), item);
                }

            }
        }
        return localCasParticipantFilter;
    }

    public Map<String, ParticipantInfo> getLocalCasParticipant(CascadeParticipantDirection direction, String childConfId, Integer index) {
        Map<String, ParticipantInfo> localCasParticipantFilter = new ConcurrentHashMap<>();
        ChildMeetingInfo childMeetingInfo = null;
        if (childConfId != null && direction.equals(CascadeParticipantDirection.DOWN)) {
            childMeetingInfo = getChildByConferenceId(childConfId);
        }
        for (ParticipantInfo item : this.allParticipantMap.values()) {
            if (!item.getConferenceId().equals(this.id)) {
                continue;
            }
            if (item.isCascadeParticipant()) {
                if (direction != null) {
                    if (!item.getCascadeParticipantParameter().getDirection().equals(direction)) {
                        continue;
                    }
                }
                if (childMeetingInfo != null) {
                    if (!item.getConfCasId().equals(childMeetingInfo.getConfCasId())) {
                        continue;
                    }
                }
                if (index != null) {
                    if (item.getCascadeParticipantParameter().getIndex().equals(index)) {
                        localCasParticipantFilter.put(item.getParticipantId(), item);
                        return localCasParticipantFilter;
                    }
                } else {
                    localCasParticipantFilter.put(item.getParticipantId(), item);
                }

            }
        }
        return localCasParticipantFilter;
    }


    public void addParticipant(ParticipantInfo participantInfo) {
        //本级不保存下级级联通道会场，解决3级h323通道级联，因第一级与第三级对第二级的级联通道uri一样，造成本级下级级联通道内存被覆盖。
        if(participantInfo.isCascadeParticipant() && CascadeParticipantDirection.UP.equals(participantInfo.getCascadeParticipantParameter().getDirection()) && !participantInfo.getConferenceId().equals(this.id)){
            log.warn("lower casChannel pid:{} id:{} confCasId:{} localId:{}", participantInfo.getParticipantId(), participantInfo.getConferenceId(), participantInfo.getConfCasId(), this.id);
            return;
        }
        log.info("add participant conf pid:{} id:{} confCasId:{} localId:{}", participantInfo.getParticipantId(), participantInfo.getConferenceId(), participantInfo.getConfCasId(), this.id);
        if(this.allParticipantMap.containsKey(participantInfo.getParticipantId())){
            ParticipantInfo memoryParticipantInfo = this.allParticipantMap.get(participantInfo.getParticipantId());
            if(memoryParticipantInfo.isCascadeParticipantH323() && !participantInfo.isCascadeMainParticipant()){
                return;
            }
        }
        this.allParticipantMap.put(participantInfo.getParticipantId(), participantInfo);
        if (participantInfo.getConferenceId().equals(this.id) && participantInfo.isCascadeParticipant()) {
            CascadeChannelInfo cascadeChannelInfo = this.cascadeChannelInfoMap.get(participantInfo.getParticipantId());
            if(cascadeChannelInfo == null){
                cascadeChannelInfo = new CascadeChannelInfo(participantInfo.getParticipantId(), participantInfo.getCascadeParticipantParameter());
                cascadeChannelInfo.setParticipantId(participantInfo.getParticipantId());
                cascadeChannelInfo.setBaseInfo(participantInfo.getCascadeParticipantParameter());
            }
            //设置级联通道使用状态
            if (cascadeChannelInfo.isMain() && !cascadeChannelInfo.getParticipantId().equals(this.conferenceState.getBroadcastId())) {
                cascadeChannelInfo.use(MeetingControlType.BROADCASTER, participantInfo.getParticipantId());
            }
            this.cascadeChannelInfoMap.put(participantInfo.getParticipantId(), cascadeChannelInfo);
        }

    }

    public void delParticipant(String participantId) {
        log.info("del participant conf pid:{}  localId:{}", participantId, this.id);
        this.allParticipantMap.remove(participantId);
        this.cascadeChannelInfoMap.remove(participantId);
    }


    public void setConferenceState(ConferenceState conferenceState) {
        setConferenceStateOld(MeetingControlType.CHAIRMAN, conferenceState.getChairmanId());
        setConferenceStateOld(MeetingControlType.BROADCASTER, conferenceState.getBroadcastId());
        setConferenceStateOld(MeetingControlType.LOCKPRESENTER, conferenceState.getLockPresenterId());
        handleCascadeChannelBroadcast(conferenceState.getBroadcastId(), this.conferenceState == null ? null : this.conferenceState.getBroadcastId());
        this.conferenceState = conferenceState;

    }


    /**
     * 处理级联通道广播状态
     *
     * @param broadcastId
     * @param lastBroadcastId
     */
    public void handleCascadeChannelBroadcast(String broadcastId, String lastBroadcastId) {
        //设置广播
        if (!StringUtils.isEmpty(broadcastId)) {
            if (this.cascadeChannelInfoMap.containsKey(broadcastId)) {
                CascadeChannelInfo cascadeChannelInfo = this.cascadeChannelInfoMap.get(broadcastId);
                if (cascadeChannelInfo.isMain() && broadcastId.equals(cascadeChannelInfo.getParticipantId())
                        && !MeetingControlType.BROADCASTER.equals(cascadeChannelInfo.getMeetingControlType())) {
                    cascadeChannelInfo.use(MeetingControlType.BROADCASTER, broadcastId);
                }
            }
        }
        if (lastBroadcastId != null) {
            //广播状态变为空 释放级联通道
            if (!StringUtils.isEmpty(lastBroadcastId) && StringUtils.isEmpty(broadcastId)) {
                if (this.cascadeChannelInfoMap.containsKey(lastBroadcastId)) {
                    CascadeChannelInfo cascadeChannelInfo = this.cascadeChannelInfoMap.get(lastBroadcastId);
                    if (MeetingControlType.BROADCASTER.equals(cascadeChannelInfo.getMeetingControlType())) {
                        cascadeChannelInfo.free();
                    }
                }
            }
        }
    }

    public ConferenceStateOld getConferenceStateOld(MeetingControlType meetingControlType) {
        if (!conferenceStateOldMap.containsKey(meetingControlType)) {
            ConferenceStateOld conferenceStateOld = new ConferenceStateOld();
            conferenceStateOld.setLastTime(System.currentTimeMillis());
            conferenceStateOldMap.put(meetingControlType, conferenceStateOld);
        }
        return conferenceStateOldMap.get(meetingControlType);
    }

    public void setConferenceStateOld(MeetingControlType meetingControlType, String pId) {
        ConferenceStateOld conferenceStateOld = getConferenceStateOld(meetingControlType);
        if (!StringUtils.isEmpty(pId)) {
            if (!pId.equals(conferenceStateOld.getPId())) {
                conferenceStateOld.setPId(pId);
                conferenceStateOld.setLastTime(System.currentTimeMillis());
            }
        }
    }

    public boolean checkParticipantConferenceState(MeetingControlType meetingControlType, String pid) {
        String currentPid = conferenceState.getByType(meetingControlType);
        if (StringUtils.isEmpty(currentPid)) {
            ConferenceStateOld conferenceStateOld = getConferenceStateOld(meetingControlType);
            if (conferenceStateOld != null && !conferenceStateOld.expired() && pid.equals(conferenceStateOld.getPId())) {
                return true;
            } else {
                return false;
            }
        } else {
            return pid.equals(currentPid);
        }
    }

    public Integer getCuurrentCasChannelNum(String welinkAccessCode) {
        int i = 0;
        for(ParticipantInfo participantInfo: allParticipantMap.values()){
            if(!participantInfo.getUri().startsWith(welinkAccessCode)){
                continue;
            }
            i++;
        }
        return i;
    }

    public ChildMeetingInfo getChildMeetingByNodeId(String nodeId) {
        if (childMeetingInfoMap == null || childMeetingInfoMap.isEmpty()) {
            return null;
        }
        for (ChildMeetingInfo childMeetingInfo : childMeetingInfoMap.values()) {
            if (nodeId.equals(childMeetingInfo.getGwId().getNodeId())) {
                return childMeetingInfo;
            }
        }
        return null;
    }

    public void setSubscribeInfo(GwId sourceGwId, String destination, String backDestination, SubscribeAttachInfo info) {
        this.sourceGwId = sourceGwId;
        this.destination = destination;
        this.backDestination = backDestination;
        this.info = info;
    }

    public synchronized boolean getAndSetHasBroacdcastFirst() {
        log.info("getAndSetHasBroacdcastFirst: {}", bSyncBroadcastFlag);
        if (!bSyncBroadcastFlag) {
            bSyncBroadcastFlag = true;
            return false;
        }
        return true;
    }

    public ChildMeetingInfo getFirstChildMeetingInfoByConfId(String childConfId) {
        for (ChildMeetingInfo item : childMeetingInfoMap.values()) {
            if (childConfId.equals(item.getId())) {
                return item;
            }
        }
        return null;
    }
}
