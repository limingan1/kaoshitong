package com.suntek.vdm.gw.welink.service.impl;

import com.suntek.vdm.gw.common.enums.CascadeChannelStatus;
import com.suntek.vdm.gw.common.enums.MeetingControlType;
import com.suntek.vdm.gw.common.pojo.AllocateCasChannelInfo;
import com.suntek.vdm.gw.common.pojo.CasChannelParameter;
import com.suntek.vdm.gw.common.pojo.CascadeChannelFreeInfo;
import com.suntek.vdm.gw.common.pojo.CascadeChannelInfo;
import com.suntek.vdm.gw.welink.api.pojo.ParticipantInfo;
import com.suntek.vdm.gw.welink.pojo.WelinkConference;
import com.suntek.vdm.gw.welink.service.WelinkCasChannelManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WelinkCasChannelManageServiceImpl implements WelinkCasChannelManageService {
    @Autowired
    private WelinkMeetingManagerService welinkMeetingManagerService;

    @Override
    public AllocateCasChannelInfo allocateCasChannel(String conferenceId, CasChannelParameter watchCasChannel, MeetingControlType meetingControlType, String watchParticipantId) {
        AllocateCasChannelInfo allocateCasChannelInfo = new AllocateCasChannelInfo();
        Map<String, CascadeChannelInfo> cascadeChannelInfoMap = getCascadeChannelInfo(conferenceId);
        if (cascadeChannelInfoMap == null) {
            return null;
        }
        if (cascadeChannelInfoMap.size() == 1) {
            //兼容单通道
            allocateCasChannelInfo.setCascadeChannelInfo(cascadeChannelInfoMap.values().stream().filter(x -> x.getIndex() == 0).findFirst().get());
            return allocateCasChannelInfo;
        }
        WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceId);
        List<CascadeChannelInfo> cascadeChannelInfoListSortedByIndex = cascadeChannelInfoMap.values().stream().sorted(Comparator.comparing(CascadeChannelInfo::getIndex).reversed()).collect(Collectors.toList());

        Integer channelIndex = watchCasChannel.getIndex();
        if (channelIndex != null && channelIndex != -1) {
            //分配指定级联通道
            CascadeChannelInfo cascadeChannelInfo = cascadeChannelInfoListSortedByIndex.stream().filter(item -> item.getIndex() == channelIndex).findFirst().get();
            cascadeChannelInfo.use(meetingControlType, watchParticipantId);
            allocateCasChannelInfo.setCascadeChannelInfo(cascadeChannelInfo);
            allocateCasChannelInfo.setReuse(true);
            return allocateCasChannelInfo;
        }
        //优先使用从通道
        for (CascadeChannelInfo item : cascadeChannelInfoListSortedByIndex) {
//            ParticipantInfo participantInfo = welinkConference.getAllParticipantMap().get(item.getParticipantId());
            if (item.isMain() && MeetingControlType.BROADCASTER.equals(item.getMeetingControlType())) {
                continue;
            }
            if (MeetingControlType.BROADCASTER.equals(meetingControlType) && item.isMain()) {
                continue;
            }
            CascadeChannelInfo cascadeChannelInfo = cascadeChannelInfoMap.get(item.getParticipantId());
            if(!watchParticipantId.equals(cascadeChannelInfo.getViewedPid())){
                continue;
            }
            cascadeChannelInfo.use(meetingControlType, watchParticipantId);
            allocateCasChannelInfo.setCascadeChannelInfo(cascadeChannelInfo);
            allocateCasChannelInfo.setReuse(true);
            return allocateCasChannelInfo;
        }

        for (CascadeChannelInfo item : cascadeChannelInfoListSortedByIndex) {
            if (item.getCascadeChannelStatus().equals(CascadeChannelStatus.FREE)) {
                if (item.getBaseInfo().isMain()) {
                    if (MeetingControlType.BROADCASTER.equals(meetingControlType)) {
                        log.info("[级联通道][分配][使用] 主通道，但是分配的是广播点名，不允许分配主通道");
                    } else {
                        CascadeChannelInfo cascadeChannelInfo = cascadeChannelInfoMap.get(item.getParticipantId());
                        cascadeChannelInfo.use(meetingControlType, watchParticipantId);
                        log.info("[级联通道][分配][使用]  pid:{} index:{}", cascadeChannelInfo.getParticipantId(), cascadeChannelInfo.getBaseInfo().getIndex());
                        allocateCasChannelInfo.setCascadeChannelInfo(cascadeChannelInfo);
                        return allocateCasChannelInfo;
                    }
                } else {
                    CascadeChannelInfo cascadeChannelInfo = cascadeChannelInfoMap.get(item.getParticipantId());
                    cascadeChannelInfo.use(meetingControlType, watchParticipantId);
                    allocateCasChannelInfo.setCascadeChannelInfo(cascadeChannelInfo);
                    return allocateCasChannelInfo;
                }
            }
        }
        List<CascadeChannelInfo> cascadeChannelInfoListSortedByUseTime = cascadeChannelInfoMap.values().stream().sorted(Comparator.comparing(CascadeChannelInfo::getUseTime)).collect(Collectors.toList());
        if(cascadeChannelInfoListSortedByUseTime.size() == 0){
            return null;
        }
        CascadeChannelInfo cascadeChannelInfoMain = cascadeChannelInfoListSortedByUseTime.stream().filter(CascadeChannelInfo::isMain).findFirst().get();
        for (CascadeChannelInfo item : cascadeChannelInfoListSortedByUseTime) {
            if (item.getBaseInfo().isMain()) {
                CascadeChannelInfo cascadeChannelInfo = cascadeChannelInfoMap.get(item.getParticipantId());
                cascadeChannelInfo.use(meetingControlType, watchParticipantId);
                allocateCasChannelInfo.setCascadeChannelInfo(cascadeChannelInfo);
            }else {
                if (!MeetingControlType.BROADCASTER.equals(meetingControlType)) {
                    //主通道不是广播的话 要判断使用时间
                    if (!MeetingControlType.BROADCASTER.equals(cascadeChannelInfoMain.getMeetingControlType()) && cascadeChannelInfoMain.getUseTime() < item.getUseTime()) {
                        continue;
                    }
                }
                CascadeChannelInfo cascadeChannelInfo = cascadeChannelInfoMap.get(item.getParticipantId());
                cascadeChannelInfo.use(meetingControlType, watchParticipantId);
                allocateCasChannelInfo.setCascadeChannelInfo(cascadeChannelInfo);
            }
            return allocateCasChannelInfo;
        }
        return allocateCasChannelInfo;
    }

    @Override
    public AllocateCasChannelInfo allocateMainCasChannel(String conferenceId) {
        AllocateCasChannelInfo allocateCasChannelInfo = new AllocateCasChannelInfo();
        Map<String, CascadeChannelInfo> cascadeChannelInfo = getCascadeChannelInfo(conferenceId);
        if(cascadeChannelInfo.size() == 0){
            return null;
        }
        CascadeChannelInfo cascadeChannelInfoMain = cascadeChannelInfo.values().stream().filter(CascadeChannelInfo::isMain).findFirst().get();
        allocateCasChannelInfo.setCascadeChannelInfo(cascadeChannelInfoMain);
        return allocateCasChannelInfo;
    }

    public Map<String, CascadeChannelInfo> getCascadeChannelInfo(String conferenceId) {
        Map<String, ParticipantInfo> localCasParticipantFilter = welinkMeetingManagerService.getLocalCasParticipant(conferenceId);
        Map<String, CascadeChannelInfo> cascadeChannelInfoMap = new ConcurrentHashMap<>();
        if (localCasParticipantFilter != null) {
            Map<String, CascadeChannelInfo> channelMap = welinkMeetingManagerService.getWelinkConferenceCascadeChannelMap(conferenceId);
            for (String key : localCasParticipantFilter.keySet()) {
                CascadeChannelInfo cascadeChannelInfo = channelMap.get(key);
                if(cascadeChannelInfo == null){
                    continue;
                }
                cascadeChannelInfoMap.put(key, cascadeChannelInfo);
            }
        }
        return cascadeChannelInfoMap;
    }
    @Override
    public CascadeChannelInfo getCascadeChannelInfoByIndex(String conferenceId, int index) {
        Map<String, CascadeChannelInfo> localCascadeChannelInfo = welinkMeetingManagerService.getWelinkConferenceCascadeChannelMap(conferenceId);

        for (CascadeChannelInfo cascadeChannelInfo : localCascadeChannelInfo.values()) {
            if(index != cascadeChannelInfo.getIndex()){
                continue;
            }
            return cascadeChannelInfo;
        }
        return null;
    }

    @Override
    public void freeCascadeChannel(CascadeChannelFreeInfo cascadeChannelFreeInfo) {
        Map<String, CascadeChannelInfo> cascadeChannelInfoMap = getCascadeChannelInfo(cascadeChannelFreeInfo.getConfCasId());
        if(cascadeChannelInfoMap.size() == 0){
            return;
        }
        for(CascadeChannelInfo cascadeChannelInfo: cascadeChannelInfoMap.values()){
            if(cascadeChannelInfo.getIndex() != cascadeChannelFreeInfo.getIndex()){
                continue;
            }
            cascadeChannelInfo.free();
            return;
        }
    }
}
