package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

@Data
public class MediaCapabilitySet {
    /**
     * 音频媒体能力
     */
    private AudioMediaCapability audioMediaCapability;

    /**
     * 视频媒体能力
     */
    private VideoMediaCapability videoMediaCapability;

    /**
     * 辅流媒体能力
     */
    private VideoMediaCapability auxMediaCapability;

    /**
     * 数据媒体能力
     */
    private DataMediaCapability dataMediaCapability;
}