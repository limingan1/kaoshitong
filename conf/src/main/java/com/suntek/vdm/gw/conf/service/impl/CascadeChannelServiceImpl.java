package com.suntek.vdm.gw.conf.service.impl;

import com.suntek.vdm.gw.conf.enumeration.SiteTypes;
import com.suntek.vdm.gw.conf.pojo.CasChannelPartys;
import com.suntek.vdm.gw.conf.service.CascadeChannelService;
import com.suntek.vdm.gw.conf.service.MeetingInfoManagerService;
import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import com.suntek.vdm.gw.common.pojo.MultiPicInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class CascadeChannelServiceImpl implements CascadeChannelService {
    @Autowired
    MeetingInfoManagerService meetingInfoManagerService;

    private static Map<String, CasChannelPartys> casChannelPartysMap = new ConcurrentHashMap<>();


    @Override
    public void updateChannelLstInfo(String conferenceId, String casConfId, CascadeParticipantDirection direction, Integer index, SiteTypes siteTypes, MultiPicInfo momentVideoSource) {
        log.info("Update cascad channel info conferenceId:{} casConfId:{} direction:{} index:{}",conferenceId,casConfId,direction,index);
        CasChannelPartys casChannelPartys = casChannelPartysMap.get(conferenceId);
        if (casChannelPartys != null) {
            casChannelPartys.updateChannelLstInfo(casConfId, direction, siteTypes, momentVideoSource, index);
        }
    }
}
