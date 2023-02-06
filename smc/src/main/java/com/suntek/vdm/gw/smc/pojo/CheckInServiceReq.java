package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class CheckInServiceReq {
    /**
     * 是否支持签到（可选）
     */
    private Boolean enableCheckIn;

    /**
     * 提前签到时间，单位分钟
     */
    private Integer checkInDuration;

}