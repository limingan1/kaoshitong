package com.suntek.vdm.gw.conf.service.impl;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.*;
import com.suntek.vdm.gw.common.enums.CascadeChannelNotifyType;
import com.suntek.vdm.gw.conf.pojo.*;
import com.suntek.vdm.gw.conf.service.*;
import com.suntek.vdm.gw.common.enums.MeetingControlType;
import com.suntek.vdm.gw.common.api.request.MeetingControlRequest;
import com.suntek.vdm.gw.smc.service.SmcMeetingControlService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class CascadeChannelNotifyServiceImpl extends BaseServiceImpl implements CascadeChannelNotifyService {
    @Autowired
    private MeetingInfoManagerService meetingInfoManagerService;
    @Autowired
    private CascadeChannelPushService cascadeChannelPushService;
    @Autowired
    private VideoSourceService videoSourceService;
    @Autowired
    private SmcMeetingControlService smcMeetingControlService;


    public void notifyHandle(CascadeChannelNotifyInfo cascadeChannelNotifyInfo) {
        MeetingInfo meetingInfo = meetingInfoManagerService.getByCasConfId(cascadeChannelNotifyInfo.getConfCasId());
        if(meetingInfo == null){
            log.info("meetingInfo not found");
            return;
        }
        String changeParticipantId = cascadeChannelNotifyInfo.getChangeParticipantId();
        boolean isLocalCasChannel = false;
        if (changeParticipantId == null) {
            Map<String, ParticipantInfo> localCasParticipantFilter = meetingInfo.getLocalCasParticipant(cascadeChannelNotifyInfo.getDirection(), cascadeChannelNotifyInfo.getRemoteConferenceId(), cascadeChannelNotifyInfo.getIndex());
            if (localCasParticipantFilter.size() == 0) {
                log.info("cascade channel not found");
                return;
            }
            //本级对应的级联会场
            ParticipantInfo participantInfo = localCasParticipantFilter.values().stream().findFirst().get();
            changeParticipantId = participantInfo.getParticipantId();
            isLocalCasChannel = true;
        }
        switch (cascadeChannelNotifyInfo.getCascadeChannelNotifyType()) {
            case VIDEO_SOURCE: {
                MultiPicInfo changeMultiPicInfo = cascadeChannelNotifyInfo.getMultiPicInfo();
                videoSourceService.videoSourceHandleByRemote(meetingInfo.getId(), changeMultiPicInfo, changeParticipantId, isLocalCasChannel);
                break;
            }
            case TARGET_OFFLINE: {
                if(changeParticipantId != null){
                    ParticipantInfo participantInfo = meetingInfo.getAllParticipantMap().get(changeParticipantId);
                    if(participantInfo != null && meetingInfo.getId().equals(participantInfo.getConferenceId())){
                        return;
                    }
                }
                targetParticipantOffline(meetingInfo.getId(), cascadeChannelNotifyInfo.getRemoteConferenceId(), changeParticipantId, isLocalCasChannel);
                break;
            }
        }
    }

    @Override
    public void notifyFree(CascadeChannelFreeInfo cascadeChannelFreeInfo) {
        MeetingInfo meetingInfo = meetingInfoManagerService.getByCasConfId(cascadeChannelFreeInfo.getConfCasId());
        if(meetingInfo == null){
            log.info("[notifyFree] meetingInfo not found");
            return;
        }
        Map<String, ParticipantInfo> localCasParticipantFilter = meetingInfo.getLocalCasParticipant(cascadeChannelFreeInfo.getDirection(), cascadeChannelFreeInfo.getRemoteConferenceId(), cascadeChannelFreeInfo.getIndex());
        if (localCasParticipantFilter.size() == 0) {
            log.info("[notifyFree] cascade channel not found");
            return;
        }
        //本级对应的级联会场
        ParticipantInfo participantInfo = localCasParticipantFilter.values().stream().findFirst().get();
        CascadeChannelInfo cascadeChannelInfo = meetingInfo.getCascadeChannelInfoMap().get(participantInfo.getParticipantId());
        cascadeChannelInfo.free();
        //检查该级联通道是否观看其他级联通道
        MultiPicInfo multiPicInfo = participantInfo.getMultiPicInfo();
        if(multiPicInfo == null || multiPicInfo.getSubPicList() == null){
            return;
        }
        SubPic subPic = multiPicInfo.getSubPicList().get(0);
        String participantId = subPic.getParticipantId();
        if(StringUtils.isEmpty(participantId)){
            return;
        }
        ParticipantInfo viewedParticipantInfo = meetingInfo.getLocalParticipant().get(participantId);
        if(viewedParticipantInfo == null || !viewedParticipantInfo.isCascadeParticipant()){
            return;
        }
        videoSourceService.checkOtherParticipantsHasViewThisChannel(meetingInfo, participantId, participantId);
    }


    public void targetParticipantOffline(String conferenceId, String remoteConferenceId, String changeParticipantId, boolean isLocalCasChannel) {
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        if (isLocalCasChannel) {
            //检查是不是广播状态  是的话取消
            if (changeParticipantId.equals(meetingInfo.getConferenceState().getBroadcastId())) {
                //取消广播
                MeetingControlRequest meetingControlRequest = new MeetingControlRequest();
                meetingControlRequest.setBroadcaster("");
                try {
                    smcMeetingControlService.meetingControl(conferenceId, meetingControlRequest, getSmcToken(CoreConfig.INTERNAL_USER_TOKEN));
                } catch (MyHttpException e) {

                }
            }
        }
        //查找本级所有级联会场
        Map<String, ParticipantInfo> localCasParticipant = meetingInfo.getLocalCasParticipant();
        for (ParticipantInfo item : localCasParticipant.values()) {
            if (item.getCascadeParticipantParameter().isMain()) {
                ChildMeetingInfo childMeetingInfo = meetingInfo.getChildByConferenceId(remoteConferenceId);
                if (childMeetingInfo != null && item.getConfCasId().equals(childMeetingInfo.getConfCasId())) {
                    //目标来的不处理
                    continue;
                }
                if (item.isCascadeParticipantH323()) {
                    cascadeChannelPushService.pushToRemote(conferenceId, item.getParticipantId(), changeParticipantId, CascadeChannelNotifyType.TARGET_OFFLINE);
                    continue;
                } else if (meetingInfo.checkParticipantConferenceState(MeetingControlType.BROADCASTER, changeParticipantId)) {
                    cascadeChannelPushService.pushToRemote(conferenceId, item.getParticipantId(), changeParticipantId, CascadeChannelNotifyType.TARGET_OFFLINE);
                    continue;
                }
            }
            //如果观看的是目标会场
            if (item.getMultiPicInfo() != null && changeParticipantId.equals(item.getMultiPicInfo().getFirstParticipantId())) {
                //找到对应的级联通道
                CascadeChannelInfo cascadeChannelInfo = meetingInfo.getCascadeChannelInfoMap().get(item.getParticipantId());
                //释放掉此条级联通道
                cascadeChannelInfo.free();
                cascadeChannelPushService.pushToRemote(conferenceId, item.getParticipantId(), null, CascadeChannelNotifyType.TARGET_OFFLINE);
                continue;
            }
        }
    }
}

