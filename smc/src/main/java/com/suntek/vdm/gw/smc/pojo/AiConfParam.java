package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class AiConfParam {
    /**
     * 是否支持AI实时字幕
     */
    private Boolean supportSubtitle;

    /**
     * 是否支持实时纪要
     */
    private Boolean supportMinutes;

    /**
     * 支持并发发言方数
     */
    private Integer concurrentSpeakerNum;
}