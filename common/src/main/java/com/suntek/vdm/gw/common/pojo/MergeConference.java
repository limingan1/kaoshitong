package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

@Data
public class MergeConference {
    private String targetConfId;
    private String targetGwId;
    private Integer cascadeNum;
}
