package com.suntek.vdm.gw.conf.service.impl;


import com.suntek.vdm.gw.conf.pojo.ChildMeetingInfo;
import com.suntek.vdm.gw.conf.service.MeetingInfoManagerAsyncService;
import com.suntek.vdm.gw.core.service.RemoteGwService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MeetingInfoManagerAsyncServiceImpl implements MeetingInfoManagerAsyncService {

    @Autowired
    private RemoteGwService remoteGwService;
    /**
     * 初始化级联会议
     * @param childMeetingInfo
     */
    @Async("taskExecutor")
    public void initCasConf(ChildMeetingInfo childMeetingInfo){
        childMeetingInfo.loadInfo(remoteGwService);
//        //如果为空 或者已经初始化  或者正在初始化  都直接返回
//        if (childMeetingInfo == null || childMeetingInfo.initialized() || childMeetingInfo.initializing()) {
//            return;
//        }else {
//            try {
//                childMeetingInfo.setLastUpdateTime(System.currentTimeMillis());//设置初始化事件
//                GetConditionsMeetingRequest body = new GetConditionsMeetingRequest();
//                //根据接入号（级联会议接入号）查询会议
//                body.setCasConfId(childMeetingInfo.getConfCasId());
//                String responseJson = remoteGwService.toByGwId(childMeetingInfo.getGwId()).post(String.format(ConfApiUrl.CONFERENCES_CONDITIONS.value(), 0, 10), body).getBody();
//                GetConditionsMeetingResponse response = JSON.parseObject(responseJson, GetConditionsMeetingResponse.class);
//                if (response.getContent() != null && response.getContent().size() > 0) {
//                    ScheduleConfBrief childConfInfo = response.getContent().get(0);
//                    childMeetingInfo.setId(childConfInfo.getId());
//                } else {
//                    //TODO  查找下级会议失败
//                }
//            } catch (MyHttpException e) {
//
//            } catch (Exception e) {
//                log.error("exception",e);
//            }
//        }
    }
}
