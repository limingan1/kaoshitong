package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class SetSubtitleResponse {
    /**
     * 会议ID
     */
    private String confId;

    /**
     * 结果
     */
    private Integer result;

    /**
     * AI字幕是否开启
     */
    private Integer isSet;
}