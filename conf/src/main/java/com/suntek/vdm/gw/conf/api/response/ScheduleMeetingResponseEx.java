package com.suntek.vdm.gw.conf.api.response;


import com.suntek.vdm.gw.common.enums.SmcVersionType;
import com.suntek.vdm.gw.conf.pojo.CascadeChannel;
import com.suntek.vdm.gw.smc.response.meeting.management.ScheduleMeetingResponse;
import lombok.Data;

import java.util.List;

@Data
public class ScheduleMeetingResponseEx extends ScheduleMeetingResponse {
    /**
     * 回复上级本级节点id
     */
    private String nodeName;

    private String vmNodeName;
    /**
     * 回复上级本级节点SM版本
     */
    private SmcVersionType smcVersionType;

    private String accessCode;

}
