package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class McuStatus {
    /**
     * 是否在线
     */
    private Boolean online;

    /**
     * gk注册状态
     */
    private Boolean gkStats;

    /**
     * sip注册状态
     */
    private Boolean sipState;

    /**
     * http连接状态
     */
    private Boolean httpState;

    /**
     * 告警状态
     */
    private Integer alarmState;
}