package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

@Data
public class VideoResolutionCap {
    /**
     * 视频能力
     */
    private Integer videoResolution;

    /**
     * 帧率
     */
    private String frame;
}