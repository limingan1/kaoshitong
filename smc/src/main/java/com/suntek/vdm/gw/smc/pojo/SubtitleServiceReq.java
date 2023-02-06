package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class SubtitleServiceReq {
    /**
     * 是否支持字幕（可选）
     */
    private Boolean enableSubtitle;

    /**
     * 字幕源语种（可选）
     */
    private Integer srcLang;
}