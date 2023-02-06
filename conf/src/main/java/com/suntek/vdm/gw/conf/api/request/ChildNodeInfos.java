package com.suntek.vdm.gw.conf.api.request;

import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.ParticipantReq;
import com.suntek.vdm.gw.smc.pojo.AttendeeReq;
import lombok.Data;

import java.util.List;

@Data
public class ChildNodeInfos {
    private String casOrgId;
    private String casOrgName;
    private Boolean isVmr;
    private List<ParticipantReq> participants;
    private List<AttendeeReq> attendees;
    private List<ChildNodeInfos> child;

    public GwId getGwId(){
        return GwId.valueOf(casOrgId);
    }
}
