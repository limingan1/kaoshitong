package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

import java.util.List;

@Data
public class ParticipantCapabilitySet {
    /**
     * 音频能力集
     */
    private List<Integer> audioMediaCapabilitySet;

    /**
     * 视频能力集
     */
    private List<ParticipantVideoCapability> videoMediaCapabilitySet;

    /**
     * 辅流能力集
     */
    private List<ParticipantVideoCapability>auxMediaCapabilitySet;

    /**
     * 加密方式
     */
    private Integer encryptionType;
}