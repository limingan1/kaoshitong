package com.suntek.vdm.gw.conf.service;

import com.suntek.vdm.gw.conf.enumeration.SiteTypes;
import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import com.suntek.vdm.gw.common.pojo.MultiPicInfo;

public interface CascadeChannelService {
    public void updateChannelLstInfo(String conferenceId, String casConfId, CascadeParticipantDirection direction, Integer index, SiteTypes siteTypes, MultiPicInfo momentVideoSource);
}
