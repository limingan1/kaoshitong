package com.suntek.vdm.gw.conf.service.impl;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.conf.pojo.CallCascadeChannelInfo;
import com.suntek.vdm.gw.conf.service.TempService;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.common.pojo.request.meeting.ParticipantsControlRequest;
import com.suntek.vdm.gw.smc.service.SmcMeetingControlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 临时代码
 */
@Service
@Slf4j
public class TempServiceImpl  extends BaseServiceImpl  implements TempService {
    @Autowired
    private SmcMeetingControlService smcMeetingControlService;

    private static List<CallCascadeChannelInfo> callCascadeChannelInfos=new ArrayList<>();

    public void callCascadeChannelAdd(String conferenceId,String pId){
        synchronized (TempServiceImpl.callCascadeChannelInfos){
            CallCascadeChannelInfo callCascadeChannelInfo=new CallCascadeChannelInfo(conferenceId,pId,System.currentTimeMillis()+1000*10);
            log.info("callCascadeChannelAdd conferenceId:{} pid:{}",conferenceId,pId);
            callCascadeChannelInfos.add(callCascadeChannelInfo);
        }
    }

    public void callCascadeChannelDel(String conferenceId,String pId){
        synchronized (TempServiceImpl.callCascadeChannelInfos){
            log.info("callCascadeChannelDel conferenceId:{} pid:{}",conferenceId,pId);
            callCascadeChannelInfos.removeIf(x->x.getConferenceId().equals(conferenceId)&&x.getPId().equals(pId));
        }
    }

    /**
     * 呼叫级联通道(临时代码)
     */
    public void callCascadeChannel(){
        synchronized (TempServiceImpl.callCascadeChannelInfos){
            for (CallCascadeChannelInfo item:callCascadeChannelInfos){
                if (item.getStartTime()<System.currentTimeMillis()){
                    ParticipantsControlRequest request=new ParticipantsControlRequest();
                    request.setIsOnline(true);
                    try{
                        smcMeetingControlService.participantsControl(item.getConferenceId(),item.getPId(),request,getSmcToken(CoreConfig.INTERNAL_USER_TOKEN));
                    }catch (MyHttpException e){

                    }
                }
            }
        }
    }
}
