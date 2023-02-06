package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

@Data
public class DataMediaBindRelation {
    /**
     * 数据会场ID
     */
    private String dataParticipantId;

    /**
     * 音视频会场ID
     */
    private String mediaParticipantId;

    /**
     * 是否添加
     */
    private Boolean isAdd;
}