package com.suntek.vdm.gw.conf.pojo;

import lombok.Data;

import java.util.List;

@Data
public class ConferenceStatusNotify {
    private List<ConferenceStatusInfo> conferenceStages;
}
