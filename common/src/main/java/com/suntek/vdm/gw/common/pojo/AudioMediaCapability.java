package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

@Data
public class AudioMediaCapability {
    /**
     * 媒体流方向
     */
    private Integer mediaDirection;

    /**
     * 媒体带宽
     */
    private Integer mediaBandWidth;

    /**
     * 音频协议类型
     */
    private Integer audioProtocol;
}