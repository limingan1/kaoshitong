package com.suntek.vdm.gw.conf.service.impl;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.conf.api.request.ProxySubscribeRequest;
import com.suntek.vdm.gw.common.enums.ConfApiUrl;
import com.suntek.vdm.gw.conf.pojo.ChildMeetingInfo;
import com.suntek.vdm.gw.conf.pojo.ConferencesSubscribeAttachInfo;
import com.suntek.vdm.gw.conf.pojo.MeetingInfo;
import com.suntek.vdm.gw.conf.service.MeetingInfoManagerService;
import com.suntek.vdm.gw.conf.service.ProxySubscribeConferencesService;
import com.suntek.vdm.gw.conf.service.SubscribeManageService;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.core.service.RemoteGwService;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.common.pojo.websocket.SubscribeAttachInfo;
import com.suntek.vdm.gw.common.pojo.websocket.SubscribeBusinessType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProxySubscribeConferencesServiceImpl implements ProxySubscribeConferencesService {
    @Autowired
    private RemoteGwService remoteGwService;
    @Autowired
    private MeetingInfoManagerService meetingInfoManagerService;
    @Autowired
    private SubscribeManageService subscribeManageService;
    @Autowired
    private NodeDataService nodeDataService;

    @Async("taskExecutor")
    public void subscribeChild(String conferenceId, String destination) {
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        try{
            for (ChildMeetingInfo item : meetingInfo.getChildMeetingInfoMap().values()) {
                item.loadInfo(remoteGwService);
                if(destination == null || item.getId() == null){
                    continue;
                }
                String childDestination = destination.replace(conferenceId, item.getId());
                subscribeChild(item.getId(), item.getAccessCode(), item.getGwId(), childDestination, conferenceId, destination);
            }
        }catch (Exception e){
            log.error("subscribe child error, msg: {}",e.getMessage());
            e.printStackTrace();
        }
    }

    @Async("taskExecutor")
    public void subscribeChild(String conferenceId, String accessCode, GwId targetGwId, String destination, String pConferenceId, String pDestination) {
//        String remoteDestination = destination.replace(pConferenceId, conferenceId);//替换会议号
        ConferencesSubscribeAttachInfo conferencesSubscribeAttachInfo = new ConferencesSubscribeAttachInfo(conferenceId, accessCode);
        SubscribeAttachInfo subscribeAttachInfo = new SubscribeAttachInfo(SubscribeBusinessType.CONFERENCES, JSON.toJSONString(conferencesSubscribeAttachInfo));
        ProxySubscribeRequest proxySubscribeRequest = new ProxySubscribeRequest(destination, pDestination, nodeDataService.getLocal().toGwId(), subscribeAttachInfo);
        try {
            remoteGwService.toByGwId(targetGwId).post(ConfApiUrl.CASCADE_SUBSCRIBE.value(), proxySubscribeRequest);
        } catch (MyHttpException e) {
            log.error("subscribe child error: {}, msg: {}", e.getCode(),e.getBody());
            //TODO  订阅异常的处理方式
            retrySubscribeChild(targetGwId, proxySubscribeRequest, 0);
        }
    }
    public void retrySubscribeChild(GwId targetGwId,ProxySubscribeRequest proxySubscribeRequest, int num){
        if(num > 5){
            return;
        }
        num++;
        log.error("retry to subScribe child. num: {}", num);
        try {
            remoteGwService.toByGwId(targetGwId).post(ConfApiUrl.CASCADE_SUBSCRIBE.value(), proxySubscribeRequest);
        } catch (MyHttpException e) {
            log.error("subscribe child error: {}, msg: {}", e.getCode(),e.getBody());
            retrySubscribeChild(targetGwId, proxySubscribeRequest, num);
        }
    }

    @Async("taskExecutor")
    public void unSubscribeChild(String conferenceId, String destination) {
        //只要还有用户订阅了本级会议就不能取消下级的
        if (subscribeManageService.hasSubScribe(destination)) {
            return;
        }
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferenceId);
        for (ChildMeetingInfo item : meetingInfo.getChildMeetingInfoMap().values()) {
            String childDestination = destination.replace(conferenceId, item.getId());
            unSubscribeChild(item.getId(), item.getGwId(), childDestination, conferenceId, meetingInfo.getAccessCode(), destination);
        }
    }

    @Async("taskExecutor")
    public void unSubscribeChild(String conferenceId, GwId targetGwId, String destination, String pConferenceId, String pAccessCode, String pDestination) {
        String remoteDestination = destination.replace(pConferenceId, conferenceId);//替换会议号
        ConferencesSubscribeAttachInfo conferencesSubscribeAttachInfo = new ConferencesSubscribeAttachInfo(conferenceId, pAccessCode);
        SubscribeAttachInfo subscribeAttachInfo = new SubscribeAttachInfo(SubscribeBusinessType.CONFERENCES, JSON.toJSONString(conferencesSubscribeAttachInfo));
        ProxySubscribeRequest proxySubscribeRequest = new ProxySubscribeRequest(remoteDestination, pDestination, nodeDataService.getTop().toGwId(), subscribeAttachInfo);
        try {
            remoteGwService.toByGwId(targetGwId).delete(ConfApiUrl.CASCADE_SUBSCRIBE.value(), proxySubscribeRequest);
        } catch (MyHttpException e) {
            //TODO  订阅异常的处理方式
        }
    }
}
