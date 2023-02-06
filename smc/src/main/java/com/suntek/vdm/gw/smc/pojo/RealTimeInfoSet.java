package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class RealTimeInfoSet {
    /**
     * 会场码流发送信息
     */
    private RealTimeInfo sendRealTimeInfo;

    /**
     * 会场码流接收信息
     */
    private RealTimeInfo receiveRealTimeInfo;

    /**
     * 实时音量
     */
    private Integer volume;
}