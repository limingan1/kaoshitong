package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class RealTimeInfo {
    /**
     * 视频协议
     */
    private Integer videoProtocol;

    /**
     * 视频能力
     */
    private Integer videoResolution;

    /**
     * 视频带宽
     */
    private Integer videoBandWidth;

    /**
     * 音频协议
     */
    private Integer audioProtocol;

    /**
     * 音频带宽
     */
    private Integer audioBandWidth;

    /**
     * 辅流协议
     */
    private Integer auxProtocol;

    /**
     * 辅流能力
     */
    private Integer auxResolution;

    /**
     * 是否打开辅流通道
     */
    private Boolean openAux;
}