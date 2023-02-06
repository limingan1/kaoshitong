package com.suntek.vdm.gw.conf.service.impl;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.request.GetChannelStatusReq;
import com.suntek.vdm.gw.conf.api.request.ProxySubscribeRequest;
import com.suntek.vdm.gw.common.pojo.response.CasConferenceInfosResponse;
import com.suntek.vdm.gw.common.enums.ConfApiUrl;
import com.suntek.vdm.gw.common.pojo.CasConfInfo;
import com.suntek.vdm.gw.conf.pojo.ChildMeetingInfo;
import com.suntek.vdm.gw.conf.pojo.MeetingInfo;
import com.suntek.vdm.gw.conf.pojo.ParticipantInfo;
import com.suntek.vdm.gw.conf.service.CascadeService;
import com.suntek.vdm.gw.conf.service.MeetingInfoManagerService;
import com.suntek.vdm.gw.conf.service.ProxySubscribeService;
import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import com.suntek.vdm.gw.core.service.RemoteGwService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class CascadeServiceImpl extends BaseServiceImpl implements CascadeService {
    @Autowired
    private ProxySubscribeService proxySubscribeService;
    @Autowired
    private MeetingInfoManagerService meetingInfoManagerService;
    @Autowired
    private RemoteGwService remoteGwService;
    @Autowired
    private TaskExecutor taskExecutor;

    @Override
    public void proxySubScribe(ProxySubscribeRequest proxySubscribeRequest, String token) throws MyHttpException {
        proxySubscribeService.subscribe(proxySubscribeRequest.getSourceGwId(), proxySubscribeRequest.getDestination(), proxySubscribeRequest.getBackDestination(), proxySubscribeRequest.getInfo(), token);
    }

    @Override
    public void unProxySubScribe(ProxySubscribeRequest proxySubscribeRequest, String token) {
        proxySubscribeService.unSubscribe(proxySubscribeRequest.getSourceGwId(), proxySubscribeRequest.getDestination(), proxySubscribeRequest.getBackDestination(), proxySubscribeRequest.getInfo(), token);
    }




    @Override
    public CasConfInfo casConferenceInfos(String conferenceId) {
        CasConfInfo response = new CasConfInfo();
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        if(meetingInfo != null) {
            response.setName(meetingInfo.getName());
            response.setConfCasId(meetingInfo.getAccessCode());
            response.setIsWeLink(false);
            getChildCasInfo(response, meetingInfo);
        }

        return response;
    }

    private void getChildCasInfo(CasConfInfo casConfInfo, MeetingInfo meetingInfo) {
        if (meetingInfo.hasChild()) {
            if (casConfInfo.getChildConf() == null) {
                casConfInfo.setChildConf(new ArrayList<>());
            }

            List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
            for (ChildMeetingInfo item : meetingInfo.getChildMeetingInfoMap().values()) {
                if (item.initialized()) {
                    // 定义任务
                    CompletableFuture<Void> cf = CompletableFuture.supplyAsync(() -> {
                        try {
                            ResponseEntity<String> response = remoteGwService.toByGwId(item.getGwId()).get(String.format(ConfApiUrl.CASCADE_CONFERENCE_INFOS.value(), item.getId()), null);
                            if (response != null && response.getStatusCode().equals(HttpStatus.OK)) {
                                CasConferenceInfosResponse casConferenceInfosResponse = JSON.parseObject(response.getBody(), CasConferenceInfosResponse.class);
                                casConfInfo.getChildConf().add(casConferenceInfosResponse.getData());
                            }
                        } catch (MyHttpException e) {
                            log.error("get child conferences info fail error:{}", e.toString());
                        }
                        return null;
                    }, taskExecutor);
                    completableFutures.add(cf);
                }
            }
            if (completableFutures.size() > 0) {
                CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()])).join();
            }
        }
    }

    public Boolean getChannelStatus(GetChannelStatusReq getChannelStatusReq){
        MeetingInfo meetingInfo = meetingInfoManagerService.getByCasConfId(getChannelStatusReq.getConfCasId());
        log.info("conferenceId: {}, conferenceStatus: {}",meetingInfo.getId(), meetingInfo.getConferenceState());
        if(meetingInfo == null){
            return false;
        }
        String broadcastId = meetingInfo.getConferenceState().getBroadcastId();
        String spokesmanId = meetingInfo.getConferenceState().getSpokesmanId();
        if(StringUtils.isEmpty(broadcastId) && StringUtils.isEmpty(spokesmanId)){
            return false;
        }
        Map<String, ParticipantInfo> localCasParticipantFilter = meetingInfo.getLocalCasParticipant(CascadeParticipantDirection.DOWN, getChannelStatusReq.getChildCondId(), 0);
        if (localCasParticipantFilter.size() == 0) {
            log.info("[notifyFree] cascade channel not found");
            return false;
        }
        //本级对应的级联会场
        ParticipantInfo participantInfo = localCasParticipantFilter.values().stream().findFirst().get();
        if(participantInfo.getParticipantId().equals(broadcastId) || participantInfo.getParticipantId().equals(spokesmanId)){
            return true;
        }
        return false;
    }

}
