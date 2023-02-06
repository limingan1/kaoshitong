package com.suntek.vdm.gw.conf.api.request;


import com.suntek.vdm.gw.common.enums.SmcVersionType;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.smc.request.meeting.management.ScheduleMeetingRequest;
import lombok.Data;

import java.util.List;

@Data
public class ScheduleMeetingRequestEx extends ScheduleMeetingRequest {
    private int cascadeNum;
    private String nodeName;
    private String accessCode;
    private SmcVersionType smcVersionType;
    private GwId gwId;
    private GwId targetGwId;
    private Boolean isVmr;
    private List<ChildNodeInfos> child;
    private String vmNodeName;

    public String getSubject() {
        return getConference().getSubject();
    }

}
