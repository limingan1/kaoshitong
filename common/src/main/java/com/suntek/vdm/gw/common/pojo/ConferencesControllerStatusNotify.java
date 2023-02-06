package com.suntek.vdm.gw.common.pojo;
import lombok.Data;

@Data
public class ConferencesControllerStatusNotify {
    private ConferenceState state;
    private ConferenceParam param;
    private Integer type;
    private String confCasId;
}
