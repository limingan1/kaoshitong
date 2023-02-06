package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class McuInfo {
    /**
     * MCU ID
     */
    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * MCU类型
     */
    private String mcuType;

    /**
     * MCU状态
     */
    private McuStatus status;
}