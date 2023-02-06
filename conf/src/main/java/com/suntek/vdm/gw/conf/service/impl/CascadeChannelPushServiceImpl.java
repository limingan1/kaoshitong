package com.suntek.vdm.gw.conf.service.impl;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.*;
import com.suntek.vdm.gw.common.enums.CascadeChannelNotifyType;
import com.suntek.vdm.gw.common.enums.ConfApiUrl;
import com.suntek.vdm.gw.conf.pojo.*;
import com.suntek.vdm.gw.conf.service.CascadeChannelPushService;
import com.suntek.vdm.gw.conf.service.MeetingInfoManagerService;
import com.suntek.vdm.gw.common.enums.CascadeParticipantType;
import com.suntek.vdm.gw.core.service.RemoteGwService;
import com.suntek.vdm.gw.common.util.CommonHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CascadeChannelPushServiceImpl implements CascadeChannelPushService {

    @Autowired
    private MeetingInfoManagerService meetingInfoManagerService;
    @Autowired
    private RemoteGwService remoteGwService;

    /**
     * 推送远端
     *
     * @param conferenceId
     * @param changeCasParticipantId
     * @param changeCasParticipantIdProxy
     * @param cascadeChannelNotifyType
     */
    @Async("taskExecutor")
    public void pushToRemote(String conferenceId, String changeCasParticipantId, String changeCasParticipantIdProxy, CascadeChannelNotifyType cascadeChannelNotifyType) {
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        if (meetingInfo == null) {
            return;
        }
        ParticipantInfo participantInfo = meetingInfo.getAllParticipantMap().get(changeCasParticipantId);
        //观看视频源(SMC视频源 非级联视频源)
        MultiPicInfo watchMultiPicInfo = null;
        switch (cascadeChannelNotifyType) {
            case VIDEO_SOURCE: {
                watchMultiPicInfo = JSON.parseObject(JSON.toJSONString(participantInfo.getMultiPicInfo()),MultiPicInfo.class);
                if(!changeCasParticipantId.equals(changeCasParticipantIdProxy)){
                    watchMultiPicInfo =  meetingInfo.getCascadeMultiPicInfoMap().get(changeCasParticipantIdProxy);
                }
                if (watchMultiPicInfo == null) {
                    log.warn("participant multiPicInfo is null. participantInfo: {}", JSON.toJSONString(participantInfo));
                    return;
                }
                for (SubPic subPic : watchMultiPicInfo.getSubPicList()) {
                    String participantId = subPic.getParticipantId();
                    if (StringUtils.isEmpty(participantId)) {
                        continue;
                    }
                    MultiPicInfo cascadeMultiPicInfo = meetingInfo.getCascadeMultiPicInfoMap().get(participantId);
                    if (cascadeMultiPicInfo == null) {
                        ParticipantInfo viewedParticipantInfo = meetingInfo.getAllParticipantMap().get(participantId);
                        if(viewedParticipantInfo !=null){
                            subPic.setName(viewedParticipantInfo.getName());
                        }
                        continue;
                    }
                    if (watchMultiPicInfo.getPicNum() == 1) {
                        watchMultiPicInfo = cascadeMultiPicInfo;
                        break;
                    } else {
                        if (cascadeMultiPicInfo.getPicNum() == 1) {
                            subPic.setParticipantId(cascadeMultiPicInfo.getFirstParticipantId());
                        }
                    }
                }
                break;
            }
            case TARGET_OFFLINE: {
                break;
            }
        }
        CascadeChannelNotifyInfo cascadeChannelNotifyInfo = new CascadeChannelNotifyInfo();
        CascadeParticipantParameter cascadeParticipantParameter = participantInfo.getCascadeParticipantParameter();
        //如果是2和2级联 需要改变的对象
        if (cascadeParticipantParameter.getCascadeParticipantType().equals(CascadeParticipantType.H323)) {
            cascadeChannelNotifyInfo.setChangeParticipantId(changeCasParticipantIdProxy);
        }
        cascadeChannelNotifyInfo.setIndex(cascadeParticipantParameter.getIndex());
        //设置反方向
        cascadeChannelNotifyInfo.setDirection(cascadeParticipantParameter.getOppositeDirection());
        cascadeChannelNotifyInfo.setMultiPicInfo(watchMultiPicInfo);
        cascadeChannelNotifyInfo.setConfCasId(participantInfo.getConfCasId());
        //本级的会议号  用于远端查找
        cascadeChannelNotifyInfo.setRemoteConferenceId(meetingInfo.getId());
        cascadeChannelNotifyInfo.setCascadeChannelNotifyType(cascadeChannelNotifyType);
        try {
            remoteGwService.toByGwId(cascadeParticipantParameter.getGwId()).post(ConfApiUrl.CASCADE_NOTIFY_SOURCE.value(), cascadeChannelNotifyInfo);
        } catch (MyHttpException e) {
            log.error("MyHttpException: {}, {}, {}, {}",e.getCode(),e.getBody(),e.getMessage(),e.getStackTrace());
            e.printStackTrace();
        }
    }

    /**
     * 推送远端
     *
     * @param conferenceId
     * @param participantInfo
     */
    @Async("taskExecutor")
    public void freeToRemote(String conferenceId, ParticipantInfo participantInfo) {
        CascadeParticipantParameter cascadeParticipantParameter = participantInfo.getCascadeParticipantParameter();
        CascadeChannelFreeInfo cascadeChannelFreeInfo = new CascadeChannelFreeInfo();

        cascadeChannelFreeInfo.setIndex(cascadeParticipantParameter.getIndex());
        //设置反方向
        cascadeChannelFreeInfo.setDirection(cascadeParticipantParameter.getOppositeDirection());
        cascadeChannelFreeInfo.setConfCasId(participantInfo.getConfCasId());
        //本级的会议号  用于远端查找
        cascadeChannelFreeInfo.setRemoteConferenceId(conferenceId);
        try {
            remoteGwService.toByGwId(cascadeParticipantParameter.getGwId()).post(ConfApiUrl.CASCADE_FREE_CHANNEL.value(),cascadeChannelFreeInfo);
        } catch (MyHttpException e) {
            e.printStackTrace();
        }
    }
}
