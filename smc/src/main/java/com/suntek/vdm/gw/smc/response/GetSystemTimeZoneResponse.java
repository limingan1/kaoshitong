package com.suntek.vdm.gw.smc.response;

import lombok.Data;

/**
 * 获取系统时间
 */
@Data
public class GetSystemTimeZoneResponse {
    /**
     *时间
     */
    private String systemTime;
    /**
     *时区
     */
    private String systemTimeZone;

}
