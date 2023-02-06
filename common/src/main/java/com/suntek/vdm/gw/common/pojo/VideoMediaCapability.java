package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

@Data
public class VideoMediaCapability {
    /**
     * 媒体流方向
     */
    private Integer mediaDirection;

    /**
     * 媒体带宽
     */
    private Integer mediaBandWidth;

    /**
     * 视频协议类型
     */
    private Integer videoProtocol;

    /**
     * 视频分辨率能力
     */
    private VideoResolutionCap videoResolutionCap;
}