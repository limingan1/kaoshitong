package com.suntek.vdm.gw.smc.request.meeting.control;

import lombok.Data;

@Data
public class SetCommonlyUsedParticipantsRequest {
    /**
     * * 是否设置(设置：true/取消：false)
     */
    private  Boolean set;
    /**
     * * 会场列表(>=1字符)
     */
    private  String participantIdList;

}
