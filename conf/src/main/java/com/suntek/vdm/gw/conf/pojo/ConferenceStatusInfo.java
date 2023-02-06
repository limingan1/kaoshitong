package com.suntek.vdm.gw.conf.pojo;

import lombok.Data;

@Data
public class ConferenceStatusInfo {
    private String conferenceId;
    private String stage;
    private Integer failTimes;
}
