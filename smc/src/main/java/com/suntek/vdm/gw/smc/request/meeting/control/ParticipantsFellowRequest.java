package com.suntek.vdm.gw.smc.request.meeting.control;

import lombok.Data;

@Data
public class ParticipantsFellowRequest {
    /**
     * 被跟随会场Id(36字符)
     */
    private  String sourceId;
}
