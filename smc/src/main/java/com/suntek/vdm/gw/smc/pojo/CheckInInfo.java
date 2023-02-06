package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class CheckInInfo {
    /**
     * 签到姓名(1~64字符)
     */
    private String name;

    /**
     * 签到账号(1~64字符)
     */
    private String account;

    /**
     * 签到时间(UTC时间,格式为yyyy-MM-dd   HH:mm:ss z)
     */
    private String checkInTime;

    /**
     * 签到类型
     */
    private String type;

    /**
     * 与会人姓名(1~64字符)
     */
    private String participantName;
}