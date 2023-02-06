package com.suntek.vdm.gw.common.pojo;

import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import lombok.Data;

@Data
public class CascadeChannelFreeInfo {
    private String confCasId;
    private String remoteConferenceId;
    private CascadeParticipantDirection direction;
    private Integer index;
    private String changeParticipantId;
}
