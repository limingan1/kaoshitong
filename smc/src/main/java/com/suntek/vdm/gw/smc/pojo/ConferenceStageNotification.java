package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class ConferenceStageNotification {
    /**
     * 会议ID
     */
    private String conferenceId;

    /**
     * 会议所处的状态
     */
    private String stage;
}