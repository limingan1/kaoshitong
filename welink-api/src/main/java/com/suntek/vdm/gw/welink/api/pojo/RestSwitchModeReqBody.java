package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class RestSwitchModeReqBody {
    /**
     * 会议显示策略。
     * ● “Fixed”： 固定广 播与会者；
     * ● “VAS”：声控切 换。
     */
    private String switchMode;

    /**
     * 画面类型。
     * ● 0：单画面；
     * ● 1：多画面。
     * 单画面设置只针对声控 模式。
     */
    private Integer imageType;

    /**
     * 被广播或点名的会场
     */
    private String participantID;

    public RestSwitchModeReqBody(String switchMode, Integer imageType) {
        this.switchMode = switchMode;
        this.imageType = imageType;
    }
}