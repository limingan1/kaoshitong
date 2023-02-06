package com.suntek.vdm.gw.common.pojo;

import com.suntek.vdm.gw.common.enums.CascadeChannelNotifyType;
import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import com.suntek.vdm.gw.common.pojo.MultiPicInfo;
import lombok.Data;


@Data
public class CascadeChannelNotifyInfo {
    private String confCasId;
    private String remoteConferenceId;
    private CascadeParticipantDirection direction;
    private Integer index;
    private String changeParticipantId;
    private CascadeChannelNotifyType cascadeChannelNotifyType;
    private MultiPicInfo multiPicInfo;

}
