package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

import java.util.List;

@Data
public class SubtitleServiceRsp {
    /**
     * 是否支持字幕
     */
    private Boolean enableSubtitle;

    /**
     * 字幕源语种
     */
    private String srcLang;

    /**
     * 支持的语种列表
     */
    private List<String> supLanguageList;

    /**
     * 终端订阅地址
     */
    private String subscribePath;
}