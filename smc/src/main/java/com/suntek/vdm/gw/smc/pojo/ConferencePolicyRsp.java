package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class ConferencePolicyRsp {
    /**
     * 自动延长会议
     */
    private Boolean autoExtend;

    /**
     * 自动结束会议
     */
    private Boolean autoEnd;

    /**
     * 自动闭音
     */
    private Boolean autoMute;

    /**
     * 语音提示语言
     */
    private Integer language;

    /**
     * 声控切换
     */
    private Boolean voiceActive;

    /**
     * 时区
     */
    private String timeZoneId;
}