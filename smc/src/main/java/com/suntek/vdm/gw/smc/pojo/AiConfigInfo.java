package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class AiConfigInfo {
    /**
     * 是否支持字幕
     */
    private Integer supportSubtitle;

    /**
     * 是否支持翻译
     */
    private Integer supportTrans;

    /**
     * 是否支持纪要
     */
    private Integer supportMinutes;

    /**
     * 并发发言方
     */
    private Integer concurrentSpeakerNum;

    /**
     * 会议开始相对时间
     */
    private long confStartRelativeTime;

    /**
     * 本场会议源语言
     */
    private String srcLang;
}