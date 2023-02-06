package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class CheckInServiceRsp {
    /**
     * 是否支持签到
     */
    private Boolean enableCheckIn;

    /**
     * 提前签到时间
     */
    private Integer checkInDuration;
}